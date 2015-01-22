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
package net.amigocraft.ttt;

import net.amigocraft.mglib.api.ConfigManager;
import net.amigocraft.mglib.api.Locale;
import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.Minigame;
import net.amigocraft.ttt.Metrics.Graph;
import net.amigocraft.ttt.listeners.MGListener;
import net.amigocraft.ttt.listeners.PlayerListener;
import net.amigocraft.ttt.listeners.SpecialPlayerListener;
import net.amigocraft.ttt.managers.command.CommandManager;
import net.amigocraft.ttt.managers.KarmaManager;
import net.amigocraft.ttt.managers.ScoreManager;
import net.amigocraft.ttt.managers.command.SpecialCommandManager;
import net.amigocraft.ttt.util.FileUtil;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

	public static boolean MGLIB = true;

	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_WHITE = "\u001B[37m";
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

	public static List<UUID> creator = new ArrayList<UUID>();
	public static List<UUID> alpha = new ArrayList<UUID>();
	public static List<UUID> testers = new ArrayList<UUID>();
	public static List<UUID> translators = new ArrayList<UUID>();

	@Override
	public void onEnable(){
		log = this.getLogger();
		kLog = Logger.getLogger("TTT Karma Debug");
		plugin = this;

		boolean compatibleMethod = false;
		if (Bukkit.getPluginManager().isPluginEnabled("MGLib")){
			try {
				Minigame.class.getMethod("isMGLibCompatible", String.class);
				compatibleMethod = true;
			}
			catch (NoSuchMethodException swallow){}
		}
		if (!Bukkit.getPluginManager().isPluginEnabled("MGLib") || !compatibleMethod || !Minigame.isMGLibCompatible("0.3.0")){
			MGLIB = false;
			Main.log.info(ANSI_RED + Main.locale.getMessage("mglib-required") + ANSI_WHITE);
			getServer().getPluginManager().registerEvents(new SpecialPlayerListener(), this);
			getCommand("ttt").setExecutor(new SpecialCommandManager());
			return;
		}

		// register plugin with MGLib
		mg = Minigame.registerPlugin(this);

		locale = mg.getLocale();

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

		try {
			File spawnFile = new File(Main.plugin.getDataFolder() + File.separator + "spawn.yml");
			if (spawnFile.exists()){
				YamlConfiguration spawnYaml = new YamlConfiguration();
				spawnYaml.load(spawnFile);
				World w = Bukkit.getWorld(spawnYaml.getString("world"));
				if (w == null){
					w = Bukkit.createWorld(new WorldCreator(spawnYaml.getString("world")));
				}
				if (w == null){
					mg.log(Main.locale.getMessage("error-setting-exit"), LogLevel.WARNING);
				}
				else {
					cm.setDefaultExitLocation(new Location(w, spawnYaml.getDouble("x"), spawnYaml.getDouble("y"), spawnYaml.getDouble("z")));
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			mg.log(Main.locale.getMessage("error-loading-exit"), LogLevel.WARNING);
		}

		// register events, commands, and the plugin variable
		getServer().getPluginManager().registerEvents(new MGListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getCommand("ttt").setExecutor(new CommandManager());

		// copy pre-0.5 folder
		File old = new File(Bukkit.getWorldContainer() + File.separator + "plugins", "Trouble In Terrorist Town");
		if (old.exists() && !getDataFolder().exists()){
			mg.log(locale.getMessage("folder-rename"), LogLevel.INFO);
			try {
				old.renameTo(getDataFolder());
			}
			catch (Exception ex){
				ex.printStackTrace();
				mg.log(locale.getMessage("folder-rename-error"), LogLevel.WARNING);
			}
		}

		// check if config should be overwritten
		if (!new File(getDataFolder(), "config.yml").exists()){
			saveDefaultConfig();
		}
		else if (!Config.IGNORE_CONFIG_VERSION && !Config.CONFIG_VERSION.equals(this.getDescription().getVersion())){
			File config = new File(this.getDataFolder(), "config.yml");
			try {
				FileUtil.copyFile(config, new File(this.getDataFolder(), "config.old.yml"));
			}
			catch (Exception ex){
				ex.printStackTrace();
				mg.log(locale.getMessage("config-copy-fail"), LogLevel.INFO);
			}
			config.delete();
			saveDefaultConfig();
		}

		createFile("karma.yml");
		createFile("bans.yml");

		// autoupdate
		if (Config.ENABLE_AUTO_UPDATE){
			new Updater(this, 52474, this.getFile(), Updater.UpdateType.DEFAULT, true);
		}

		// submit metrics
		if (Config.ENABLE_METRICS){
			try {
				Metrics metrics = new Metrics(this);
				Graph graph = metrics.createGraph("MGLib Version");
				graph.addPlotter(new Metrics.Plotter(Bukkit.getPluginManager().getPlugin("MGLib").getDescription().getVersion()) {
					public int getValue(){
						return 1;
					}
				});
				metrics.addGraph(graph);
				metrics.start();
			}
			catch (IOException e){
				if (Config.VERBOSE_LOGGING){
					mg.log(locale.getMessage("metrics-fail"), LogLevel.INFO);
				}
			}
		}

		File invDir = new File(this.getDataFolder() + File.separator + "inventories");
		invDir.mkdir();

		maxKarma = Config.MAX_KARMA;

		// add special players to list
		creator.add(UUID.fromString("8ea8a3c0-ab53-4d80-8449-fa5368798dfc")); // AngryNerd1

		alpha.add(UUID.fromString("93a94c4a-0ad1-49c5-be92-d6fb416f938a")); // HardcoreBukkit
		alpha.add(UUID.fromString("1fdac8d1-6c37-4afd-8a16-aba6bec4b101")); // jmm1999
		alpha.add(UUID.fromString("a83f8496-fa91-41e4-84e0-578a742704f7")); // jon674
		alpha.add(UUID.fromString("e6f80dfe-d8ec-490f-9267-75797a213577")); // jpf6368
		alpha.add(UUID.fromString("7d5ba8ca-4a7c-41ff-9a27-4f74d006b086")); // momhipie
		alpha.add(UUID.fromString("8c63bf21-ab7a-431b-aa45-c9e661e6e812")); // shiny3
		alpha.add(UUID.fromString("57cb8d8f-0e74-4eeb-8188-52adbed3e216")); // xJHA929x
		alpha.add(UUID.fromString("7fa299a6-1525-404c-a5f6-bf116cc2ceff")); // ZerosAce00000

		testers.add(UUID.fromString("1b7fa3f3-3ac6-408b-990c-60cd37450208")); // Alexandercitt
		testers.add(UUID.fromString("18d0e7d7-f331-43e1-aded-6204fed565c1")); // Callmegusgus
		testers.add(UUID.fromString("93ed0f85-c30d-4204-9908-1b3557c9b611")); // Captn_Carles
		testers.add(UUID.fromString("809cbc32-5d27-47ef-82ff-3f1ae8d6c3f8")); // MoustachedMudkip
		testers.add(UUID.fromString("63bcf544-1793-42c1-a9f9-dcfd34f72536")); // pdidy1
		testers.add(UUID.fromString("6f93a373-3765-4316-af73-199ca1ea14cf")); // redraskal
		testers.add(UUID.fromString("987c31ca-2939-4d37-aa0d-eb9f44c979cc")); // rob_black
		testers.add(UUID.fromString("3e2b55fe-5e77-4ecf-8053-9ac9f5b118a3")); // RokkeyCX
		testers.add(UUID.fromString("9ffc9678-2c59-4fb5-9025-bf424e32a5f7")); // SuicideSilence_
		testers.add(UUID.fromString("e7ddb2e9-c8ca-47dd-81ea-0dad36d61572")); // talkingmelon
		testers.add(UUID.fromString("29fa58ce-1ff0-4d43-a4de-e3dd16e7fbb5")); // Weblack

		translators.add(UUID.fromString("dc713cb2-d96d-490e-8f6b-758f2da08935")); // gasacz
		translators.add(UUID.fromString("8baab7be-4942-4742-854b-0687d783b985")); // guigo007
		translators.add(UUID.fromString("ece5d120-402a-4a32-b78a-fdfaf5adab33")); // JeyWake
		translators.add(UUID.fromString("a83f8496-fa91-41e4-84e0-578a742704f7")); // jon674
		translators.add(UUID.fromString("dcd6037d-a68d-4593-a857-7853406ec11e")); // Nikkolo_DTU
		translators.add(UUID.fromString("abdaf9ad-3034-43a2-b22e-ba81fb949708")); // Rocoty
		translators.add(UUID.fromString("3e2b55fe-5e77-4ecf-8053-9ac9f5b118a3")); // RokkeyCX
		translators.add(UUID.fromString("9ffc9678-2c59-4fb5-9025-bf424e32a5f7")); // SuicideSilence_
		translators.add(UUID.fromString("e4714759-8a41-468d-8f93-b796c0f17aaa")); // victormac737

		if (Config.VERBOSE_LOGGING){
			mg.log(this + " " + locale.getMessage("enabled"), LogLevel.INFO);
		}
	}

	@Override
	public void onDisable(){
		if (MGLIB){
			// uninitialize static variables so as not to cause memory leaks when reloading
			KarmaManager.playerKarma = null;
			ScoreManager.uninitialize();
			if (Config.VERBOSE_LOGGING){
				mg.log(this + " " + locale.getMessage("disabled"), LogLevel.INFO);
			}
		}
		plugin = null;
		lang = null;
	}

	public static void createFile(String s){
		File f = new File(Main.plugin.getDataFolder(), s);
		if (!f.exists()){
			if (Config.VERBOSE_LOGGING){
				mg.log(locale.getMessage("creating-file").replace("%", s), LogLevel.INFO);
			}
			try {
				f.createNewFile();
			}
			catch (Exception ex){
				ex.printStackTrace();
				mg.log(locale.getMessage("write-fail").replace("%", s), LogLevel.INFO);
			}
		}
	}

	public static void createLocale(String s){
		File exLocale = new File(Main.plugin.getDataFolder() + File.separator + "locales", s);
		if (!exLocale.exists()){
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
				while ((len = is.read(buffer)) != -1){
					os.write(buffer, 0, len);
				}
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
			finally {
				try {
					if (is != null){
						is.close();
					}
					if (os != null){
						os.close();
					}
				}
				catch (Exception exc){
					exc.printStackTrace();
				}
			}
		}
	}

}
