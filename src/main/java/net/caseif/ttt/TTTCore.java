/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016, Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.caseif.ttt;

import static net.caseif.ttt.util.constant.PluginInfo.FLINT_MAJOR_VERSION;
import static net.caseif.ttt.util.constant.PluginInfo.MIN_FLINT_VERSION;

import net.caseif.ttt.command.CommandManager;
import net.caseif.ttt.listeners.ListenerManager;
import net.caseif.ttt.lobby.StatusLobbySignPopulator;
import net.caseif.ttt.util.TelemetryRunner;
import net.caseif.ttt.util.compatibility.LegacyConfigFolderRenamer;
import net.caseif.ttt.util.compatibility.LegacyMglibStorageConverter;
import net.caseif.ttt.util.compatibility.LegacyMglibStorageDeleter;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.config.OperatingMode;
import net.caseif.ttt.util.config.TTTConfig;
import net.caseif.ttt.util.constant.Stage;
import net.caseif.ttt.util.helper.gamemode.ArenaHelper;
import net.caseif.ttt.util.helper.gamemode.ContributorListHelper;
import net.caseif.ttt.util.helper.platform.BungeeHelper;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ImmutableSet;
import net.caseif.flint.FlintCore;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.arena.SpawningMode;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.rosetta.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Minecraft port of Trouble In Terrorist Town.
 *
 * @author Maxim Roncace
 * @version 0.12.0-SNAPSHOT
 */
public class TTTCore {

    private static TTTCore INSTANCE;

    public static Minigame mg;

    public static Logger log;
    public static Logger kLog;
    private static JavaPlugin plugin;

    public static LocaleManager locale;
    public static TTTConfig config;
    public static ContributorListHelper clh;
    private static TelemetryRunner telRunner;

    public static final boolean HALLOWEEN;

    public static final Map<UUID, String> ARENA_EDITORS = new HashMap<>();

    // dedicated mode stuff
    private static Arena dedicatedArena;

    static {
        Calendar cal = Calendar.getInstance();
        HALLOWEEN = cal.get(Calendar.MONTH) == Calendar.OCTOBER && cal.get(Calendar.DAY_OF_MONTH) == 31;
    }

