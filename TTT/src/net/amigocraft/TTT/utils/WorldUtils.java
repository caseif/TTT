package net.amigocraft.TTT.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import net.amigocraft.TTT.TTT;

import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class WorldUtils {
	
	private TTT plugin = TTT.plugin;
	
	// world checking method from Multiverse
	public static boolean isWorld(File worldFolder){
		File[] files = worldFolder.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File file, String name){
				return name.equalsIgnoreCase("level.dat");
			}
		});
		if (files != null && files.length > 0){
			return true;
		}
		return false;
	}
	
	public static void teleportPlayer(Player p){
		try {
			File spawnFile = new File(TTT.plugin.getDataFolder() + File.separator + "spawn.yml");
			if (spawnFile.exists()){
				YamlConfiguration spawnYaml = new YamlConfiguration();
				if (spawnYaml.isSet("world") && spawnYaml.isSet("x") &&
						spawnYaml.isSet("y") && spawnYaml.isSet("z"));
				Location l = new Location(TTT.plugin.getServer().getWorld(spawnYaml.getString("world")),
						spawnYaml.getDouble("x"), spawnYaml.getDouble("y"), spawnYaml.getDouble("z"));
				if (spawnYaml.isSet("pitch"))
					l.setPitch((float)spawnYaml.getDouble("pitch"));
				if (spawnYaml.isSet("yaw"))
					l.setYaw((float)spawnYaml.getDouble("yaw"));
				p.teleport(l);
			}
			else
				p.teleport(TTT.plugin.getServer().getWorlds().get(0).getSpawnLocation());
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public void rollbackWorld(final String worldName){
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			public void run(){
				File folder = new File(worldName);
				if (folder.exists()){
					if (WorldUtils.isWorld(folder)){
						File newFolder = new File("TTT_" + worldName);
						try {
							FileUtils.copyDirectory(folder, newFolder);
							plugin.log.info(plugin.local.getMessage("rollback") + " \"" + worldName + "\"!");
						}
						catch (IOException ex){
							plugin.log.info(plugin.local.getMessage("folder-error") + " " + worldName);
							ex.printStackTrace();
						}
					}
					else
						plugin.log.info(plugin.local.getMessage("cannot-load-world"));
				}
			}
		}, 100L);
	}
}
