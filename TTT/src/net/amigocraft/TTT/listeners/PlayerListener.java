package net.amigocraft.TTT.listeners;

import static net.amigocraft.TTT.TTTPlayer.destroy;
import static net.amigocraft.TTT.TTTPlayer.getTTTPlayer;
import static net.amigocraft.TTT.TTTPlayer.isPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.amigocraft.TTT.Body;
import net.amigocraft.TTT.FixedLocation;
import net.amigocraft.TTT.Role;
import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;
import net.amigocraft.TTT.utils.InventoryUtils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerListener implements Listener {

	private TTT plugin = TTT.plugin;

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent e){
		if (isPlayer(e.getPlayer().getName())){
			TTTPlayer tPlayer = getTTTPlayer(e.getPlayer().getName());
			if (!tPlayer.isDead()){
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK){
					if (e.getClickedBlock().getType() == Material.CHEST){
						int index = -1;
						for (int i = 0; i < plugin.bodies.size(); i++){
							if (plugin.bodies.get(i).getLocation().equals(FixedLocation.getFixedLocation(e.getClickedBlock()))){
								index = i;
								break;
							}
						}
						if (index != -1){
							boolean found = false;
							for (Body b : plugin.foundBodies){
								if (b.getLocation().equals(FixedLocation.getFixedLocation(e.getClickedBlock()))){
									found = true;
									break;
								}
							}
							if (!found){
								for (Player p : e.getPlayer().getWorld().getPlayers()){
									if (plugin.bodies.get(index).getRole() == Role.INNOCENT)
										p.sendMessage(ChatColor.DARK_GREEN + e.getPlayer().getName() + " " +
												plugin.local.getMessage("found-body").replace("%", plugin.bodies.get(index).getName())  + ". " + plugin.local.getMessage("was-innocent"));
									else if (plugin.bodies.get(index).getRole() == Role.TRAITOR)
										p.sendMessage(ChatColor.DARK_RED + e.getPlayer().getName() + " " +
												plugin.local.getMessage("found-body").replace("%", plugin.bodies.get(index).getName())  + ". " + plugin.local.getMessage("was-traitor"));
									else if (plugin.bodies.get(index).getRole() == Role.DETECTIVE)
										p.sendMessage(ChatColor.DARK_BLUE + e.getPlayer().getName() + " " +
												plugin.local.getMessage("found-body").replace("%", plugin.bodies.get(index).getName())  + ". " + plugin.local.getMessage("was-detective"));
								}
								plugin.foundBodies.add(plugin.bodies.get(index));
							}
							if (tPlayer.getRole() == Role.DETECTIVE){
								if (e.getPlayer().getItemInHand() != null){
									if (e.getPlayer().getItemInHand().getType() == Material.COMPASS){
										if (e.getPlayer().getItemInHand().getItemMeta() != null){
											if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null){
												if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals("§1" + plugin.local.getMessage("dna-scanner"))){
													e.setCancelled(true);
													Player killer = plugin.getServer().getPlayer(getTTTPlayer(plugin.bodies.get(index).getName()).getKiller());
													if (killer != null){
														if (isPlayer(killer.getName())){
															tPlayer.setTracking(killer.getName());
															e.getPlayer().sendMessage(ChatColor.BLUE + plugin.local.getMessage("collected-dna").replace("%", plugin.bodies.get(index).getName()));
														}
														else
															e.getPlayer().sendMessage(ChatColor.BLUE + plugin.local.getMessage("killer-left"));
													}
													else
														e.getPlayer().sendMessage(ChatColor.BLUE + plugin.local.getMessage("killer-left"));
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
					if (e.getPlayer().getItemInHand() != null){
						if (e.getPlayer().getItemInHand().getItemMeta() != null){
							if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null){
								if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals("§5" + plugin.local.getMessage("gun"))){
									if ((plugin.getConfig().getBoolean("guns-outside-arenas"))){
										if (tPlayer.isDead()){
											e.setCancelled(true);
											if (e.getPlayer().getInventory().contains(Material.ARROW) || !plugin.getConfig().getBoolean("require-ammo-for-guns")){
												if (plugin.getConfig().getBoolean("require-ammo-for-guns")){
													InventoryUtils.removeArrow(e.getPlayer().getInventory());
													e.getPlayer().updateInventory();
												}
												e.getPlayer().launchProjectile(Arrow.class);
											}
											else
												e.getPlayer().sendMessage(ChatColor.RED + plugin.local.getMessage("need-ammo"));
										}
									}
								}
							}
						}
					}
				}
				else {
					e.setCancelled(true);
					if (tPlayer.isDead()){
						if (e.getClickedBlock() != null){
							for (Body b : plugin.bodies){
								if (b.getLocation().equals(FixedLocation.getFixedLocation(e.getClickedBlock()))){
									if (e.getClickedBlock().getType() == Material.CHEST){
										Inventory chestinv = ((Chest)e.getClickedBlock().getState()).getInventory();
										Inventory inv = plugin.getServer().createInventory(null, chestinv.getSize());
										inv.setContents(chestinv.getContents());
										e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + plugin.local.getMessage("discreet"));
										plugin.discreet.add(e.getPlayer().getName());
										e.getPlayer().openInventory(inv);
									}
									break;
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
			if (isPlayer(e.getPlayer().getName())){
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("no-kits"));
			}
		}
		else if (e.getMessage().startsWith("msg") || e.getMessage().startsWith("tell") || e.getMessage().startsWith("r") || e.getMessage().startsWith("msg") || e.getMessage().startsWith("me")){
			String p = e.getPlayer().getName();
			if (isPlayer(p)){
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("no-pm"));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e){
		if (e.getEntityType() == EntityType.PLAYER){
			if (e instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent)e;
				if (ed.getDamager().getType() == EntityType.PLAYER){
					if (isPlayer(((Player)ed.getDamager()).getName())){
						if (getTTTPlayer(((Player)ed.getDamager()).getName()).isDead()){
							e.setCancelled(true);
						}
					}

					if (isPlayer(((Player)ed.getDamager()).getName())){
						if (plugin.gameTime.get(((Player)ed.getDamager()).getWorld().getName()) == null)
							e.setCancelled(true);
					}
					if (((Player)ed.getDamager()).getItemInHand() != null)
						if (((Player)ed.getDamager()).getItemInHand().getItemMeta() != null)
							if (((Player)ed.getDamager()).getItemInHand().getItemMeta().getDisplayName() != null)
								if (((Player)ed.getDamager()).getItemInHand().getItemMeta().getDisplayName().equals("§5" + plugin.local.getMessage("crowbar")))
									e.setDamage(plugin.getConfig().getInt("crowbar-damage"));
				}
			}
			Player p = (Player)e.getEntity();
			if (isPlayer(p.getName())){
				int armor = 0;
				if (e.getCause() == DamageCause.ENTITY_ATTACK ||
						e.getCause() == DamageCause.PROJECTILE ||
						e.getCause() == DamageCause.FIRE ||
						e.getCause() == DamageCause.FIRE_TICK ||
						e.getCause() == DamageCause.BLOCK_EXPLOSION || 
						e.getCause() == DamageCause.CONTACT ||
						e.getCause() == DamageCause.LAVA ||
						e.getCause() == DamageCause.ENTITY_EXPLOSION){
					HashMap<Material, Integer> protection = new HashMap<Material, Integer>();
					protection.put(Material.LEATHER_HELMET, 1);
					protection.put(Material.LEATHER_CHESTPLATE, 3);
					protection.put(Material.LEATHER_LEGGINGS, 2);
					protection.put(Material.LEATHER_BOOTS, 1);
					protection.put(Material.IRON_HELMET, 2);
					protection.put(Material.IRON_CHESTPLATE, 5);
					protection.put(Material.IRON_LEGGINGS, 3);
					protection.put(Material.IRON_BOOTS, 1);
					protection.put(Material.CHAINMAIL_HELMET, 2);
					protection.put(Material.CHAINMAIL_CHESTPLATE, 5);
					protection.put(Material.CHAINMAIL_LEGGINGS, 3);
					protection.put(Material.CHAINMAIL_BOOTS, 1);
					protection.put(Material.GOLD_HELMET, 2);
					protection.put(Material.GOLD_CHESTPLATE, 6);
					protection.put(Material.GOLD_LEGGINGS, 5);
					protection.put(Material.GOLD_BOOTS, 2);
					protection.put(Material.DIAMOND_HELMET, 3);
					protection.put(Material.DIAMOND_CHESTPLATE, 8);
					protection.put(Material.DIAMOND_LEGGINGS, 6);
					protection.put(Material.DIAMOND_BOOTS, 3);
					if (p.getInventory().getArmorContents()[0] != null)
						if (protection.containsKey(p.getInventory().getArmorContents()[0].getType()))
							armor += protection.get(p.getInventory().getArmorContents()[0].getType());
					if (p.getInventory().getArmorContents()[1] != null)
						if (protection.containsKey(p.getInventory().getArmorContents()[1].getType()))
							armor += protection.get(p.getInventory().getArmorContents()[1].getType());
					if (p.getInventory().getArmorContents()[2] != null)
						if (protection.containsKey(p.getInventory().getArmorContents()[2].getType()))
							armor += protection.get(p.getInventory().getArmorContents()[2].getType());
					if (p.getInventory().getArmorContents()[3] != null)
						if (protection.containsKey(p.getInventory().getArmorContents()[3].getType()))
							armor += protection.get(p.getInventory().getArmorContents()[3].getType());
				}
				if (e.getDamage() - ((armor * .04) * e.getDamage()) >= ((Player)e.getEntity()).getHealth()){
					if (getTTTPlayer(p.getName()).getRole() != null){
						e.setCancelled(true);
						p.setHealth(20);
						p.sendMessage(ChatColor.DARK_PURPLE + plugin.local.getMessage("dead"));
						getTTTPlayer(p.getName()).setDead(true);
						Block block = p.getLocation().getBlock();
						block.setType(Material.CHEST);
						Chest chest = (Chest)block.getState();
						// player identifier
						ItemStack id = new ItemStack(Material.PAPER, 1);
						ItemMeta idMeta = id.getItemMeta();
						idMeta.setDisplayName(plugin.local.getMessage("id"));
						List<String> idLore = new ArrayList<String>();
						idLore.add(plugin.local.getMessage("body-of"));
						idLore.add(((Player)e.getEntity()).getName());
						idMeta.setLore(idLore);
						id.setItemMeta(idMeta);
						// role identifier
						ItemStack ti = new ItemStack(Material.WOOL, 1);
						ItemMeta tiMeta = ti.getItemMeta();
						if (getTTTPlayer(p.getName()).getRole() == Role.INNOCENT){
							ti.setDurability((short)5);
							tiMeta.setDisplayName("§2" + plugin.local.getMessage("innocent"));
							List<String> tiLore = new ArrayList<String>();
							tiLore.add(plugin.local.getMessage("innocent-id"));
							tiMeta.setLore(tiLore);
						}
						else if (getTTTPlayer(p.getName()).getRole() == Role.TRAITOR){
							ti.setDurability((short)14);
							tiMeta.setDisplayName("§4" + plugin.local.getMessage("traitor"));
							List<String> lore = new ArrayList<String>();
							lore.add(plugin.local.getMessage("traitor-id"));
							tiMeta.setLore(lore);
						}
						else if (getTTTPlayer(p.getName()).getRole() == Role.DETECTIVE){
							ti.setDurability((short)11);
							tiMeta.setDisplayName("§1" + plugin.local.getMessage("detective"));
							List<String> lore = new ArrayList<String>();
							lore.add(plugin.local.getMessage("detective-id"));
							tiMeta.setLore(lore);
						}
						ti.setItemMeta(tiMeta);
						chest.getInventory().addItem(new ItemStack[]{id, ti});
						plugin.bodies.add(new Body(p.getName(), getTTTPlayer(p.getName()).getRole(), FixedLocation.getFixedLocation(block), System.currentTimeMillis()));
					}
					else
						p.setHealth(20);
				}
				if (getTTTPlayer(p.getName()).isDead()){
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e){
		if (isPlayer(e.getPlayer().getName()))
			if (getTTTPlayer(e.getPlayer().getName()).isDead())
				e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e){
		if (isPlayer(e.getPlayer().getName())){
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + plugin.local.getMessage("no-drop"));
		}
	}

	@EventHandler
	public void onFoodDeplete(FoodLevelChangeEvent e){
		if (e.getEntity().getType() == EntityType.PLAYER){
			Player p = (Player)e.getEntity();
			if (isPlayer(p.getName()))
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		String p = e.getPlayer().getName();
		if (isPlayer(e.getPlayer().getName())){
			if (getTTTPlayer(p).getRole() != null){
				String worldName = "";
				if (isPlayer(p)){
					worldName = getTTTPlayer(p).getGame();
					destroy(p);
				}
				for (Player pl : plugin.getServer().getWorld("TTT_" + worldName).getPlayers())
					pl.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + p + plugin.local.getMessage("left-game").replace("%", worldName));
				for (Player pl : plugin.getServer().getWorld("TTT_" + worldName).getPlayers())
					pl.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + p + plugin.local.getMessage("left-game").replace("%", worldName));
			}
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e){
		String p = e.getPlayer().getName();
		if (isPlayer(p)){
			if (e.getPlayer().getLocation().getWorld().getName().replace("TTT_", "") != getTTTPlayer(p).getGame()){
				for (Player pl : plugin.getServer().getWorld("TTT_" + getTTTPlayer(p).getGame()).getPlayers())
					pl.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + p + plugin.local.getMessage("left-game").replace("%", getTTTPlayer(p).getGame()));
				destroy(p);
			}
		}
	}

	public void onHealthRegenerate(EntityRegainHealthEvent e){
		if (e.getEntity() instanceof Player){
			Player p = (Player)e.getEntity();
			if (isPlayer(p.getName())){
				if (plugin.gameTime.get(getTTTPlayer(p.getName())) != null)
					e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e){
		if (plugin.discreet.contains(e.getPlayer().getName()))
			plugin.discreet.remove(e.getPlayer().getName());
	}

	@SuppressWarnings("deprecation")
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerChat(AsyncPlayerChatEvent e){
		for (Player p : plugin.getServer().getOnlinePlayers()){
			// check if sender is in TTT game
			if (isPlayer(p.getName())){
				if (getTTTPlayer(e.getPlayer().getName()) != null){
					if (!p.getWorld().getName().equals(e.getPlayer().getWorld().getName()))
						e.getRecipients().remove(p);
				}

				// check if sender is dead
				else if (getTTTPlayer(p.getName()).isDead()){
					if (getTTTPlayer(p.getName()).isDead()){
						if (!p.getWorld().getName().equals("TTT_" + getTTTPlayer(p.getName()).getGame()))
							e.getRecipients().remove(p);
					}
					else
						e.getRecipients().remove(p);
				}
			}
		}

		if (getTTTPlayer(e.getPlayer().getName()) != null){
			TTTPlayer tPlayer = getTTTPlayer(e.getPlayer().getName());
			if (tPlayer.getRole() != null){
				if (tPlayer.getRole() == Role.DETECTIVE){
					final Player player = e.getPlayer();
					e.getPlayer().setDisplayName(ChatColor.BLUE + "[Detective] " + e.getPlayer().getDisplayName());
					plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
						public void run(){
							String name = player.getDisplayName();
							name = name.replace(ChatColor.BLUE + "[Detective] ", "");
							player.setDisplayName(name);
						}
					}, 1L);
				}
			}
		}
	}
}
