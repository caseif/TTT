package net.amigocraft.TTT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import net.amigocraft.TTT.AutoUpdate;
import net.amigocraft.TTT.Metrics;
import net.amigocraft.TTT.listeners.BlockListener;
import net.amigocraft.TTT.listeners.PlayerListener;
import net.amigocraft.TTT.localization.Localization;
import net.amigocraft.TTT.managers.CommandManager;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class TTT extends JavaPlugin implements Listener {

	public static String ANSI_RED = "\u001B[31m";
	public static String ANSI_GREEN = "\u001B[32m";
	public static String ANSI_WHITE = "\u001B[37m";

	public Logger log = this.getLogger();
	public static TTT plugin = new TTT();
	public Localization local = new Localization();
	public static String lang;

	public HashMap<String, Integer> time = new HashMap<String, Integer>();
	public HashMap<String, Integer> tasks = new HashMap<String, Integer>();
	public HashMap<String, Integer> gameTime = new HashMap<String, Integer>();
	public List<Body> bodies = new ArrayList<Body>();
	public List<Body> foundBodies = new ArrayList<Body>();
	public List<String> discreet = new ArrayList<String>();

	public int tries = 0;

	@Override
	public void onEnable(){
		// check if server is offline
		if (!getServer().getOnlineMode()){
			if (!getServer().getIp().equals("127.0.0.1") && !getServer().getIp().equals("localhost")){
				log.info("This plugin does not support offline servers! Disabling...");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			else if (plugin.getConfig().getBoolean("verbose-logging"))
				log.info("Server is probably using BungeeCord. Allowing plugin to load...");
		}

		// register events, commands, and the plugin variable
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getCommand("ttt").setExecutor(new CommandManager());
		TTT.plugin = this;

		// check if config should be overwritten
		saveDefaultConfig();
		if (!getConfig().getString("config-version").equals(this.getDescription().getVersion())){
			File config = new File(this.getDataFolder(), "config.yml");
			config.delete();
		}

		// create the default config
		saveDefaultConfig();

		TTT.lang = getConfig().getString("localization");

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

		if (plugin.getConfig().getBoolean("verbose-logging"))
			log.info(this + " " + local.getMessage("enabled"));
	}

	@Override
	public void onDisable(){
		ANSI_RED = null;
		ANSI_GREEN = null;
		ANSI_WHITE = null;
		plugin = null;
		lang = null;
		if (plugin.getConfig().getBoolean("verbose-logging"))
			log.info(this + " " + local.getMessage("disabled"));
	}
}
