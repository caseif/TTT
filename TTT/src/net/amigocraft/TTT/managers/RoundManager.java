package net.amigocraft.TTT.managers;

import static net.amigocraft.TTT.TTTPlayer.getTTTPlayer;
import static net.amigocraft.TTT.TTTPlayer.isPlayer;
import static net.amigocraft.TTT.TTTPlayer.players;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.amigocraft.TTT.Body;
import net.amigocraft.TTT.Role;
import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;
import net.amigocraft.TTT.utils.NumUtils;
import net.amigocraft.TTT.utils.WorldUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RoundManager {

	private TTT plugin = TTT.plugin;
	
	private static HashMap<String, Integer> tasks = new HashMap<String, Integer>();
	
	private static List<String> checkPlayers = new ArrayList<String>();

	@SuppressWarnings("deprecation")
	public void gameTimer(final String worldName){
		
		tasks.put(worldName, plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable(){
			
			public void run(){
				
				// verify that all players are still online
				List<TTTPlayer> offlinePlayers = new ArrayList<TTTPlayer>();
				for (TTTPlayer tp : players){
					if (tp.getWorld().equals(worldName)){
						if (Round.getRound(worldName) != null){
							Player p = plugin.getServer().getPlayer(tp.getName());
							if (p != null){
								if (!plugin.getServer().getWorld("TTT_" + worldName).getPlayers().contains(p)){
									if (checkPlayers.contains(tp.getName())){
										if (plugin.getConfig().getBoolean("verbose-logging"))
											plugin.log.info(tp.getName() + " was missing from TTT world for 2 ticks, removing...");
										checkPlayers.remove(tp.getName());
										offlinePlayers.add(tp);
										Bukkit.broadcastMessage("[TTT] " + tp.getName() + " " + plugin.local.getMessage("left-map") + " \"" + worldName + "\"");
									}
									else
										checkPlayers.add(tp.getName());
								}
							}
						}
					}
				}
				for (TTTPlayer tp : offlinePlayers){
					tp.destroy();
				}

				// set compass targets
				for (TTTPlayer p : players){
					if (p.getKiller() != null){
						Player tracker = plugin.getServer().getPlayer(p.getName());
						Player killer = plugin.getServer().getPlayer(p.getKiller());
						if (tracker != null || killer != null)
							if (!offlinePlayers.contains(tracker) && !offlinePlayers.contains(killer))
								tracker.setCompassTarget(killer.getLocation());
					}
				}

				// check if game is over
				boolean iLeft = false;
				boolean tLeft = false;
				for (TTTPlayer tp : players){
					if (tp.getWorld().equals(worldName) && !tp.isDead()){
						if (tp.getRole() == Role.INNOCENT){
							iLeft = true;
						}
						if (tp.getRole() == Role.TRAITOR){
							tLeft = true;
						}
					}
				}
				if (!(tLeft && iLeft)){
					plugin.getServer().getScheduler().cancelTask(tasks.get(worldName));
					tasks.remove(worldName);
					List<Body> removeBodies = new ArrayList<Body>();
					List<Body> removeFoundBodies = new ArrayList<Body>(); 
					for (Body b : plugin.bodies){
						if (b.getPlayer().isDead()){
							if (getTTTPlayer(b.getPlayer().getName()).getWorld().equals(worldName)){
								removeBodies.add(b);
								if (plugin.foundBodies.contains(b))
									removeFoundBodies.add(b);
							}
						}
						else {
							removeBodies.add(b);
							if (plugin.foundBodies.contains(b))
								removeFoundBodies.add(b);
						}
					}

					for (Body b : removeBodies)
						plugin.bodies.remove(b);

					for (Body b : removeFoundBodies)
						plugin.foundBodies.remove(b);

					removeBodies.clear();
					removeFoundBodies.clear();

					if (!tLeft)
						Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[TTT] " + plugin.local.getMessage("innocent-win").replace("%", "\"" + worldName + "\"") + "!");
					else if (!iLeft)
						Bukkit.broadcastMessage(ChatColor.DARK_RED + "[TTT] " + plugin.local.getMessage("traitor-win").replace("%", "\"" + worldName + "\"") + "!");
					for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
						if (isPlayer(p.getName())){
							TTTPlayer tp = getTTTPlayer(p.getName());
							if (tp != null){
								if (tp.isDead()){
									p.setAllowFlight(false);
									for (Player pl : plugin.getServer().getOnlinePlayers()){
										pl.showPlayer(p);
									}
								}
								tp.destroy();
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
						}
						WorldUtils.teleportPlayer(p);
					}
					plugin.getServer().unloadWorld("TTT_" + worldName, false);
					WorldUtils.rollbackWorld(worldName);
					if (Round.getRound(worldName) != null)
						Round.getRound(worldName).destroy();
					else if (plugin.getConfig().getBoolean("verbose-logging"))
						plugin.log.warning("That's odd, the round has already been destroyed...");
				}
				else {
					Round r = Round.getRound(worldName);
					int rTime = r.getTime();
					if (rTime % 60 == 0 && rTime >= 60){
						for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + Integer.toString(rTime / 60) + " " + plugin.local.getMessage("minutes") + " " + plugin.local.getMessage("left"));
						}
						r.tickDown();
					}
					else if (rTime % 10 == 0 && rTime > 10 && rTime < 60){
						for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + Integer.toString(rTime) + " " + plugin.local.getMessage("seconds") + " " + plugin.local.getMessage("left"));
						}
						r.tickDown();
					}
					else if (rTime < 10 && rTime > 0){
						for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + Integer.toString(rTime) + " " + plugin.local.getMessage("seconds") + " " + plugin.local.getMessage("left"));
						}
						r.tickDown();
					}
					else if (rTime <= 0){
						List<Body> removeBodies = new ArrayList<Body>();
						List<Body> removeFoundBodies = new ArrayList<Body>(); 
						for (Body b : plugin.bodies){
							if (getTTTPlayer(b.getPlayer().getName()).isDead()){
								if (getTTTPlayer(b.getPlayer().getName()).getWorld().equals(worldName)){
									removeBodies.add(b);
									if (plugin.foundBodies.contains(b))
										removeFoundBodies.add(b);
								}
							}
						}

						for (Body b : removeBodies)
							plugin.bodies.remove(b);

						for (Body b : removeFoundBodies)
							plugin.foundBodies.remove(b);

						removeBodies.clear();
						removeFoundBodies.clear();

						for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_GREEN + "[TTT] " + plugin.local.getMessage("innocent-win").replace("%", "\"" + worldName + "\"") + "!");
							if (getTTTPlayer(p.getName()).isDead()){
								p.setAllowFlight(false);
								for (Player pl : plugin.getServer().getOnlinePlayers()){
									pl.showPlayer(p);
								}
							}
							getTTTPlayer(p.getName()).destroy();
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
									}
									p.getInventory().setContents(invI);
									if (invY.getItemStack("h") != null)
										p.getInventory().setHelmet(invY.getItemStack("h"));
									if (invY.getItemStack("c") != null)
										p.getInventory().setChestplate(invY.getItemStack("c"));
									if (invY.getItemStack("l") != null)
										p.getInventory().setLeggings(invY.getItemStack("l"));
									if (invY.getItemStack("b") != null)
										p.getInventory().setBoots(invY.getItemStack("b"));
									p.updateInventory();
									invF.delete();
								}
								catch (Exception ex){
									ex.printStackTrace();
									p.sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("inv-load-fail"));
								}
							}
							WorldUtils.teleportPlayer(p);
						}
						r.destroy();
						plugin.getServer().getScheduler().cancelTask(tasks.get(worldName));
						tasks.remove(worldName);
						plugin.getServer().unloadWorld("TTT_" + worldName, false);
						WorldUtils.rollbackWorld(worldName);
						return;
					}
				}
				// hide dead players
				for (TTTPlayer p : players){
					if (p.isDead()){
						if (plugin.getServer().getPlayer(p.getName()) != null){
							if (plugin.getServer().getWorld("TTT_" + worldName) != null){
								if (plugin.getServer().getWorld("TTT_" + worldName).getPlayers().contains(plugin.getServer().getPlayer(p.getName()))){
									plugin.getServer().getPlayer(p.getName()).setAllowFlight(true);
									for (TTTPlayer other : players){
										if (other.getWorld().equals(worldName) && plugin.getServer().getPlayer(other.getName()) != null)
											plugin.getServer().getPlayer(other.getName()).hidePlayer(plugin.getServer().getPlayer(p.getName()));
									}
								}
							}
						}
					}
				}
			}
		}, 0L, 20L).getTaskId());
	}
}