    TTTCore(JavaPlugin plugin, LocaleManager localeManager) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Cannot initialize singleton class TTTCore more than once");
        }
        INSTANCE = this;

        TTTCore.plugin = plugin;
        TTTCore.locale = localeManager;
    }

    public void initialize() {
        log = plugin.getLogger();
        kLog = Logger.getLogger("TTT Karma Debug");
        kLog.setParent(log);

        // register plugin with Flint
        mg = FlintCore.registerPlugin(plugin.getName());

        config = new TTTConfig(plugin.getConfig());

        if (FlintCore.getMajorVersion() != FLINT_MAJOR_VERSION) {
            TTTBootstrap.INSTANCE.failMajor();
        }

        if (FlintCore.getApiRevision() < MIN_FLINT_VERSION) {
            TTTBootstrap.INSTANCE.failMinor();
            return;
        }

        checkJavaVersion();
        checkBukkitVersion();

        if (TTTCore.config.get(ConfigKey.OPERATING_MODE) == OperatingMode.DEDICATED) {
            ArenaHelper.applyNextArena();
            if (Bukkit.getOnlinePlayers().size() > 0) {
                Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        BungeeHelper.initialize();
                    }
                });
            }
        }

        clh = new ContributorListHelper(TTTCore.class.getResourceAsStream("/contributors.txt"));

        if (TTTCore.config.get(ConfigKey.ENABLE_TELEMETRY)) {
            telRunner = new TelemetryRunner();
        }

        mg.setConfigValue(ConfigNode.FORBIDDEN_COMMANDS, ImmutableSet.of("kit", "msg", "pm", "r", "me", "back"));
        mg.setConfigValue(ConfigNode.STATUS_LOBBY_SIGN_POPULATOR,
                new StatusLobbySignPopulator(mg.getConfigValue(ConfigNode.STATUS_LOBBY_SIGN_POPULATOR)));

        applyConfigOptions();

        doCompatibilityActions();
        mg.setConfigValue(ConfigNode.SPAWNING_MODE, SpawningMode.RANDOM);

        // register events and commands
        ListenerManager.registerEventListeners();
        plugin.getCommand("ttt").setExecutor(new CommandManager());

        // check if config should be overwritten
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveDefaultConfig();
        } else {
            try {
                config.addMissingKeys();
            } catch (Exception ex) {
                ex.printStackTrace();
                log.severe("Failed to write new config keys!");
            }
        }

        createFile("karma.yml");
        createFile("bans.yml");

        File invDir = new File(plugin.getDataFolder() + File.separator + "inventories");
        invDir.mkdir();

        if (TTTCore.config.get(ConfigKey.OPERATING_MODE) == OperatingMode.DEDICATED) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                getDedicatedArena().getOrCreateRound().addChallenger(pl.getUniqueId());
            }
        }
    }

    public void applyConfigOptions() {
        locale.setDefaultLocale(TTTCore.config.get(ConfigKey.LOCALE));

        mg.setConfigValue(ConfigNode.MAX_PLAYERS, TTTCore.config.get(ConfigKey.MAXIMUM_PLAYERS));
        Stage.initialize();
        mg.setConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES,
                ImmutableSet.of(Stage.WAITING, Stage.PREPARING, Stage.PLAYING, Stage.ROUND_OVER));
    }

    public void deinitialize() {
        if (TTTCore.config.get(ConfigKey.VERBOSE_LOGGING)) {
            logInfo("info.plugin.disable", plugin.toString());
        }

        // uninitialize static variables so as not to cause memory leaks when reloading
        INSTANCE = null;
        mg = null;
        log = null;
        kLog = null;
        plugin = null;
        locale = null;
        config = null;
        clh = null;
        dedicatedArena = null;
    }

    public static TTTCore getInstance() {
        return INSTANCE;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static Arena getDedicatedArena() {
        return dedicatedArena;
    }

    public static void setDedicatedArena(Arena arena) {
        dedicatedArena = arena;
    }

    public void createFile(String s) {
        File f = new File(TTTCore.plugin.getDataFolder(), s);
        if (!f.exists()) {
            if (TTTCore.config.get(ConfigKey.VERBOSE_LOGGING)) {
                logInfo("info.plugin.compatibility.creating-file", s);
            }
            try {
                //noinspection ResultOfMethodCallIgnored
                f.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                logWarning("error.plugin.file-write", s);
            }
        }
    }

    public void createLocale(String s) {
        File exLocale = new File(TTTCore.plugin.getDataFolder() + File.separator + "locales", s);
        if (!exLocale.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                File dir = new File(TTTCore.plugin.getDataFolder(), "locales");
                //noinspection ResultOfMethodCallIgnored
                dir.mkdir();
                //noinspection ResultOfMethodCallIgnored
                exLocale.createNewFile();
                is = TTTCore.class.getClassLoader().getResourceAsStream("locales/" + s);
                os = new FileOutputStream(exLocale);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

    public void logInfo(String localizationKey, String... replacements) {
        log.info(localize(localizationKey, replacements));
    }

    public void logWarning(String localizationKey, String... replacements) {
        log.warning(localize(localizationKey, replacements));
    }

    public void logSevere(String localizationKey, String... replacements) {
        log.severe(localize(localizationKey, replacements));
    }

    private String localize(String localizationKey, String... replacements) {
        return locale.getLocalizable(localizationKey).withReplacements(replacements).localize();
    }

    private void doCompatibilityActions() {
        LegacyConfigFolderRenamer.renameLegacyFolder();

        LegacyMglibStorageConverter.convertArenaStore();
        LegacyMglibStorageConverter.convertLobbyStore();

        LegacyMglibStorageDeleter.deleteObsoleteStorage();
    }

    private void checkJavaVersion() {
        try {
            if (Float.parseFloat(StandardSystemProperty.JAVA_CLASS_VERSION.value()) < 52.0) {
                logWarning("error.plugin.old-java");
            }
        } catch (NumberFormatException ignored) {
            logWarning("error.plugin.unknown-java");
        }
    }

    private void checkBukkitVersion() {
        String ver = Bukkit.class.getPackage().getSpecificationVersion();
        if (ver.startsWith("1.8") && !ver.startsWith("1.8.8")) {
            logWarning("error.plugin.old-bukkit-1.8");
        }
    }

}
