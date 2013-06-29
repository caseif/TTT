package net.amigocraft.TTT;

import java.io.File;
import java.io.IOException;
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

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class TTT extends JavaPlugin implements Listener {

	public static Logger log;
	public static Logger kLog;
	public static TTT plugin;
	public Localization local = new Localization();
	public static String lang;

	public static List<Body> bodies = new ArrayList<Body>();
	public static List<Body> foundBodies = new ArrayList<Body>();

	public static int maxKarma = 1000;

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

		// check if config should be overwritten
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		else if (!getConfig().getString("config-version").equals(this.getDescription().getVersion())){
			File config = new File(this.getDataFolder(), "config.yml");
			config.delete();
			saveDefaultConfig();
		}

		lang = getConfig().getString("localization");

		createFile("karma.yml");
		createFile("bans.yml");

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
		File f = new File(TTT.plugin.getDataFolder(), "s");
		if (!f.exists()){
			if (getConfig().getBoolean("verbose-logging"))
				log.info(s + " not found, creating...");
			try {
				f.createNewFile();
			}
			catch (Exception ex){
				ex.printStackTrace();
				log.warning("Failed to write to " + s + "!");
			}
		}
	}

}
