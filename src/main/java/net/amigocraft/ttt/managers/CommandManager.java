package net.amigocraft.ttt.managers;

import java.io.File;

import net.amigocraft.mglib.MGUtil;
import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.exception.ArenaExistsException;
import net.amigocraft.mglib.exception.ArenaNotExistsException;
import net.amigocraft.mglib.exception.InvalidLocationException;
import net.amigocraft.mglib.exception.PlayerNotPresentException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.mglib.exception.PlayerPresentException;
import net.amigocraft.mglib.exception.RoundFullException;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.Variables;
import net.amigocraft.ttt.utils.FileUtils;
import net.amigocraft.ttt.utils.NumUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (label.equalsIgnoreCase("ttt")){
			if (args.length > 0){
				if (args[0].equalsIgnoreCase("import") || args[0].equalsIgnoreCase("i")){
					if (sender.hasPermission("ttt.import")){
						if (args.length > 1){
							if (new File(Bukkit.getWorldContainer(), args[1]).exists()){
								if (FileUtils.isWorld(args[1])){
									World w = Bukkit.createWorld(new WorldCreator(args[1]));
									if (w != null){
										try {
											Main.mg.createArena(args[1], w.getSpawnLocation());
											sender.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + Main.locale.getMessage("import-success"));
										}
										catch (ArenaExistsException e){
											//TODO: replace this message with something more accurate
											sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("already-imported"));
										}
									}
									else
										sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("cannot-load-world"));
								}
								else
									sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("cannot-load-world"));
							}
							else
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("folder-error"));
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
									r = Main.mg.getRound(args[1]);
									if (r == null)
										r = Main.mg.createRound(args[1]);
									r.addPlayer(sender.getName());
								}
								catch (ArenaNotExistsException ex){
									sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("arena-invalid"));
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
								String arena = mp.getArena();
								try {
									mp.removeFromRound();
								}
								catch (PlayerNotPresentException ex){
									sender.sendMessage(Main.locale.getMessage("not-in-game"));
								}
								catch (PlayerOfflineException ex){}
								sender.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + sender.getName() + " " +
										Main.locale.getMessage("left-game").replace("%", arena));
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
				else if (args[0].equalsIgnoreCase("carena") || args[0].equalsIgnoreCase("ca")){
					if (sender.hasPermission("ttt.arena.create")){
						String w = null;
						int x = 0;
						int y = 0;
						int z = 0;
						if (args.length == 2){ // use sender's location
							if (sender instanceof Player){
								w = ((Player)sender).getWorld().getName();
								x = ((Player)sender).getLocation().getBlockX();
								y = ((Player)sender).getLocation().getBlockY();
								z = ((Player)sender).getLocation().getBlockZ();
							}
							else {
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("must-be-ingame"));
								return true;
							}
						}
						else if (args.length == 6){ // use 3 provided coords and world
							if (NumUtils.isInt(args[2]) && NumUtils.isInt(args[3]) && NumUtils.isInt(args[4]) && FileUtils.isWorld(args[5])){
								x = Integer.parseInt(args[2]);
								y = Integer.parseInt(args[3]);
								z = Integer.parseInt(args[4]);
								w = args[5];
							}
							else {
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
								return true;
							}
						}
						else {
							sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
							return true;
						}
						try {
							Main.mg.createArena(args[1], new Location(Bukkit.createWorld(new WorldCreator(w)), x, y, z));
						}
						catch (ArenaExistsException ex){
							sender.sendMessage("already-imported");
						}
					}
					else
						sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("no-permission"));
				}
				else if (args[0].equalsIgnoreCase("addspawn") || args[0].equalsIgnoreCase("as")){
					if (sender.hasPermission("ttt.arena.addspawn")){
						World w = null;
						int x = 0;
						int y = 0;
						int z = 0;
						if (args.length == 2){ // use sender's location
							if (sender instanceof Player){
								w = ((Player)sender).getWorld();
								x = ((Player)sender).getLocation().getBlockX();
								y = ((Player)sender).getLocation().getBlockY();
								z = ((Player)sender).getLocation().getBlockZ();
							}
							else {
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("must-be-ingame"));
								return true;
							}
						}
						else if (args.length == 5){ // use 3 provided coords
							if (NumUtils.isInt(args[2]) && NumUtils.isInt(args[3]) && NumUtils.isInt(args[4])){
								x = Integer.parseInt(args[2]);
								y = Integer.parseInt(args[3]);
								z = Integer.parseInt(args[4]);
							}
							else {
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
								return true;
							}
						}
						else {
							sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
							return true;
						}
						try {
							if (w == null)
								Main.mg.getArenaFactory(args[1]).addSpawn(x, y, z);
							else
								Main.mg.getArenaFactory(args[1]).addSpawn(new Location(w, x, y, z));
						}
						catch (InvalidLocationException ex){
							sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("same-world"));
						}
						catch (ArenaNotExistsException ex){
							sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("arena-invalid"));
						}
					}
					else
						sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("no-permission"));
				}
				else if (args[0].equalsIgnoreCase("removespawn") || args[0].equalsIgnoreCase("rs")){
					if (sender.hasPermission("ttt.arena.removespawn")){
						int x = 0;
						int y = 0;
						int z = 0;
						int index = Integer.MAX_VALUE;
						if (args.length == 2){ // use sender's location
							if (sender instanceof Player){
								x = ((Player)sender).getLocation().getBlockX();
								y = ((Player)sender).getLocation().getBlockY();
								z = ((Player)sender).getLocation().getBlockZ();
							}
							else {
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("must-be-ingame"));
								return true;
							}
						}
						else if (args.length == 3)
							if (NumUtils.isInt(args[2]))
								index = Integer.parseInt(args[2]);
							else {
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
								return true;
							}
						else if (args.length == 5){ // use 3 provided coords
							if (NumUtils.isInt(args[2]) && NumUtils.isInt(args[3]) && NumUtils.isInt(args[4])){
								x = Integer.parseInt(args[2]);
								y = Integer.parseInt(args[3]);
								z = Integer.parseInt(args[4]);
							}
							else {
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
								return true;
							}
						}
						else {
							sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
							return true;
						}
						if (index != Integer.MAX_VALUE){
							YamlConfiguration yaml = MGUtil.loadArenaYaml("TTT");
							if (yaml.isSet(args[1] + ".spawns")){
								if (yaml.isSet(args[1] + ".spawns." + index)){
									yaml.set(args[1] + ".spawns." + index, null);
									MGUtil.saveArenaYaml("TTT", yaml);
								}
								else
									sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
							}
							else
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("arena-invalid"));
						}
						else {
							YamlConfiguration yaml = MGUtil.loadArenaYaml("TTT");
							if (yaml.isSet(args[1] + ".spawns")){
								ConfigurationSection cs = yaml.getConfigurationSection(args[1] + ".spawns");
								for (String k : cs.getKeys(false)){
									if (cs.getInt(k + ".x") == x && cs.getInt(k + ".y") == y && cs.getInt(k + ".z") == z){
										cs.set(k, null);
										MGUtil.saveArenaYaml("TTT", yaml);
										return true;
									}
								}
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
							}
							else
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("arena-invalid"));
						}
					}
					else
						sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("no-permission"));
				}
				else if (args[0].equalsIgnoreCase("setexit") || args[0].equalsIgnoreCase("se") ||
						args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("ss")){
					if (sender.hasPermission("ttt.setexit")){
						if (sender instanceof Player){
							try {
								File spawnFile = new File(Main.plugin.getDataFolder() + File.separator + "spawn.yml");
								if (!spawnFile.exists()){
									if (Variables.VERBOSE_LOGGING)
										Main.mg.log("No spawn.yml found, creating...", LogLevel.INFO);
									spawnFile.createNewFile();
								}
								YamlConfiguration spawnYaml = new YamlConfiguration();
								spawnYaml.load(spawnFile);
								spawnYaml.set("world", ((Player)sender).getLocation().getWorld().getName());
								spawnYaml.set("x", ((Player)sender).getLocation().getBlockX() + 0.5);
								spawnYaml.set("y", ((Player)sender).getLocation().getBlockY());
								spawnYaml.set("z", ((Player)sender).getLocation().getBlockZ() + 0.5);
								spawnYaml.save(spawnFile);
								Location l = ((Player)sender).getLocation();
								Main.mg.getConfigManager().setDefaultExitLocation(
										new Location(l.getWorld(), l.getBlockX() + 0.5, l.getBlockY(), l.getBlockZ() + 0.5));
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
						if (sender.hasPermission("ttt.arena.join"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt join, j " + ChatColor.GREEN +
									Main.locale.getMessage("join-help"));
						if (sender.hasPermission("ttt.arena.quit"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt quit, q " + ChatColor.GREEN +
									Main.locale.getMessage("quit-help"));
						if (sender.hasPermission("ttt.arena.import"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt import, i " + ChatColor.GREEN +
									Main.locale.getMessage("import-help"));
						if (sender.hasPermission("ttt.arena.create"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt carena, ca " + ChatColor.GREEN +
									Main.locale.getMessage("createarena-help"));
						if (sender.hasPermission("ttt.arena.addspawn"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt addspawn, ad " + ChatColor.GREEN +
									Main.locale.getMessage("addspawn-help"));
						if (sender.hasPermission("ttt.arena.removespawn"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt removespawn, rs " + ChatColor.GREEN +
									Main.locale.getMessage("removespawn-help"));
						if (sender.hasPermission("ttt.setspawn"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt setexit, se " + ChatColor.GREEN +
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
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "This server is running TTT version " + Main.plugin.getDescription().getVersion() +
						" by Maxim Roncacé.");
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type \"/ttt help\" for help.");
			}
			return true;
		}
		return false;
	}

}
