package net.amigocraft.ttt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import net.amigocraft.mglib.api.ConfigManager;
import net.amigocraft.mglib.api.Locale;
import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.Minigame;
import net.amigocraft.ttt.Metrics;
import net.amigocraft.ttt.listeners.EntityListener;
import net.amigocraft.ttt.listeners.MGListener;
import net.amigocraft.ttt.listeners.PlayerListener;
import net.amigocraft.ttt.managers.CommandManager;
import net.amigocraft.ttt.managers.KarmaManager;
import net.amigocraft.ttt.managers.ScoreManager;
import net.amigocraft.ttt.utils.FileUtils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	public static String ANSI_RED = "\u001B[31m";
	public static String ANSI_WHITE = "\u001B[37m";
	public static Minigame mg;

	public static Logger log;
	public static Logger kLog;
	public static Main plugin;
	public static Locale locale;
	public static String lang;

	public static List<Body> bodies = new ArrayList<Body>();
	public static List<Body> foundBodies = new ArrayList<Body>();

	public static int maxKarma = 1000;

	public static String stability = "stable";
	
	public static List<UUID> creator = new ArrayList<UUID>();
	public static List<UUID> alpha = new ArrayList<UUID>();
	public static List<UUID> testers = new ArrayList<UUID>();
	public static List<UUID> translators = new ArrayList<UUID>();

	@Override
	public void onEnable(){
		log = this.getLogger();
		kLog = Logger.getLogger("TTT Karma Debug");
		plugin = this;
		
		// initialize config variables
		Variables.initialize();
		
		// register plugin with MGLib
		mg = Minigame.registerPlugin(this);
		
		locale = mg.getLocale();
		
		ConfigManager cm = mg.getConfigManager();
		cm.setBlockPlaceAllowed(false);
		cm.setBlockBreakAllowed(false);
		cm.setKitsAllowed(false);
		cm.setPMsAllowed(false);
		cm.setPlayerClass(TTTPlayer.class);
		cm.setRandomSpawning(false);
		cm.setTeleportationAllowed(false);
		cm.setTeamChatEnabled(true);
		cm.setDefaultPreparationTime(Variables.SETUP_TIME);
		cm.setDefaultPlayingTime(Variables.TIME_LIMIT);
		cm.setAllowJoinRoundWhilePreparing(true);
		cm.setAllowJoinRoundInProgress(false);

		// register events, commands, and the plugin variable
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
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
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		else if (!Variables.IGNORE_CONFIG_VERSION && !Variables.CONFIG_VERSION.equals(this.getDescription().getVersion())){
			File config = new File(this.getDataFolder(), "config.yml");
			try {
				FileUtils.copyFile(config, new File(this.getDataFolder(), "config.old.yml"));
			}
			catch (Exception ex){
				ex.printStackTrace();
				mg.log(locale.getMessage("config-copy-fail"), LogLevel.INFO);
			}
			config.delete();
			saveDefaultConfig();
		}

		if (Variables.ENABLE_VERSION_CHECK && !this.getDescription().getVersion().contains("SNAPSHOT"))
			checkVersion();

		createFile("karma.yml");
		createFile("bans.yml");

		// autoupdate
		if (Variables.ENABLE_AUTO_UPDATE){
			new Updater(this, 52474, this.getFile(), Updater.UpdateType.DEFAULT, true);
		}

		// submit metrics
		if (Variables.ENABLE_METRICS){
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			}
			catch (IOException e){
				if (Variables.VERBOSE_LOGGING)
					mg.log(locale.getMessage("metrics-fail"), LogLevel.INFO);
			}
		}

		File invDir = new File(this.getDataFolder() + File.separator + "inventories");
		invDir.mkdir();

		maxKarma = Variables.MAX_KARMA;
		
		// add special players to list
		creator.add(UUID.fromString("8ea8a3c0-ab53-4d80-8449-fa5368798dfc"));
		
		alpha.add(UUID.fromString("7fa299a6-1525-404c-a5f6-bf116cc2ceff")); // ZerosAce00000
		alpha.add(UUID.fromString("7d5ba8ca-4a7c-41ff-9a27-4f74d006b086")); // momhipie
		alpha.add(UUID.fromString("57cb8d8f-0e74-4eeb-8188-52adbed3e216")); // xJHA929x
		alpha.add(UUID.fromString("1fdac8d1-6c37-4afd-8a16-aba6bec4b101")); // jmm1999
		alpha.add(UUID.fromString("a83f8496-fa91-41e4-84e0-578a742704f7")); // jon674
		alpha.add(UUID.fromString("93a94c4a-0ad1-49c5-be92-d6fb416f938a")); // HardcoreBukkit
		alpha.add(UUID.fromString("8c63bf21-ab7a-431b-aa45-c9e661e6e812")); // shiny3
		alpha.add(UUID.fromString("e6f80dfe-d8ec-490f-9267-75797a213577")); // jpf6368
		
		testers.add(UUID.fromString("1b7fa3f3-3ac6-408b-990c-60cd37450208")); // Alexandercitt
		
		translators.add(UUID.fromString("a83f8496-fa91-41e4-84e0-578a742704f7")); // jon674
		translators.add(UUID.fromString("dcd6037d-a68d-4593-a857-7853406ec11e")); // Nikkolo_DTU
		translators.add(UUID.fromString("ece5d120-402a-4a32-b78a-fdfaf5adab33")); // JeyWake

		if (Variables.VERBOSE_LOGGING)
			mg.log(this + " " + locale.getMessage("enabled"), LogLevel.INFO);
	}

	@Override
	public void onDisable(){
		
		// uninitialize static variables so as not to cause memory leaks when reloading
		KarmaManager.playerKarma = null;
		ScoreManager.uninitialize();
		if (Variables.VERBOSE_LOGGING)
			mg.log(this + " " + locale.getMessage("disabled"), LogLevel.INFO);
		plugin = null;
		lang = null;
	}

	public void createFile(String s){
		File f = new File(Main.plugin.getDataFolder(), s);
		if (!f.exists()){
			if (Variables.VERBOSE_LOGGING)
				mg.log(locale.getMessage("creating-file").replace("%", s), LogLevel.INFO);
			try {
				f.createNewFile();
			}
			catch (Exception ex){
				ex.printStackTrace();
				mg.log(locale.getMessage("write-fail").replace("%", s), LogLevel.INFO);
			}
		}
	}

	public void createLocale(String s){
		File exLocale = new File(getDataFolder() + File.separator + "locales", s);
		if (!exLocale.exists()){
			InputStream is = null;
			OutputStream os = null;
			try {
				File dir = new File(getDataFolder(), "locales");
				dir.mkdir();
				exLocale.createNewFile();
				is = Main.class.getClassLoader().getResourceAsStream(
						"locales/" + s);
				os = new FileOutputStream(exLocale);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) != -1) {
					os.write(buffer, 0, len);
				}
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
			finally {
				try {
					is.close();
					os.close();
				}
				catch (Exception exc){
					exc.printStackTrace();
				}
			}
		}
	}

	public void checkVersion(){
		try {
			Thread t = new Thread(new BuildChecker());
			t.start();
			t.join(1000);
			if (t.isAlive() || (BuildChecker.response >= 400 && BuildChecker.response <= 499) ||
					(BuildChecker.response >= 500 && BuildChecker.response <= 599)){
				t.interrupt();
				if ((BuildChecker.response >= 400 && BuildChecker.response <= 499) ||
						(BuildChecker.response >= 500 && BuildChecker.response <= 599))
					mg.log(locale.getMessage("connect-fail-1"), LogLevel.INFO);
				else
					mg.log(locale.getMessage("connect-fail-2"), LogLevel.INFO);
				BuildChecker.response = 0;
				Thread t2 = new Thread(new BuildChecker());
				t2.start();
				t2.join(1000);
				if (t2.isAlive() || (BuildChecker.response >= 400 && BuildChecker.response <= 499) ||
						(BuildChecker.response >= 500 && BuildChecker.response <= 599)){
					t2.interrupt();
					String response = "";
					if ((BuildChecker.response >= 400 && BuildChecker.response <= 499) ||
							(BuildChecker.response >= 500 && BuildChecker.response <= 599))
						response = " (" +
								locale.getMessage("response").replace("%", Integer.toString(BuildChecker.response) +
										")");
					mg.log(locale.getMessage("connect-fail-3").replace(" %", response), LogLevel.WARNING);
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			mg.log(locale.getMessage("build-check-fail"), LogLevel.WARNING);
		}
	}

}
