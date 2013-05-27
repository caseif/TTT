package net.amigocraft.TTT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

	public Logger log;
	public static TTT plugin;
	public Localization local = new Localization();
	public static String lang;

	public List<Body> bodies = new ArrayList<Body>();
	public List<Body> foundBodies = new ArrayList<Body>();
	public List<String> discreet = new ArrayList<String>();

	@Override
	public void onEnable(){
		log = this.getLogger();
		plugin = this;
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
		if (plugin.getConfig().getBoolean("verbose-logging"))
			log.info(this + " " + local.getMessage("disabled"));
		plugin = null;
		lang = null;
	}
}
