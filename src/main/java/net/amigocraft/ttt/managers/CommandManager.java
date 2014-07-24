package net.amigocraft.ttt.managers;

import java.io.File;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.exception.ArenaNotExistsException;
import net.amigocraft.mglib.exception.PlayerNotPresentException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.mglib.exception.PlayerPresentException;
import net.amigocraft.mglib.exception.RoundFullException;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.Variables;
import net.amigocraft.ttt.utils.WorldUtils;

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
							sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-1"));
							sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("usage-import"));
						}						
					}
					else
						sender.sendMessage(ChatColor.RED + Main.locale.getMessage("no-permission-import"));
				}
				else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")){
					if (sender instanceof Player){
						if (sender.hasPermission("ttt.join")){
							if (args.length > 1){
								Round r;
								try {
									r = Main.mg.createRound(args[1]);
									r.addPlayer(sender.getName());
								}
								catch (ArenaNotExistsException ex){
									sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("map-invalid"));
								}
								catch (PlayerOfflineException ex){ // this should never be able to happen
									ex.printStackTrace();
								}
								catch (PlayerPresentException ex){
									sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("already-entered"));
								}
								catch (RoundFullException ex){
									sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("round-full"));
								}
							}
							else {
								sender.sendMessage(ChatColor.RED + Main.locale.getMessage("invalid-args-1"));
								sender.sendMessage(ChatColor.RED + Main.locale.getMessage("usage-join"));
							}
						}
						else
							sender.sendMessage(ChatColor.RED + Main.locale.getMessage("no-permission-join"));
					}
					else
						sender.sendMessage(ChatColor.RED + Main.locale.getMessage("must-be-ingame"));
				}
				else if (args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase("q")){
					if (sender instanceof Player){
						if (sender.hasPermission("ttt.quit")){
							if (Main.mg.isPlayer(sender.getName())){
								MGPlayer mp = Main.mg.getMGPlayer(sender.getName());
								try {
								mp.removeFromRound();
								}
								catch (PlayerNotPresentException ex){}
								catch (PlayerOfflineException ex){}
								sender.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + sender.getName() + " " +
										Main.locale.getMessage("left-game").replace("%",
												Main.mg.getMGPlayer(sender.getName()).getArena()));
							}
							else
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("not-in-game"));
						}
						else
							sender.sendMessage(ChatColor.RED + "[TTT] " +
									Main.locale.getMessage("no-permission-quit"));
					}
					else
						sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("must-be-ingame"));
				}
				else if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("ss")){
					if (sender.hasPermission("ttt.setspawn")){
						if (sender instanceof Player){
							try {
								File spawnFile = new File(Main.plugin.getDataFolder() + File.separator + "spawn.yml");
								if (!spawnFile.exists()){
									if (Variables.VERBOSE_LOGGING)
										Main.log.info("No spawn.yml found, creating...");
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
							sender.sendMessage(ChatColor.RED + Main.locale.getMessage("must-be-ingame"));
					}
					else
						sender.sendMessage(ChatColor.RED + Main.locale.getMessage("no-permission"));

				}
				else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
					if (args.length > 1 && args[1].equalsIgnoreCase("lobby")){
						if (sender.hasPermission("ttt.lobby.create")){
							sender.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE +
									Main.locale.getMessage("lobby-help"));
							sender.sendMessage("");
							sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("first") + " " +
									Main.locale.getMessage("line") + " " +
									ChatColor.GREEN + "[TTT]");
							sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("second") + " " +
									Main.locale.getMessage("line") + " " +
									ChatColor.GREEN + Main.locale.getMessage("type"));
							sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("third") + " " +
									Main.locale.getMessage("line") + " " +
									ChatColor.GREEN + Main.locale.getMessage("round"));
							sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("fourth") + " " +
									Main.locale.getMessage("line") + " " +
									ChatColor.GREEN + Main.locale.getMessage("number"));
						}
						else
							sender.sendMessage(Main.locale.getMessage("no-permission"));
					}
					else if (sender.hasPermission("ttt.help")){
						sender.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE +
								Main.locale.getMessage("commands"));
						sender.sendMessage("");
						if (sender.hasPermission("ttt.join"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt join, j " + ChatColor.GREEN +
									Main.locale.getMessage("join-help"));
						if (sender.hasPermission("ttt.quit"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt quit, q " + ChatColor.GREEN +
									Main.locale.getMessage("quit-help"));
						if (sender.hasPermission("ttt.import"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt import, i " + ChatColor.GREEN +
									Main.locale.getMessage("import-help"));
						if (sender.hasPermission("ttt.setspawn"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt setspawn, ss " + ChatColor.GREEN +
									Main.locale.getMessage("spawn-help"));
						if (sender.hasPermission("ttt.help"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt help, ? " + ChatColor.GREEN +
									Main.locale.getMessage("help-help"));
					}
					else
						sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("no-permission"));
				}
				else {
					sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
					sender.sendMessage(ChatColor.RED + Main.locale.getMessage("usage-1"));
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + Main.locale.getMessage("invalid-args-1"));
				sender.sendMessage(ChatColor.RED + Main.locale.getMessage("usage-1"));
			}
			return true;
		}
		return false;
	}

}
