package net.amigocraft.TTT.listeners;

import static net.amigocraft.TTT.TTTPlayer.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.amigocraft.TTT.Body;
import net.amigocraft.TTT.LobbySign;
import net.amigocraft.TTT.Location2i;
import net.amigocraft.TTT.Role;
import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.Stage;
import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;
import net.amigocraft.TTT.managers.KarmaManager;
import net.amigocraft.TTT.managers.LobbyManager;
import net.amigocraft.TTT.managers.RoundManager;
import net.amigocraft.TTT.utils.InventoryUtils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerListener implements Listener {

	private TTT plugin = TTT.plugin;

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e){
		if (isPlayer(e.getPlayer().getName())){ // check if player is in TTT round
			TTTPlayer tPlayer = getTTTPlayer(e.getPlayer().getName());
			// disable pressure plates for dead players
			if (e.getAction() == Action.PHYSICAL && tPlayer.isDead()){
				e.setCancelled(true);
				return;
			}
			else if (e.getAction() == Action.RIGHT_CLICK_BLOCK){
				// disallow cheating/bed setting
				if (e.getClickedBlock().getType() == Material.ENDER_CHEST ||
						e.getClickedBlock().getType() == Material.BED_BLOCK){
					e.setCancelled(true);
					return;
				}
				// handle body checking
				if (e.getClickedBlock().getType() == Material.CHEST){
					if (tPlayer.isDead()){
						e.setCancelled(true);
						for (Body b : TTT.bodies){
							if (b.getLocation().equals(Location2i.getLocation(e.getClickedBlock()))){
								Inventory chestinv = ((Chest)e.getClickedBlock().getState()).getInventory();
								Inventory inv = plugin.getServer().createInventory(null, chestinv.getSize());
								inv.setContents(chestinv.getContents());
								e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("discreet"));
								tPlayer.setDiscreet(true);
								e.getPlayer().openInventory(inv);
								break;
							}
						}
					}
					else {
						int index = -1;
						for (int i = 0; i < TTT.bodies.size(); i++){
							if (TTT.bodies.get(i).getLocation().equals(
									Location2i.getLocation(e.getClickedBlock()))){
								index = i;
								break;
							}
						}
						if (index != -1){
							boolean found = false;
							for (Body b : TTT.foundBodies){
								if (b.getLocation().equals(Location2i.getLocation(e.getClickedBlock()))){
									found = true;
									break;
								}
							}
							if (!found){ // it's a new body
								for (Player p : e.getPlayer().getWorld().getPlayers()){
									if (TTT.bodies.get(index).getPlayer().getRole() == Role.INNOCENT)
										p.sendMessage(ChatColor.DARK_GREEN + e.getPlayer().getName() + " " +
												TTT.local.getMessage("found-body").replace("%",
														TTT.bodies.get(index).getPlayer().getName())  + ". " +
														TTT.local.getMessage("was-innocent"));
									else if (TTT.bodies.get(index).getPlayer().getRole() == Role.TRAITOR)
										p.sendMessage(ChatColor.DARK_RED + e.getPlayer().getName() + " " +
												TTT.local.getMessage("found-body").replace("%",
														TTT.bodies.get(index).getPlayer().getName())  + ". " +
														TTT.local.getMessage("was-traitor"));
									else if (TTT.bodies.get(index).getPlayer().getRole() == Role.DETECTIVE)
										p.sendMessage(ChatColor.DARK_BLUE + e.getPlayer().getName() + " " +
												TTT.local.getMessage("found-body").replace("%",
														TTT.bodies.get(index).getPlayer().getName())  + ". " +
														TTT.local.getMessage("was-detective"));
								}
								TTT.foundBodies.add(TTT.bodies.get(index));
								TTT.bodies.get(index).getPlayer().setBodyFound(true);
								LobbyManager.updateSigns(TTT.bodies.get(index).getPlayer().getWorld());
							}
							if (tPlayer.getRole() == Role.DETECTIVE){ // handle DNA scanning
								if (e.getPlayer().getItemInHand() != null){
									if (e.getPlayer().getItemInHand().getType() == Material.COMPASS){
										if (e.getPlayer().getItemInHand().getItemMeta() != null){
											if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName()
													!= null){
												if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName()
														.equals("�1" + TTT.local.getMessage("dna-scanner"))){
													e.setCancelled(true);
													Player killer = plugin.getServer().getPlayer(
															getTTTPlayer(TTT.bodies.get(index).getPlayer()
																	.getName()).getKiller());
													if (killer != null){
														if (isPlayer(killer.getName())){
															tPlayer.setTracking(killer.getName());
															e.getPlayer().sendMessage(ChatColor.BLUE +
																	TTT.local.getMessage("collected-sample")
																	.replace("%", TTT.bodies.get(index).getPlayer()
																			.getName()));
														}
														else
															e.getPlayer().sendMessage(ChatColor.BLUE +
																	TTT.local.getMessage("killer-left"));
													}
													else
														e.getPlayer().sendMessage(ChatColor.BLUE +
																TTT.local.getMessage("killer-left"));
													return;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			if (tPlayer.isDead()){
				e.setCancelled(true);
				return;
			}
		}
		// guns
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
			if (e.getPlayer().getItemInHand() != null){
				if (e.getPlayer().getItemInHand().getItemMeta() != null){
					if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null){
						if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName()
								.endsWith(TTT.local.getMessage("gun"))){
							if ((isPlayer(e.getPlayer().getName()) &&
									Round.getRound(getTTTPlayer(e.getPlayer().getName())
											.getWorld()).getStage() == Stage.PLAYING) ||
									plugin.getConfig().getBoolean("guns-outside-arenas")){
								e.setCancelled(true);
								if (e.getPlayer().getInventory().contains(Material.ARROW) ||
										!plugin.getConfig().getBoolean("require-ammo-for-guns")){
									if (plugin.getConfig().getBoolean("require-ammo-for-guns")){
										InventoryUtils.removeArrow(e.getPlayer().getInventory());
										e.getPlayer().updateInventory();
									}
									e.getPlayer().launchProjectile(Arrow.class);
								}
								else
									e.getPlayer().sendMessage(ChatColor.RED +
											TTT.local.getMessage("need-ammo"));
							}
							return;
						}
					}
				}
			}
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK){
			LobbySign sign = null;
			for (LobbySign l : LobbyManager.signs){
				if (l.getX() == e.getClickedBlock().getX() && l.getY() == e.getClickedBlock().getY() &&
						l.getZ() == e.getClickedBlock().getZ() &&
						l.getWorld().equals(e.getClickedBlock().getWorld().getName())){
					if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getPlayer().isSneaking() &&
							e.getPlayer().hasPermission("ttt.lobby.destroy")){
						sign = l;
						e.getPlayer().sendMessage(ChatColor.GREEN + "[TTT] " +
								TTT.local.getMessage("lobby-unregister"));
					}
					else if (e.getPlayer().hasPermission("ttt.lobby.use")){
						RoundManager.handleJoin(e.getPlayer(), l.getRound());
						e.setCancelled(true);
					}
					else {
						e.getPlayer().sendMessage(ChatColor.RED +
								"[TTT] " + TTT.local.getMessage("no-permission"));
						e.setCancelled(true);
					}
				}
			}
			if (sign != null)
				LobbyManager.removeSign(sign);
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
		if (e.getMessage().startsWith("kit")){
			if (isPlayer(e.getPlayer().getName())){
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + TTT.local.getMessage("no-kits"));
			}
		}
		else if (e.getMessage().startsWith("msg") || e.getMessage().startsWith("tell") || e.getMessage()
				.startsWith("r") || e.getMessage().startsWith("msg") || e.getMessage().startsWith("me")){
			String p = e.getPlayer().getName();
			if (isPlayer(p)){
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + TTT.local.getMessage("no-pm"));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e){
		if (e.getEntityType() == EntityType.PLAYER){
			if (e instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent)e;
				if (ed.getDamager().getType() == EntityType.PLAYER){
					Player damager = (Player)ed.getDamager();
					if (isPlayer(damager.getName())){
						TTTPlayer dt = getTTTPlayer(((Player)ed.getDamager()).getName());
						if (dt.isDead()){
							e.setCancelled(true);
							return;
						}
						if (dt.getRound().getStage() != Stage.PLAYING){
							e.setCancelled(true);
							return;
						}
						if (damager.getItemInHand() != null)
							if (damager.getItemInHand().getItemMeta() != null)
								if (damager.getItemInHand().getItemMeta().getDisplayName()
										!= null)
									if (damager.getItemInHand().getItemMeta().getDisplayName()
											.endsWith(TTT.local.getMessage("crowbar")))
										e.setDamage(plugin.getConfig().getInt("crowbar-damage"));
						e.setDamage((int)(e.getDamage() * dt.getDamageReduction()));
					}
				}
			}
			Player p = (Player)e.getEntity();
			if (isPlayer(p.getName())){
				TTTPlayer t = getTTTPlayer(p.getName());
				if (t.getRound().getStage() != Stage.PLAYING)
					e.setCancelled(true);
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
				int actualDamage = (int)(e.getDamage() - ((armor * .04) * e.getDamage()));
				TTT.log.info(actualDamage + "");
				if (e instanceof EntityDamageByEntityEvent){
					EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent)e;
					if (ed.getDamager() instanceof Player){
						KarmaManager.handleDamageKarma(getTTTPlayer(
								((Player)(ed).getDamager()).getName()), t, actualDamage);
					}
					else if (ed.getDamager() instanceof Projectile){
						if (((Projectile)ed.getDamager()).getShooter() instanceof Player){
							KarmaManager.handleDamageKarma(getTTTPlayer(
									((Player)((Projectile)ed.getDamager()).getShooter()).getName()), t, actualDamage);
						}
					}
				}
				if (actualDamage >= ((Player)e.getEntity()).getHealth()){
					if (t.getRole() != null){
						e.setCancelled(true);
						p.setHealth(20);
						p.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("dead"));
						t.setDead(true);
						if (e instanceof EntityDamageByEntityEvent)
							if (((EntityDamageByEntityEvent)e).getDamager() instanceof Player)
								t.setKiller(((Player)((EntityDamageByEntityEvent)e).getDamager()).getName());
						Block block = p.getLocation().getBlock();
						block.setType(Material.CHEST);
						Chest chest = (Chest)block.getState();
						// player identifier
						ItemStack id = new ItemStack(Material.PAPER, 1);
						ItemMeta idMeta = id.getItemMeta();
						idMeta.setDisplayName(TTT.local.getMessage("id"));
						List<String> idLore = new ArrayList<String>();
						idLore.add(TTT.local.getMessage("body-of"));
						idLore.add(t.getName());
						idMeta.setLore(idLore);
						id.setItemMeta(idMeta);
						// role identifier
						ItemStack ti = new ItemStack(Material.WOOL, 1);
						ItemMeta tiMeta = ti.getItemMeta();
						if (t.getRole() == Role.INNOCENT){
							ti.setDurability((short)5);
							tiMeta.setDisplayName("�2" + TTT.local.getMessage("innocent"));
							List<String> tiLore = new ArrayList<String>();
							tiLore.add(TTT.local.getMessage("innocent-id"));
							tiMeta.setLore(tiLore);
						}
						else if (t.getRole() == Role.TRAITOR){
							ti.setDurability((short)14);
							tiMeta.setDisplayName("�4" + TTT.local.getMessage("traitor"));
							List<String> lore = new ArrayList<String>();
							lore.add(TTT.local.getMessage("traitor-id"));
							tiMeta.setLore(lore);
						}
						else if (t.getRole() == Role.DETECTIVE){
							ti.setDurability((short)11);
							tiMeta.setDisplayName("�1" + TTT.local.getMessage("detective"));
							List<String> lore = new ArrayList<String>();
							lore.add(TTT.local.getMessage("detective-id"));
							tiMeta.setLore(lore);
						}
						ti.setItemMeta(tiMeta);
						chest.getInventory().addItem(new ItemStack[]{id, ti});
						TTT.bodies.add(new Body(t, Location2i.getLocation(block), System.currentTimeMillis()));

						if (e instanceof EntityDamageByEntityEvent){
							EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent)e;
							if (ed.getDamager() instanceof Player){
								// set killer's karma
								TTTPlayer victim = t;
								TTTPlayer killer = getTTTPlayer(((Player)(ed.getDamager())).getName());
								KarmaManager.handleKillKarma(killer, victim);
							}
							else if (ed.getDamager() instanceof Projectile){
								if (((Projectile)ed.getDamager()).getShooter() != null &&
										((Projectile)ed.getDamager()).getShooter()
										instanceof Player){
									KarmaManager.handleKillKarma(getTTTPlayer(
											((Player)((Projectile)ed.getDamager()).getShooter()).getName()), t);
								}
							}
						}
						LobbyManager.updateSigns(t.getWorld());
					}
					else
						p.setHealth(20);
				}
				if (t.isDead()){
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e){
		if (isPlayer(e.getPlayer().getName()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e){
		if (isPlayer(e.getPlayer().getName())){
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "[TTT] " + TTT.local.getMessage("no-drop"));
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
	public void onPlayerJoin(PlayerJoinEvent e){
		if (TTT.plugin.getConfig().getBoolean("karma-persistence"))
			KarmaManager.loadKarma(e.getPlayer().getName());
		if (e.getPlayer().hasPermission("ttt.build.warn"))
			if (TTT.stability.equals("unstable"))
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "[TTT] " + TTT.local.getMessage("unstable-build"));
			else if (TTT.stability.equals("unknown"))
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "[TTT] " + TTT.local.getMessage("unknown-build"));
			else if (TTT.stability.equals("pre"))
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "[TTT] " + TTT.local.getMessage("prerelease"));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		String p = e.getPlayer().getName();
		if (isPlayer(p)){
			String worldName = "";
			worldName = getTTTPlayer(p).getWorld();
			RoundManager.resetPlayer(e.getPlayer());
			for (Player pl : plugin.getServer().getWorld("TTT_" + worldName).getPlayers())
				pl.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + p + " " + TTT.local.getMessage("left-game")
						.replace("%", worldName));
		}
		if (!TTT.plugin.getConfig().getBoolean("karma-persistence"))
			KarmaManager.playerKarma.remove(e.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e){
		String p = e.getPlayer().getName();
		if (isPlayer(p)){
			if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())){
				String worldName = "TTT_" + getTTTPlayer(p).getWorld();
				RoundManager.resetPlayer(e.getPlayer());
				for (Player pl : plugin.getServer().getWorld(worldName).getPlayers())
					pl.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + p + " " + TTT.local.getMessage("left-game")
							.replace("%", worldName));
			}
		}
	}

	@EventHandler
	public void onHealthRegenerate(EntityRegainHealthEvent e){
		if (e.getEntity() instanceof Player){
			Player p = (Player)e.getEntity();
			if (isPlayer(p.getName())){
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e){
		if (isPlayer(e.getPlayer().getName()))
			TTTPlayer.getTTTPlayer(e.getPlayer().getName()).setDiscreet(false);
	}

	@SuppressWarnings("deprecation")
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent e){
		for (Player p : plugin.getServer().getOnlinePlayers()){
			// check if recipient is in TTT game
			if (isPlayer(p.getName())){ // recipient is in game
				if (!p.getWorld().getName().equals(e.getPlayer().getWorld().getName())) // sender is not in game
					e.getRecipients().remove(p);
				else if (isPlayer(e.getPlayer().getName())){
					if (getTTTPlayer(p.getName()).isDead() !=
							getTTTPlayer(e.getPlayer().getName()).isDead()) // one is dead; the other is not
						e.getRecipients().remove(p);
				}
			}
		}

		if (isPlayer(e.getPlayer().getName())){
			TTTPlayer tPlayer = getTTTPlayer(e.getPlayer().getName());
			if (tPlayer.getRole() != null){
				if (tPlayer.getRole() == Role.DETECTIVE){
					final Player player = e.getPlayer();
					e.getPlayer().setDisplayName(ChatColor.DARK_BLUE + "[Detective] " + ChatColor.DARK_BLUE + e.getPlayer()
							.getDisplayName());
					plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
						public void run(){
							String name = player.getDisplayName();
							name = name.replace(ChatColor.DARK_BLUE + "[Detective] " + ChatColor.DARK_BLUE, "");
							player.setDisplayName(name);
						}
					}, 1L);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent e){
		for (HumanEntity he : e.getViewers()){
			Player p = (Player)he;
			if (isPlayer(p.getName())){
				if (getTTTPlayer(p.getName()).isDead()){
					e.setCancelled(true);
				}
				else if (e.getInventory().getType() == InventoryType.CHEST){
					Block block = null;
					Block block2 = null;
					if (e.getInventory().getHolder() instanceof Chest)
						block = ((Chest)e.getInventory().getHolder()).getBlock();
					else if (e.getInventory().getHolder() instanceof DoubleChest){
						block = ((Chest)((DoubleChest)e.getInventory().getHolder()).getLeftSide()).getBlock();
						block2 = ((Chest)((DoubleChest)e.getInventory().getHolder()).getRightSide()).getBlock();
					}
					boolean found1 = false;
					boolean found2 = false;
					for (Body b : TTT.bodies){
						if (b.getLocation().equals(Location2i.getLocation(block))){
							found1 = true;
							e.setCancelled(true);
							if (block2 == null || found2)
								break;
						}
						if (block2 != null){
							if (b.getLocation().equals(Location2i.getLocation(block2))){
								e.setCancelled(true);
								if (!found1)
									break;
							}
						}
					}
				}
			}
		}
	}


}
