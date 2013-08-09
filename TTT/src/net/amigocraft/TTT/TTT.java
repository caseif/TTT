package net.amigocraft.TTT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.amigocraft.TTT.AutoUpdate;
import net.amigocraft.TTT.Metrics;
import net.amigocraft.TTT.listeners.BlockListener;
import net.amigocraft.TTT.listeners.EntityListener;
import net.amigocraft.TTT.listeners.PlayerListener;
import net.amigocraft.TTT.localization.Localization;
import net.amigocraft.TTT.managers.CommandManager;
import net.amigocraft.TTT.utils.WorldUtils;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class TTT extends JavaPlugin implements Listener {

	public static String ANSI_RED = "\u001B[31m";
	public static String ANSI_WHITE = "\u001B[37m";

	public static Logger log;
	public static Logger kLog;
	public static TTT plugin;
	public static Localization local = new Localization();
	public static String lang;

	public static List<Body> bodies = new ArrayList<Body>();
	public static List<Body> foundBodies = new ArrayList<Body>();

	public static int maxKarma = 1000;

	public static boolean unstable = false;

	@Override
	public void onEnable(){
		log = this.getLogger();
		kLog = Logger.getLogger("TTT Karma Debug");
		plugin = this;

		// register events, commands, and the plugin variable
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getCommand("ttt").setExecutor(new CommandManager());

		createLocale("l33t.properties");
		createLocale("template.properties");
		lang = getConfig().getString("localization");
		Localization.initialize();

		// copy pre-0.5 folder
		File old = new File(Bukkit.getWorldContainer() + File.separator + "plugins", "Trouble In Terrorist Town");
		if (old.exists() && !getDataFolder().exists()){
			log.info(local.getMessage("folder-rename"));
			try {
				old.renameTo(getDataFolder());
			}
			catch (Exception ex){
				ex.printStackTrace();
				log.warning(local.getMessage("folder-rename-error"));
			}
		}

		// check if config should be overwritten
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		else if (!getConfig().getString("config-version").equals(this.getDescription().getVersion())){
			File config = new File(this.getDataFolder(), "config.yml");
			try {
				WorldUtils.copyFile(config, new File(this.getDataFolder(), "config.old.yml"));
			}
			catch (Exception ex){
				ex.printStackTrace();
				log.warning(local.getMessage("config-copy-fail"));
			}
			config.delete();
			saveDefaultConfig();
		}

		checkVersion();

		createFile("karma.yml");
		createFile("bans.yml");
		createFile("signs.yml");

		// autoupdate
		if (getConfig().getBoolean("enable-auto-update")){
			try {new AutoUpdate(this);}
			catch (Exception e){e.printStackTrace();}
		}

		// submit metrics
		if (getConfig().getBoolean("enable-metrics")){
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			}
			catch (IOException e) {
				if (plugin.getConfig().getBoolean("verbose-logging"))
					log.warning(local.getMessage("metrics-fail"));
			}
		}

		File invDir = new File(this.getDataFolder() + File.separator + "inventories");
		invDir.mkdir();

		maxKarma = getConfig().getInt("max-karma");

		if (getConfig().getBoolean("verbose-logging"))
			log.info(this + " " + local.getMessage("enabled"));
	}

	@Override
	public void onDisable(){
		if (getConfig().getBoolean("verbose-logging"))
			log.info(this + " " + local.getMessage("disabled"));
		plugin = null;
		lang = null;
	}

	public void createFile(String s){
		File f = new File(TTT.plugin.getDataFolder(), s);
		if (!f.exists()){
			if (getConfig().getBoolean("verbose-logging"))
				log.info(local.getMessage("creating-file").replace("%", s));
			try {
				f.createNewFile();
			}
			catch (Exception ex){
				ex.printStackTrace();
				log.warning(local.getMessage("write-fail").replace("%", s));
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
				is = TTT.class.getClassLoader().getResourceAsStream(
						"net/amigocraft/TTT/localization/example/" + s);
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
			t.join(2000);
			if (t.isAlive()){
				t.interrupt();
				log.info(local.getMessage("connect-fail-1"));
				Thread t2 = new Thread(new BuildChecker());
				t2.start();
				t2.join(2000);
				if (t2.isAlive()){
					t2.interrupt();
					log.warning(local.getMessage("connect-fail-2"));
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			log.warning(local.getMessage("build-check-fail"));
		}
	}

}
