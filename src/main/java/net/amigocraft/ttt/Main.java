package net.amigocraft.ttt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.amigocraft.mglib.api.ConfigManager;
import net.amigocraft.mglib.api.Locale;
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
			log.info(locale.getMessage("folder-rename"));
			try {
				old.renameTo(getDataFolder());
			}
			catch (Exception ex){
				ex.printStackTrace();
				log.warning(locale.getMessage("folder-rename-error"));
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
				log.warning(locale.getMessage("config-copy-fail"));
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
					log.warning(locale.getMessage("metrics-fail"));
			}
		}

		File invDir = new File(this.getDataFolder() + File.separator + "inventories");
		invDir.mkdir();

		maxKarma = Variables.MAX_KARMA;

		if (Variables.VERBOSE_LOGGING)
			log.info(this + " " + locale.getMessage("enabled"));
	}

	@Override
	public void onDisable(){
		
		// uninitialize static variables so as not to cause memory leaks when reloading
		KarmaManager.playerKarma = null;
		ScoreManager.uninitialize();
		if (Variables.VERBOSE_LOGGING)
			log.info(this + " " + locale.getMessage("disabled"));
		plugin = null;
		lang = null;
	}

	public void createFile(String s){
		File f = new File(Main.plugin.getDataFolder(), s);
		if (!f.exists()){
			if (Variables.VERBOSE_LOGGING)
				log.info(locale.getMessage("creating-file").replace("%", s));
			try {
				f.createNewFile();
			}
			catch (Exception ex){
				ex.printStackTrace();
				log.warning(locale.getMessage("write-fail").replace("%", s));
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
					log.info(locale.getMessage("connect-fail-1"));
				else
					log.info(locale.getMessage("connect-fail-2"));
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
					log.warning(locale.getMessage("connect-fail-3").replace(" %", response));
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			log.warning(locale.getMessage("build-check-fail"));
		}
	}

}
