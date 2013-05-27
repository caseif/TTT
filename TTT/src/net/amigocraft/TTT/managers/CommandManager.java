package net.amigocraft.TTT.managers;

import static net.amigocraft.TTT.TTTPlayer.getTTTPlayer;
import static net.amigocraft.TTT.TTTPlayer.isPlayer;
import static net.amigocraft.TTT.TTTPlayer.players;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.Stage;
import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;
import net.amigocraft.TTT.utils.NumUtils;
import net.amigocraft.TTT.utils.WorldUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CommandManager implements CommandExecutor {

	private TTT plugin = TTT.plugin;

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (commandLabel.equalsIgnoreCase("ttt")){
			if (args.length > 0){
				if (args[0].equalsIgnoreCase("import") || args[0].equalsIgnoreCase("i")){
					if (sender.hasPermission("ttt.import")){
						if (args.length > 1){
							WorldUtils.importWorld(sender, args[1]);
						}
						else {
							sender.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("invalid-args-1"));
							sender.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("usage-import"));
						}						
					}
					else
						sender.sendMessage(ChatColor.RED + plugin.local.getMessage("no-permission-import"));
				}
				else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")){
					if (sender instanceof Player){
						if (sender.hasPermission("ttt.join")){
							if (args.length > 1){
								boolean valid = false;
								if (Round.getRound(args[1]) == null)
									valid = true;
								else if (Round.getRound(args[1]).getStage() != Stage.PLAYING)
									valid = true;
								if (valid){
									File folder = new File(args[1]);
									File tttFolder = new File("TTT_" + args[1]);
									if (folder.exists() && tttFolder.exists()){
										boolean loaded = false;
										for (World w : Bukkit.getServer().getWorlds()){
											if(w.getName().equals("TTT_" + args[1])){
												loaded = true;
												break;
											}
										}
										final String worldName = args[1];
										for (Round r : Round.rounds){
											plugin.log.info(r.getWorld());
										}
										Round r = Round.getRound(worldName);
										if (r == null){
											r = new Round(worldName);
										}
										if (!loaded){
											plugin.getServer().createWorld(new WorldCreator("TTT_" + worldName));
										}
										((Player)sender).teleport(plugin.getServer().getWorld("TTT_" + worldName).getSpawnLocation());
										new TTTPlayer(((Player)sender).getName(), worldName);
										File invF = new File(plugin.getDataFolder() + File.separator + "inventories" + File.separator + sender.getName() + ".inv");
										Inventory inv = ((Player)sender).getInventory();
										PlayerInventory pInv = (PlayerInventory)inv;
										try {
											if (!invF.exists())
												invF.createNewFile();
											YamlConfiguration invY = new YamlConfiguration();
											invY.load(invF);
											for (int i = 0; i < inv.getContents().length; i++)
												invY.set(Integer.toString(i), inv.getContents()[i]);
											if (pInv.getHelmet() != null)
												invY.set("h", pInv.getHelmet());
											if (pInv.getChestplate() != null)
												invY.set("c", pInv.getChestplate());
											if (pInv.getLeggings() != null)
												invY.set("l", pInv.getLeggings());
											if (pInv.getBoots() != null)
												invY.set("b", pInv.getBoots());
											invY.save(invF);
										}
										catch (Exception ex){
											ex.printStackTrace();
											sender.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("inv-save-error"));
										}
										inv.clear();
										pInv.setArmorContents(new ItemStack[]{null, null, null, null});
										sender.sendMessage(ChatColor.GREEN + plugin.local.getMessage("success-join") + " " + worldName);
										List<String> testers = new ArrayList<String>();
										testers.add("ZerosAce00000");
										testers.add("momhipie");
										testers.add("xJHA929x");
										testers.add("jmm1999");
										testers.add("jon674");
										testers.add("HardcoreBukkit");
										testers.add("shiny3");
										testers.add("jpf6368");
										String addition = "";
										if (sender.getName().equals("AngryNerd1"))
											addition = ", " + ChatColor.DARK_RED + plugin.local.getMessage("creator") + "," + ChatColor.DARK_PURPLE;
										else if (testers.contains(sender.getName())){
											addition = ", " + ChatColor.DARK_RED + plugin.local.getMessage("tester") + "," + ChatColor.DARK_PURPLE;
										}
										Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[TTT] " + sender.getName() + addition + " " + plugin.local.getMessage("joined-map") + " \"" + worldName + "\"");
										int ingamePlayers = 0;
										for (TTTPlayer p : players)
											if (p.getWorld().equals(worldName))
												ingamePlayers += 1;
										if (ingamePlayers >= plugin.getConfig().getInt("minimum-players") && r.getStage() != Stage.PREPARING){
											for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers())
												p.sendMessage(ChatColor.DARK_PURPLE + plugin.local.getMessage("round-starting"));
											r.setTime(plugin.getConfig().getInt("setup-time"));
											r.setStage(Stage.PREPARING);
											SetupManager.setupTimer(worldName);
										}
										else {
											((Player)sender).sendMessage(ChatColor.DARK_PURPLE + plugin.local.getMessage("waiting"));
										}
									}
									else
										sender.sendMessage(ChatColor.RED + plugin.local.getMessage("map-invalid"));
									folder = null;
									tttFolder = null;
								}
								else
									sender.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("in-progress"));
							}
							else {
								sender.sendMessage(ChatColor.RED + plugin.local.getMessage("invalid-args-1"));
								sender.sendMessage(ChatColor.RED + plugin.local.getMessage("usage-join"));
							}
						}
						else
							sender.sendMessage(ChatColor.RED + plugin.local.getMessage("no-permission-join"));
					}
					else
						sender.sendMessage(ChatColor.RED + plugin.local.getMessage("must-be-ingame"));
				}
				else if (args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase("q")){
					if (sender instanceof Player){
						if (sender.hasPermission("ttt.quit")){
							if (isPlayer(sender.getName())){
								WorldUtils.teleportPlayer((Player)sender);
								TTTPlayer tPlayer = getTTTPlayer(sender.getName());
								getTTTPlayer(sender.getName()).destroy();
								if (plugin.getServer().getWorld("TTT_" + tPlayer.getWorld()) != null)
									for (Player pl : plugin.getServer().getWorld("TTT_" + tPlayer.getWorld()).getPlayers())
										pl.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + ((Player)sender).getName() + " " + plugin.local.getMessage("left-game").replace("%", tPlayer.getWorld()));
								Player p = (Player)sender;
								p.getInventory().clear();
								File invF = new File(plugin.getDataFolder() + File.separator + "inventories" + File.separator + p.getName() + ".inv");
								if (invF.exists()){
									try {
										YamlConfiguration invY = new YamlConfiguration();
										invY.load(invF);
										ItemStack[] invI = new ItemStack[p.getInventory().getSize()];
										for (String k : invY.getKeys(false)){
											if (NumUtils.isInt(k))
												invI[Integer.parseInt(k)] = invY.getItemStack(k);
											else if (k.equalsIgnoreCase("h"))
												p.getInventory().setHelmet(invY.getItemStack(k));
											else if (k.equalsIgnoreCase("c"))
												p.getInventory().setChestplate(invY.getItemStack(k));
											else if (k.equalsIgnoreCase("l"))
												p.getInventory().setLeggings(invY.getItemStack(k));
											else if (k.equalsIgnoreCase("b"))
												p.getInventory().setBoots(invY.getItemStack(k));
										}
										p.getInventory().setContents(invI);
										p.updateInventory();
										invF.delete();
									}
									catch (Exception ex){
										ex.printStackTrace();
										p.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("inv-load-error"));
									}
								}
							}
							else
								sender.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("not-in-game"));
						}
						else
							sender.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("no-permission-quit"));
					}
					else
						sender.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("must-be-ingame"));
				}
				else if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("ss")){
					if (sender.hasPermission("ttt.setspawn")){
						if (sender instanceof Player){
							try {
								File spawnFile = new File(plugin.getDataFolder() + File.separator + "spawn.yml");
								if (!spawnFile.exists()){
									if (plugin.getConfig().getBoolean("verbose-logging"))
										plugin.log.info("No spawn.yml found, creating...");
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
							}
							catch (Exception ex){
								ex.printStackTrace();
							}
						}
						else
							sender.sendMessage(ChatColor.RED + plugin.local.getMessage("must-be-ingame"));
					}
					else
						sender.sendMessage(ChatColor.RED + plugin.local.getMessage("no-permission"));

				}
				else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
					if (sender.hasPermission("ttt.help")){
						sender.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + plugin.local.getMessage("commands"));
						sender.sendMessage("");
						if (sender.hasPermission("ttt.join"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt join, j " + ChatColor.GREEN + plugin.local.getMessage("join-help"));
						if (sender.hasPermission("ttt.quit"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt quit, q " + ChatColor.GREEN + plugin.local.getMessage("quit-help"));
						if (sender.hasPermission("ttt.import"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt import, i " + ChatColor.GREEN + plugin.local.getMessage("import-help"));
						if (sender.hasPermission("ttt.setspawn"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt setspawn, ss " + ChatColor.GREEN + plugin.local.getMessage("spawn-help"));
						if (sender.hasPermission("ttt.help"))
							sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt help, ? " + ChatColor.GREEN + plugin.local.getMessage("help-help"));
					}
					else
						sender.sendMessage(ChatColor.DARK_PURPLE + plugin.local.getMessage("no-permission"));
				}
				else {
					sender.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("invalid-args-2"));
					sender.sendMessage(ChatColor.RED + plugin.local.getMessage("usage-1"));
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + plugin.local.getMessage("invalid-args-1"));
				sender.sendMessage(ChatColor.RED + plugin.local.getMessage("usage-1"));
			}
			return true;
		}
		return false;
	}

}
