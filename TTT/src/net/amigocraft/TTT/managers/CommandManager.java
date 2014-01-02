package net.amigocraft.TTT.managers;

import static net.amigocraft.TTT.TTTPlayer.*;

import java.io.File;

import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.Variables;
import net.amigocraft.TTT.utils.WorldUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (commandLabel.equalsIgnoreCase("ttt")){
			if (args.length > 0){
				if (args[0].equalsIgnoreCase("import") || args[0].equalsIgnoreCase("i")){
					if (sender.hasPermission("ttt.import")){
						if (args.length > 1){
							WorldUtils.importWorld(sender, args[1]);
						}
						else {
							sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.local.getMessage("invalid-args-1"));
							sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.local.getMessage("usage-import"));
						}						
					}
					else
						sender.sendMessage(ChatColor.RED + TTT.local.getMessage("no-permission-import"));
				}
				else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")){
					if (sender instanceof Player){
						if (sender.hasPermission("ttt.join")){
							if (args.length > 1){
								RoundManager.handleJoin((Player)sender, args[1]);
							}
							else {
								sender.sendMessage(ChatColor.RED + TTT.local.getMessage("invalid-args-1"));
								sender.sendMessage(ChatColor.RED + TTT.local.getMessage("usage-join"));
							}
						}
						else
							sender.sendMessage(ChatColor.RED + TTT.local.getMessage("no-permission-join"));
					}
					else
						sender.sendMessage(ChatColor.RED + TTT.local.getMessage("must-be-ingame"));
				}
				else if (args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase("q")){
					if (sender instanceof Player){
						if (sender.hasPermission("ttt.quit")){
							if (isPlayer(sender.getName())){
								sender.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + sender.getName() + " " +
										TTT.local.getMessage("left-game").replace("%",
												getTTTPlayer(sender.getName()).getWorld()));
								RoundManager.resetPlayer((Player)sender);
							}
							else
								sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.local.getMessage("not-in-game"));
						}
						else
							sender.sendMessage(ChatColor.RED + "[TTT] " +
									TTT.local.getMessage("no-permission-quit"));
					}
					else
						sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.local.getMessage("must-be-ingame"));
				}
				else if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("ss")){
					if (sender.hasPermission("ttt.setspawn")){
						if (sender instanceof Player){
							try {
								File spawnFile = new File(TTT.plugin.getDataFolder() + File.separator + "spawn.yml");
								if (!spawnFile.exists()){
									if (Variables.VERBOSE_LOGGING)
										TTT.log.info("No spawn.yml found, creating...");
									spawnFile.createNewFile();
								}
								YamlConfiguration spawnYaml = new YamlConfiguration();
								spawnYaml.load(spawnFile);
								spawnYaml.set("world", ((Player)sender).getLocation().getWorld().getName());
								spawnYaml.set("x", ((Player)sender).getLocation().getX());
								spawnYaml.set("y", ((Player)sender).getLocation().getY());
								spawnYaml.set("z", ((Player)sender).getLocation().getZ());
								spawnYaml.set("pitch", ((Player)sender).getLocation().getPitch());
								spawnYaml.set("yaw", ((Player)sender).getLocation().getYaw());
								spawnYaml.save(spawnFile);
								sender.sendMessage(ChatColor.DARK_PURPLE + "Successfully set TTT return point!");
							}
							catch (Exception ex){
								ex.printStackTrace();
								sender.sendMessage(ChatColor.RED + "An error occurred while setting the TTT return point.");
							}
						}
						else
							sender.sendMessage(ChatColor.RED + TTT.local.getMessage("must-be-ingame"));
					}
					else
						sender.sendMessage(ChatColor.RED + TTT.local.getMessage("no-permission"));

				}
				else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
					if (args.length > 1 && args[1].equalsIgnoreCase("lobby")){
						if (sender.hasPermission("ttt.lobby.create")){
							sender.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE +
									TTT.local.getMessage("lobby-help"));
							sender.sendMessage("");
							sender.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("first") + " " +
									TTT.local.getMessage("line") + " " +
									ChatColor.GREEN + "[TTT]");
							sender.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("second") + " " +
									TTT.local.getMessage("line") + " " +
									ChatColor.GREEN + TTT.local.getMessage("type"));
							sender.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("third") + " " +
									TTT.local.getMessage("line") + " " +
									ChatColor.GREEN + TTT.local.getMessage("round"));
							sender.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("fourth") + " " +
									TTT.local.getMessage("line") + " " +
									ChatColor.GREEN + TTT.local.getMessage("number"));
						}
						else
							sender.sendMessage(TTT.local.getMessage("no-permission"));
					}
					else if (sender.hasPermission("ttt.help")){
						sender.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE +
								TTT.local.getMessage("commands"));
						sender.sendMessage("");
						if (sender.hasPermission("ttt.join"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt join, j " + ChatColor.GREEN +
									TTT.local.getMessage("join-help"));
						if (sender.hasPermission("ttt.quit"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt quit, q " + ChatColor.GREEN +
									TTT.local.getMessage("quit-help"));
						if (sender.hasPermission("ttt.import"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt import, i " + ChatColor.GREEN +
									TTT.local.getMessage("import-help"));
						if (sender.hasPermission("ttt.setspawn"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt setspawn, ss " + ChatColor.GREEN +
									TTT.local.getMessage("spawn-help"));
						if (sender.hasPermission("ttt.help"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt help, ? " + ChatColor.GREEN +
									TTT.local.getMessage("help-help"));
					}
					else
						sender.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("no-permission"));
				}
				else {
					sender.sendMessage(ChatColor.RED + "[TTT] " + TTT.local.getMessage("invalid-args-2"));
					sender.sendMessage(ChatColor.RED + TTT.local.getMessage("usage-1"));
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + TTT.local.getMessage("invalid-args-1"));
				sender.sendMessage(ChatColor.RED + TTT.local.getMessage("usage-1"));
			}
			return true;
		}
		return false;
	}

}
