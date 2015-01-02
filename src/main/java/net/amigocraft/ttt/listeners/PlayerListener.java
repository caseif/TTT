/*
 * TTT
 * Copyright (c) 2013-2015, Maxim Roncacé <mproncace@lapis.blue>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.amigocraft.ttt.listeners;

import net.amigocraft.mglib.api.Location3D;
import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.api.Stage;
import net.amigocraft.ttt.Body;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.Config;
import net.amigocraft.ttt.managers.KarmaManager;
import net.amigocraft.ttt.managers.ScoreManager;
import net.amigocraft.ttt.util.InventoryUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

public class PlayerListener implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e){
		if (Main.mg.isPlayer(e.getPlayer().getName())){ // check if player is in TTT round
			MGPlayer player = Main.mg.getMGPlayer(e.getPlayer().getName());
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK){
				// disallow cheating/bed setting
				if (e.getClickedBlock().getType() == Material.ENDER_CHEST || e.getClickedBlock().getType() == Material.BED_BLOCK){
					e.setCancelled(true);
					return;
				}
				// handle body checking
				if (e.getClickedBlock().getType() == Material.CHEST){
					if (player.isSpectating()){
						e.setCancelled(true);
						for (Body b : Main.bodies){
							if (b.getLocation().equals(Location3D.valueOf(e.getClickedBlock().getLocation()))){
								Inventory chestInv = ((Chest) e.getClickedBlock().getState()).getInventory();
								Inventory inv = Main.plugin.getServer().createInventory(chestInv.getHolder(), chestInv.getSize());
								inv.setContents(chestInv.getContents());
								e.getPlayer().openInventory(inv);
								break;
							}
						}
					}
					else {
						int index = -1;
						for (int i = 0; i < Main.bodies.size(); i++){
							if (Main.bodies.get(i).getLocation().equals(Location3D.valueOf(e.getClickedBlock().getLocation()))){
								index = i;
								break;
							}
						}
						if (index != -1){
							boolean found = false;
							for (Body b : Main.foundBodies){
								if (b.getLocation().equals(Location3D.valueOf(e.getClickedBlock().getLocation()))){
									found = true;
									break;
								}
							}
							if (!found){ // it's a new body
								Body b = Main.bodies.get(index);
								MGPlayer bodyPlayer = Main.mg.getMGPlayer(Main.bodies.get(index).getPlayer());
								if (b.getTeam().equals("Innocent")){
									Main.mg.getRound(b.getArena()).broadcast(ChatColor.DARK_GREEN + e.getPlayer().getName() + " " +
											Main.locale.getMessage("found-body").replace("%", b.getPlayer()) + ". " +
											Main.locale.getMessage("was-innocent"));
								}
								else if (b.getTeam().equals("Traitor")){
									Main.mg.getRound(b.getArena()).broadcast(ChatColor.DARK_RED + e.getPlayer().getName() + " " +
											Main.locale.getMessage("found-body").replace("%", b.getPlayer()) + ". " +
											Main.locale.getMessage("was-traitor"));
								}
								else {
									Main.mg.getRound(b.getArena()).broadcast(ChatColor.BLUE + e.getPlayer().getName() + " " +
											Main.locale.getMessage("found-body").replace("%", b.getPlayer()) + ". " +
											Main.locale.getMessage("was-detective"));
								}
								Main.foundBodies.add(Main.bodies.get(index));
								if (bodyPlayer != null && bodyPlayer.getArena().equals(Main.bodies.get(index).getArena())){
									bodyPlayer.setPrefix("§m");
									bodyPlayer.setMetadata("bodyFound", true);
									if (ScoreManager.sbManagers.containsKey(bodyPlayer.getArena()))
										ScoreManager.sbManagers.get(bodyPlayer.getArena()).update(bodyPlayer);
									else
										for (String s : ScoreManager.sbManagers.keySet())
											System.out.println(bodyPlayer.getArena() + ", " + s);
								}
							}
							if (player.hasMetadata("detective")){ // handle DNA scanning
								if (e.getPlayer().getItemInHand() != null &&
										e.getPlayer().getItemInHand().getType() == Material.COMPASS &&
										e.getPlayer().getItemInHand().getItemMeta() != null &&
										e.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null &&
										e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(
												"§1" + Main.locale.getMessage("dna-scanner"))
										){
									e.setCancelled(true);
									Player killer = Main.plugin.getServer().getPlayer((String)Main.mg.getMGPlayer(Main.bodies.get(index).getPlayer()).getMetadata("killer"));
									if (killer != null){
										if (Main.mg.isPlayer(killer.getName())){
											if (!Main.mg.getMGPlayer(killer.getName()).isSpectating()){
												player.setMetadata("tracking", killer.getName());
											}
											e.getPlayer().sendMessage(ChatColor.BLUE + Main.locale.getMessage("collected-sample")
													.replace("%", Main.bodies.get(index).getPlayer()));
										}
										else {
											e.getPlayer().sendMessage(ChatColor.BLUE + Main.locale.getMessage("killer-left"));
										}
									}
									else {
										e.getPlayer().sendMessage(ChatColor.BLUE + Main.locale.getMessage("killer-left"));
									}
									return;
								}
							}
						}
					}
				}
			}
		}
		// guns
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
			if (e.getPlayer().getItemInHand() != null){
				if (e.getPlayer().getItemInHand().getItemMeta() != null){
					if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null){
						if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().endsWith(Main.locale.getMessage("gun"))){
							if ((Main.mg.isPlayer(e.getPlayer().getName()) &&
									!Main.mg.getMGPlayer(e.getPlayer().getName()).isSpectating() &&
									(Main.mg.getMGPlayer(e.getPlayer().getName()).getRound().getStage() == Stage.PLAYING) ||
									Config.GUNS_OUTSIDE_ARENAS)){
								e.setCancelled(true);
								if (e.getPlayer().getInventory().contains(Material.ARROW) || !Config.REQUIRE_AMMO_FOR_GUNS){
									if (Config.REQUIRE_AMMO_FOR_GUNS){
										InventoryUtil.removeArrow(e.getPlayer().getInventory());
										e.getPlayer().updateInventory();
									}
									e.getPlayer().launchProjectile(Arrow.class);
								}
								else {
									e.getPlayer().sendMessage(ChatColor.RED + Main.locale.getMessage("need-ammo"));
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
		if (e.getMessage().startsWith("kit")){
			if (Main.mg.isPlayer(e.getPlayer().getName())){
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("no-kits"));
			}
		}
		else if (e.getMessage().startsWith("msg") ||
				e.getMessage().startsWith("tell") ||
				e.getMessage().startsWith("r") ||
				e.getMessage().startsWith("msg") ||
				e.getMessage().startsWith("me")){
			String p = e.getPlayer().getName();
			if (Main.mg.isPlayer(p)){
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("no-pm"));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent e){
		if (e.getEntityType() == EntityType.PLAYER){
			MGPlayer victim = Main.mg.getMGPlayer(((Player)e.getEntity()).getName());
			if (victim != null && victim.getRound().getStage() != Stage.PLAYING){
				if (e.getCause() == DamageCause.VOID){
					victim.spawnIn();
				}
				else {
					e.setCancelled(true);
				}
			}
			if (e instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent) e;
				if (ed.getDamager().getType() == EntityType.PLAYER ||
						(ed.getDamager() instanceof Projectile && ((Projectile) ed.getDamager()).getShooter() instanceof Player)){
					Player damager = ed.getDamager().getType() == EntityType.PLAYER ?
					                 (Player) ed.getDamager() :
					                 (Player) ((Projectile) ed.getDamager()).getShooter();
					if (Main.mg.isPlayer(damager.getName())){
						MGPlayer mgDamager = Main.mg.getMGPlayer(damager.getName());
						if (mgDamager.getRound().getStage() != Stage.PLAYING){
							e.setCancelled(true);
							return;
						}
						else if (victim == null){
							e.setCancelled(true);
							return;
						}
						if (damager.getItemInHand() != null){
							if (damager.getItemInHand().getItemMeta() != null){
								if (damager.getItemInHand().getItemMeta().getDisplayName() != null){
									if (damager.getItemInHand().getItemMeta().getDisplayName().endsWith(Main.locale.getMessage("crowbar"))){
										e.setDamage(Config.CROWBAR_DAMAGE);
									}
								}
							}
						}
						e.setDamage((int) (e.getDamage() * (Double)mgDamager.getMetadata("damageRed")));
						KarmaManager.handleDamageKarma(mgDamager, victim, e.getDamage());
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e){
		if (Main.mg.isPlayer(e.getPlayer().getName())){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e){
		if (Main.mg.isPlayer(e.getPlayer().getName())){
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("no-drop"));
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if (Config.KARMA_PERSISTENCE){
			KarmaManager.loadKarma(e.getPlayer().getName());
		}
		if (e.getPlayer().hasPermission("ttt.build.warn")){
			if (Main.stability.equals("unstable")){
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "[TTT] " + Main.locale.getMessage("unstable-build"));
			}
			else if (Main.stability.equals("unknown")){
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "[TTT] " + Main.locale.getMessage("unknown-build"));
			}
			else if (Main.stability.equals("pre")){
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "[TTT] " + Main.locale.getMessage("prerelease"));
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		if (!Config.KARMA_PERSISTENCE){
			KarmaManager.playerKarma.remove(e.getPlayer().getName());
		}
	}

	@EventHandler
	public void onHealthRegenerate(EntityRegainHealthEvent e){
		if (e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();
			if (Main.mg.isPlayer(p.getName())){
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent e){
		if (Main.mg.isPlayer(e.getPlayer().getName())){
			MGPlayer mp = Main.mg.getMGPlayer(e.getPlayer().getName());
			if (mp.hasMetadata("Detective")){
				final Player player = e.getPlayer();
				e.getPlayer().setDisplayName(ChatColor.DARK_BLUE + "[Detective] " + ChatColor.DARK_BLUE + e.getPlayer().getDisplayName());
				Main.plugin.getServer().getScheduler().runTask(Main.plugin, new Runnable() {
					public void run(){
						String name = player.getDisplayName();
						name = name.replace(ChatColor.DARK_BLUE + "[Detective] " + ChatColor.DARK_BLUE, "");
						player.setDisplayName(name);
					}
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent e){
		for (HumanEntity he : e.getViewers()){
			Player p = (Player) he;
			if (Main.mg.isPlayer(p.getName())){
				if (e.getInventory().getType() == InventoryType.CHEST){
					Block block = null;
					Block block2 = null;
					if (e.getInventory().getHolder() instanceof Chest){
						block = ((Chest) e.getInventory().getHolder()).getBlock();
					}
					else if (e.getInventory().getHolder() instanceof DoubleChest){
						block = ((Chest) ((DoubleChest) e.getInventory().getHolder()).getLeftSide()).getBlock();
						block2 = ((Chest) ((DoubleChest) e.getInventory().getHolder()).getRightSide()).getBlock();
					}
					else {
						return;
					}
					boolean found1 = false;
					boolean found2 = false;
					for (Body b : Main.bodies){
						if (b.getLocation().equals(Location3D.valueOf(block.getLocation()))){
							found1 = true;
							e.setCancelled(true);
							if (block2 == null){
								break;
							}
						}
						if (block2 != null){
							if (b.getLocation().equals(Location3D.valueOf(block.getLocation()))){
								e.setCancelled(true);
								if (!found1){
									break;
								}
							}
						}
					}
				}
			}
		}
	}

}
