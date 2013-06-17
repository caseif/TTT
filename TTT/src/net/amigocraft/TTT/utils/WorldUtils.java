package net.amigocraft.TTT.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.amigocraft.TTT.TTT;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class WorldUtils {

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

	public static void rollbackWorld(final String worldName){
		TTT.plugin.getServer().getScheduler().scheduleSyncDelayedTask(TTT.plugin, new Runnable(){
			public void run(){
				File folder = new File(worldName);
				if (folder.exists()){
					if (WorldUtils.isWorld(folder)){
						File newFolder = new File("TTT_" + worldName);
						try {
							copyDirectory(folder, newFolder);
							if (TTT.plugin.getConfig().getBoolean("verbose-logging"))
								TTT.log.info(TTT.plugin.local.getMessage("rollback") + " \"" + worldName + "\"!");
						}
						catch (IOException ex){
							TTT.log.info(TTT.plugin.local.getMessage("folder-error") + " " + worldName);
							ex.printStackTrace();
						}
					}
					else
						TTT.log.info(TTT.plugin.local.getMessage("cannot-load-world"));
				}
			}
		}, 100L);
	}

	public static void importWorld(CommandSender sender, String worldName){
		File folder = new File(worldName);
		if (folder.exists()){
			if (!worldName.substring(0, 3).equalsIgnoreCase("TTT_")){
				if (isWorld(folder)){
					File newFolder = new File("TTT_" + worldName);
					if (!newFolder.exists()){
						try {
							File sessionLock = new File(folder + File.separator + "session.lock");
							File uidDat = new File(folder + File.separator + "uid.dat");
							sessionLock.delete();
							uidDat.delete();
							copyDirectory(folder, newFolder);
							sender.sendMessage(ChatColor.GREEN + "[TTT] " + TTT.plugin.local.getMessage("import-success"));
						}
						catch (IOException e){
							sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.plugin.local.getMessage("folder-error"));
							e.printStackTrace();
						}
					}
					else
						sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.plugin.local.getMessage("already-imported"));
				}
				else
					sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.plugin.local.getMessage("cannot-load-world"));
			}
			else
				sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.plugin.local.getMessage("start-error"));
		}
		else
			sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.plugin.local.getMessage("folder-not-found"));
	}

	public static void copyDirectory(File sourceLocation , File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()){
			if (!targetLocation.exists()){
				targetLocation.mkdir();
			}
			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++){
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		}
		else {
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}
}
