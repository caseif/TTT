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

import static net.caseif.ttt.util.Constants.MIN_MGLIB_VERSION;

import net.caseif.ttt.listeners.MGListener;
import net.caseif.ttt.listeners.PlayerListener;
import net.caseif.ttt.listeners.SpecialPlayerListener;
import net.caseif.ttt.managers.KarmaManager;
import net.caseif.ttt.managers.ScoreManager;
import net.caseif.ttt.managers.command.CommandManager;
import net.caseif.ttt.managers.command.SpecialCommandManager;
import net.caseif.ttt.util.ContributorsReader;

import com.google.common.base.StandardSystemProperty;
import net.amigocraft.mglib.MGUtil;
import net.amigocraft.mglib.api.ConfigManager;
import net.amigocraft.mglib.api.Locale;
import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.Minigame;
import net.caseif.crosstitles.TitleUtil;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Minecraft port of Trouble In Terrorist Town.
 *
 * @author Maxim Roncacé
 * @version 0.7.0
 */
public class Main extends JavaPlugin {

	public static boolean MGLIB = true;
	public static Minigame mg;

	public static Logger log;
	public static Logger kLog;
	public static Main plugin;
	public static Locale locale;
	public static String lang;

	//TODO: associate bodies with rounds
	public static List<Body> bodies = new ArrayList<Body>();
	public static List<Body> foundBodies = new ArrayList<Body>();

	public static int maxKarma = 1000;

	public static List<UUID> devs = new ArrayList<UUID>();
	public static List<UUID> alpha = new ArrayList<UUID>();
	public static List<UUID> testers = new ArrayList<UUID>();
	public static List<UUID> translators = new ArrayList<UUID>();

	public static final String MGLIB_ERROR_MESSAGE = "This version of TTT requires MGLib version " + MIN_MGLIB_VERSION +
			" or higher. You can download and install it from http://dev.bukkit.org/bukkit-plugins/mglib/. " +
			"Note that TTT will not function without it!";

	@Override
	public void onEnable() {
		log = this.getLogger();
		kLog = Logger.getLogger("TTT Karma Debug");
		plugin = this;

		checkJavaVersion();

		boolean compatibleMethod = false;
		if (Bukkit.getPluginManager().isPluginEnabled("MGLib")) {
			try {
				Minigame.class.getMethod("isMGLibCompatible", String.class);
				compatibleMethod = true;
			}
			catch (NoSuchMethodException swallow) {
			}
		}
		if (!Bukkit.getPluginManager().isPluginEnabled("MGLib") || !compatibleMethod ||
				!Minigame.isMGLibCompatible(MIN_MGLIB_VERSION)) {
			MGLIB = false;
			Main.log.info(MGLIB_ERROR_MESSAGE);
			getServer().getPluginManager().registerEvents(new SpecialPlayerListener(), this);
			getCommand("ttt").setExecutor(new SpecialCommandManager());
			return;
		}

		// register plugin with MGLib
		mg = Minigame.registerPlugin(this);

		ConfigManager cm = mg.getConfigManager();
		cm.setBlockPlaceAllowed(false);
		cm.setBlockBreakAllowed(false);
		cm.setHangingBreakAllowed(false);
		cm.setKitsAllowed(false);
		cm.setPMsAllowed(false);
		cm.setRandomSpawning(false);
		cm.setTeleportationAllowed(false);
		cm.setTeamChatEnabled(true);
		cm.setDefaultPreparationTime(Config.SETUP_TIME);
		cm.setDefaultPlayingTime(Config.TIME_LIMIT);
		cm.setAllowJoinRoundWhilePreparing(true);
		cm.setAllowJoinRoundInProgress(false);
		cm.setMinPlayers(Config.MINIMUM_PLAYERS);
		cm.setMaxPlayers(Config.MAXIMUM_PLAYERS);
		cm.setPvPAllowed(true);
		cm.setTeamDamageAllowed(true);
		cm.setOverrideDeathEvent(true);
		cm.setMobSpawningAllowed(false);
		cm.setEntityTargetingEnabled(false);
		cm.setDefaultLocale(Config.LOCALE);

		locale = mg.getLocale();

		try {
			File spawnFile = new File(Main.plugin.getDataFolder() + File.separator + "spawn.yml");
			if (spawnFile.exists()) {
				YamlConfiguration spawnYaml = new YamlConfiguration();
				spawnYaml.load(spawnFile);
				World w = Bukkit.getWorld(spawnYaml.getString("world"));
				if (w == null) {
					w = Bukkit.createWorld(new WorldCreator(spawnYaml.getString("world")));
				}
				if (w == null) {
					Bukkit.getScheduler().runTaskLater(this, new Runnable() {
						public void run() {
							mg.log(locale.getMessage("error.plugin.set-exit"), LogLevel.WARNING);
						}
					}, 2L);
				}
				else {
					if (spawnYaml.isSet("pitch") && spawnYaml.isSet("yaw")) {
						cm.setDefaultExitLocation(new Location(
								w, spawnYaml.getDouble("x"), spawnYaml.getDouble("y"), spawnYaml.getDouble("z"),
								(float)spawnYaml.getDouble("yaw"), (float)spawnYaml.getDouble("pitch")
						));
					}
					else {
						cm.setDefaultExitLocation(new Location(
								w, spawnYaml.getDouble("x"), spawnYaml.getDouble("y"), spawnYaml.getDouble("z")
						));
					}
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					mg.log(locale.getMessage("error.plugin.load-exit"), LogLevel.WARNING);
				}
			}, 2L);
		}

