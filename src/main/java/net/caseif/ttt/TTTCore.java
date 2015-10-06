/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncacé <mproncace@lapis.blue>
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

import static net.caseif.ttt.util.Constants.MIN_FLINT_VERSION;

import net.caseif.ttt.command.CommandManager;
import net.caseif.ttt.command.SpecialCommandManager;
import net.caseif.ttt.listeners.MinigameListener;
import net.caseif.ttt.listeners.PlayerListener;
import net.caseif.ttt.listeners.SpecialPlayerListener;
import net.caseif.ttt.listeners.WizardListener;
import net.caseif.ttt.listeners.WorldListener;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Constants.Stage;
import net.caseif.ttt.util.compatibility.LegacyConfigFolderRenamer;
import net.caseif.ttt.util.compatibility.LegacyMglibStorageConverter;
import net.caseif.ttt.util.helper.ConfigHelper;
import net.caseif.ttt.util.helper.ContributorListHelper;

import com.google.common.collect.ImmutableSet;
import net.caseif.crosstitles.TitleUtil;
import net.caseif.flint.FlintCore;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.rosetta.LocaleManager;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Minecraft port of Trouble In Terrorist Town.
 *
 * @author Maxim Roncacé
 * @version 0.8.0-SNAPSHOT
 */
public class TTTCore extends JavaPlugin {

    private static final String CODENAME = "Chad";

    public static boolean STEEL = true;
    public static Minigame mg;

    public static Logger log;
    public static Logger kLog;
    private static TTTCore plugin;
    public static LocaleManager locale;

    //TODO: associate bodies with rounds
    public static List<Body> bodies = new ArrayList<>();
    public static List<Body> foundBodies = new ArrayList<>();

    public static int maxKarma = 1000;

    public static ContributorListHelper clh;

    @Override
    public void onEnable() {
        log = this.getLogger();
        kLog = Logger.getLogger("TTT Karma Debug");
        kLog.setParent(log);
        plugin = this;
        locale = new LocaleManager(this);

        if (!Bukkit.getPluginManager().isPluginEnabled("Steel") || FlintCore.getApiRevision() < MIN_FLINT_VERSION) {
            STEEL = false;
            logInfo("error.plugin.flint", MIN_FLINT_VERSION + "");
            getServer().getPluginManager().registerEvents(new SpecialPlayerListener(), this);
            getCommand("ttt").setExecutor(new SpecialCommandManager());
            return;
        }

        clh = new ContributorListHelper(TTTCore.class.getResourceAsStream("/contributors.txt"));

        // register plugin with Flint
        mg = FlintCore.registerPlugin(getName());

        doCompatibilityActions();

        mg.setConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES,
                ImmutableSet.of(Stage.WAITING, Stage.PREPARING, Stage.PLAYING));
        mg.setConfigValue(ConfigNode.MAX_PLAYERS, ConfigHelper.MAXIMUM_PLAYERS);
        mg.setConfigValue(ConfigNode.RANDOM_SPAWNING, true);

        // register events and commands
        mg.getEventBus().register(new MinigameListener());
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new WizardListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getCommand("ttt").setExecutor(new CommandManager());

        // check if config should be overwritten
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        } else {
            try {
                ConfigHelper.addMissingKeys();
            } catch (Exception ex) {
                ex.printStackTrace();
                logSevere("Failed to write new config keys!");
            }
        }

        createFile("karma.yml");
        createFile("bans.yml");

        // autoupdate
        if (ConfigHelper.ENABLE_AUTO_UPDATE) {
            new Updater(this, 52474, this.getFile(), Updater.UpdateType.DEFAULT, true);
        }

        // submit metrics
        if (ConfigHelper.ENABLE_METRICS) {
            try {
                Metrics metrics = new Metrics(this);
                Metrics.Graph graph = metrics.createGraph("Steel Version");
                graph.addPlotter(new Metrics.Plotter(
                        Bukkit.getPluginManager().getPlugin("Steel").getDescription().getVersion()
                ) {
                    public int getValue() {
                        return 1;
                    }
                });
                metrics.addGraph(graph);
                metrics.start();
            } catch (IOException ex) {
                if (ConfigHelper.VERBOSE_LOGGING) {
                    logWarning("error.plugin.mcstats");
                }
            }
        }

        File invDir = new File(this.getDataFolder() + File.separator + "inventories");
        invDir.mkdir();

        maxKarma = ConfigHelper.KARMA_MAX;

        if (ConfigHelper.SEND_TITLES && !TitleUtil.areTitlesSupported()) {
            logWarning("error.plugin.title-support");
        }
    }

    @Override
    public void onDisable() {
        if (STEEL) {
            // uninitialize static variables so as not to cause memory leaks when reloading
            ScoreboardManager.uninitialize();
            if (ConfigHelper.VERBOSE_LOGGING) {
                logInfo("info.plugin.disable", this.toString());
            }
        }
        locale = null;
        plugin = null;
    }

    public static TTTCore getInstance() {
        return plugin;
    }

    public static String getCodename() {
        return CODENAME;
    }

    public void createFile(String s) {
        File f = new File(TTTCore.plugin.getDataFolder(), s);
        if (!f.exists()) {
            if (ConfigHelper.VERBOSE_LOGGING) {
                logInfo("info.plugin.compatibility.creating-file", s);
            }
            try {
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
                dir.mkdir();
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
    }

}