		// register events, commands, and the plugin variable
		getServer().getPluginManager().registerEvents(new MGListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getCommand("ttt").setExecutor(new CommandManager());

		// copy pre-0.5 folder
		final File old = new File(Bukkit.getWorldContainer() + File.separator + "plugins", "Trouble In Terrorist Town");
		if (old.exists() && !getDataFolder().exists()) {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					mg.log(locale.getMessage("info.plugin.compatibility.rename"), LogLevel.INFO);
					try {
						old.renameTo(getDataFolder());
					}
					catch (Exception ex) {
						ex.printStackTrace();
						mg.log(locale.getMessage("error.plugin.folder-rename"), LogLevel.WARNING);
					}
				}
			}, 2L);
		}

		// check if config should be overwritten
		if (!new File(getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
		}
		else {
			try {
				Config.addMissingKeys();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				MGUtil.log("Failed to write new config keys!", null, LogLevel.SEVERE);
			}
		}

		createFile("karma.yml");
		createFile("bans.yml");

		// autoupdate
		if (Config.ENABLE_AUTO_UPDATE) {
			new Updater(this, 52474, this.getFile(), Updater.UpdateType.DEFAULT, true);
		}

		// submit metrics
		if (Config.ENABLE_METRICS) {
			try {
				Metrics metrics = new Metrics(this);
				Metrics.Graph graph = metrics.createGraph("MGLib Version");
				graph.addPlotter(new Metrics.Plotter(
						Bukkit.getPluginManager().getPlugin("MGLib").getDescription().getVersion()
				) {
					public int getValue() {
						return 1;
					}
				});
				metrics.addGraph(graph);
				metrics.start();
			}
			catch (IOException e) {
				if (Config.VERBOSE_LOGGING) {
					Bukkit.getScheduler().runTaskLater(this, new Runnable() {
						public void run() {
							mg.log(locale.getMessage("error.plugin.mcstats"), LogLevel.INFO);
						}
					}, 2L);
				}
			}
		}

		File invDir = new File(this.getDataFolder() + File.separator + "inventories");
		invDir.mkdir();

		maxKarma = Config.MAX_KARMA;

		// add special players to list
		ContributorsReader reader = new ContributorsReader(Main.class.getResourceAsStream("/contributors.txt"));
		Map<String, Set<String>> contributors = reader.read();

		if (contributors.containsKey("dev")) {
			for (String uuid : contributors.get("dev")) {
				devs.add(UUID.fromString(uuid));
			}
		}

		if (contributors.containsKey("alpha")) {
			for (String uuid : contributors.get("alpha")) {
				alpha.add(UUID.fromString(uuid));
			}
		}

		if (contributors.containsKey("tester")) {
			for (String uuid : contributors.get("tester")) {
				testers.add(UUID.fromString(uuid));
			}
		}

		if (contributors.containsKey("translator")) {
			for (String uuid : contributors.get("translator")) {
				translators.add(UUID.fromString(uuid));
			}
		}

		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				if (Config.SEND_TITLES && !TitleUtil.areTitlesSupported()) {
					Main.mg.log(Main.locale.getMessage("error.plugin.title-support"), LogLevel.WARNING);
				}
			}
		}, 2L);
	}

	@Override
	public void onDisable() {
		if (MGLIB) {
			// uninitialize static variables so as not to cause memory leaks when reloading
			KarmaManager.playerKarma = null;
			ScoreManager.uninitialize();
			if (Config.VERBOSE_LOGGING) {
				mg.log(locale.getMessage("info.plugin.disable", this.toString()), LogLevel.INFO);
			}
		}
		locale = null;
		plugin = null;
	}

	public static void createFile(String s) {
		File f = new File(Main.plugin.getDataFolder(), s);
		if (!f.exists()) {
			if (Config.VERBOSE_LOGGING) {
				mg.log(locale.getMessage("info.plugin.compatibility.creating-file", s), LogLevel.INFO);
			}
			try {
				f.createNewFile();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				mg.log(locale.getMessage("error.plugin.file-write", s), LogLevel.INFO);
			}
		}
	}

	public static void createLocale(String s) {
		File exLocale = new File(Main.plugin.getDataFolder() + File.separator + "locales", s);
		if (!exLocale.exists()) {
			InputStream is = null;
			OutputStream os = null;
			try {
				File dir = new File(Main.plugin.getDataFolder(), "locales");
				dir.mkdir();
				exLocale.createNewFile();
				is = Main.class.getClassLoader().getResourceAsStream("locales/" + s);
				os = new FileOutputStream(exLocale);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) != -1) {
					os.write(buffer, 0, len);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			finally {
				try {
					if (is != null) {
						is.close();
					}
					if (os != null) {
						os.close();
					}
				}
				catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}

	// borrowed/stolen from http://git.io/vGjDf
	private void checkJavaVersion() {
		try {
			if (Float.parseFloat(StandardSystemProperty.JAVA_CLASS_VERSION.value()) < 52.0) {
				getLogger().warning("You're using Java 7 or lower. Keep in mind that future versions of TTT are " +
						"likely to require Java 8 or higher. Java 8 contains various improvements, especially " +
						"regarding performance. If possible, please upgrade as soon as possible.");
			}
		} catch (NumberFormatException ignored) {
			getLogger().warning("Failed to detect Java version. If you're using Java 7 or lower, keep in mind that " +
					"future versions of TTT are likely to require Java 8 or higher. Java 8 contains various " +
					"improvements, especially regarding performance. If possible, please upgrade as soon as possible.");
		}
	}

}
