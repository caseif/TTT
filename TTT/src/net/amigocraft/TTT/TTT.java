package net.amigocraft.TTT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import net.amigocraft.TTT.AutoUpdate;
import net.amigocraft.TTT.Metrics;
import net.amigocraft.TTT.listeners.BlockListener;
import net.amigocraft.TTT.listeners.PlayerListener;
import net.amigocraft.TTT.localization.Localization;
import net.amigocraft.TTT.managers.CommandManager;
import static net.amigocraft.TTT.TTTPlayer.*;
import net.amigocraft.TTT.utils.NumUtils;
import net.amigocraft.TTT.utils.WorldUtils;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class TTT extends JavaPlugin implements Listener {

	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public static Logger log = Logger.getLogger("Minecraft");
	public static TTT plugin = new TTT();
	public Localization local = new Localization();
	public static String lang;

	public HashMap<String, Integer> time = new HashMap<String, Integer>();
	public HashMap<String, Integer> tasks = new HashMap<String, Integer>();
	public HashMap<String, Integer> gameTime = new HashMap<String, Integer>();
	public List<Body> bodies = new ArrayList<Body>();
	public List<Body> foundBodies = new ArrayList<Body>();
	public List<String> discreet = new ArrayList<String>();

	public int tries = 0;

	@Override
	public void onEnable(){
		// check if server is offline
		if (!getServer().getOnlineMode()){
			if (!getServer().getIp().equals("127.0.0.1") && !getServer().getIp().equals("localhost")){
				log.info("[TTT] This plugin does not support offline servers! Disabling...");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			else
				log.info("[TTT] Server is probably using BungeeCord. Allowing plugin to load...");
		}

		// register events, commands, and the plugin variable
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getCommand("ttt").setExecutor(new CommandManager());
		TTT.plugin = this;

		// check if config should be overwritten
		saveDefaultConfig();
		if (!getConfig().getString("config-version").equals(this.getDescription().getVersion())){
			File config = new File(this.getDataFolder(), "config.yml");
			config.delete();
		}

		// create the default config
		saveDefaultConfig();

		TTT.lang = getConfig().getString("localization");

		// autoupdate
		if (getConfig().getBoolean("enable-auto-update")){
			try {new AutoUpdate(this);}
			catch (Exception e){e.printStackTrace();}
		}

		// submit metrics
		if (getConfig().getBoolean("enable-metrics")){
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			}
			catch (IOException e) {log.warning("[TTT] " + local.getMessage("metrics-fail"));}
		}

		File invDir = new File(this.getDataFolder() + File.separator + "inventories");
		invDir.mkdir();

		log.info(this + " " + local.getMessage("enabled"));
	}

	@Override
	public void onDisable(){
		log.info(this + " " + local.getMessage("disabled"));
	}

	public int setupTimer(final String worldName){
		return getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				// verify that all players are still online
				List<TTTPlayer> offlinePlayers = new ArrayList<TTTPlayer>();
				for (TTTPlayer tp : players){
					if (tp.getGame().equals(worldName)){
						Player p = getServer().getPlayer(tp.getName());
						if (p != null){
							if (!getServer().getWorld("TTT_" + worldName).getPlayers().contains(p)){
								offlinePlayers.add(tp);
								Bukkit.broadcastMessage("[TTT]" + tp.getName() + " " + local.getMessage("left-map") + " \"" + worldName + "\"");
							}
						}
					}
				}
				for (TTTPlayer p : offlinePlayers){
					p.destroy();
				}
				int currentTime = time.get(worldName);
				int playerCount = 0; 
				for (TTTPlayer tp : players){
					if (tp.getGame().equals(worldName))
						playerCount += 1;
				}
				if (playerCount >= getConfig().getInt("minimum-players")){
					if((currentTime % 10) == 0 && currentTime > 0){
						for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + local.getMessage("begin") + " " + currentTime + " " + local.getMessage("seconds") + "!");
						}
					}
					else if (currentTime > 0 && currentTime < 10){
						for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + local.getMessage("begin") + " " + currentTime + " " + local.getMessage("seconds") + "!");
						}
					}
					else if (currentTime <= 0){
						int players = getServer().getWorld("TTT_" + worldName).getPlayers().size();
						int traitorNum = 0;
						int limit = (int)(players * getConfig().getDouble("traitor-ratio"));
						if (limit == 0)
							limit = 1;
						List<String> innocents = new ArrayList<String>();
						List<String> traitors = new ArrayList<String>();
						List<String> detectives = new ArrayList<String>();
						for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
							innocents.add(p.getName());
							p.sendMessage(ChatColor.DARK_PURPLE + local.getMessage("begun"));
						}
						while (traitorNum < limit){
							Random randomGenerator = new Random();
							int index = randomGenerator.nextInt(players);
							String traitor = innocents.get(index);
							if (innocents.contains(traitor)){
								innocents.remove(traitor);
								traitors.add(traitor);
								traitorNum += 1;
							}
						}
						int dLimit = (int)(players * getConfig().getDouble("detective-ratio"));
						if (players >= getConfig().getInt("minimum-players-for-detective") && dLimit == 0)
							dLimit += 1;
						int detectiveNum = 0;
						while (detectiveNum < dLimit){
							Random randomGenerator = new Random();
							int index = randomGenerator.nextInt(innocents.size());
							String detective = innocents.get(index);
							innocents.remove(detective);
							detectives.add(detective);
							detectiveNum += 1;
						}
						ItemStack crowbar = new ItemStack(Material.IRON_SWORD, 1);
						ItemMeta cbMeta = crowbar.getItemMeta();
						cbMeta.setDisplayName("§5" + local.getMessage("crowbar"));
						crowbar.setItemMeta(cbMeta);
						ItemStack gun = new ItemStack(Material.ANVIL, 1);
						ItemMeta gunMeta = crowbar.getItemMeta();
						gunMeta.setDisplayName("§5" + local.getMessage("gun"));
						gun.setItemMeta(gunMeta);
						ItemStack ammo = new ItemStack(Material.ARROW, 28);
						ItemStack dnaScanner = new ItemStack(Material.COMPASS, 1);
						ItemMeta dnaMeta = dnaScanner.getItemMeta();
						dnaMeta.setDisplayName("§1" + local.getMessage("dna-scanner"));
						dnaScanner.setItemMeta(dnaMeta);
						for (TTTPlayer tp : TTTPlayer.players){
							Player pl = getServer().getPlayer(tp.getName());
							if (innocents.contains(tp.getName())){
								tp.setRole(Role.INNOCENT);
								pl.sendMessage(ChatColor.DARK_GREEN + local.getMessage("you-are-innocent"));
								pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo});
							}
							else if (traitors.contains(tp.getName())){
								tp.setRole(Role.TRAITOR);
								pl.sendMessage(ChatColor.DARK_RED + local.getMessage("you-are-traitor"));
								if (traitors.size() > 1){
									pl.sendMessage(ChatColor.DARK_RED + local.getMessage("allies"));
									for (String t : traitors)
										pl.sendMessage("- " + t);
								}
								else
									pl.sendMessage(ChatColor.DARK_RED + local.getMessage("alone"));
								pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo});
							}
							else if (detectives.contains(tp.getName())){
								tp.setRole(Role.DETECTIVE);
								pl.sendMessage(ChatColor.BLUE + local.getMessage("you-are-detective"));
								pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo, dnaScanner});
							}
							pl.setHealth(20);
							pl.setFoodLevel(20);
						}
						time.remove(worldName);
						gameTime.put(worldName, getConfig().getInt("time-limit"));
						Bukkit.getScheduler().cancelTask(tasks.get(worldName));
						tasks.remove(worldName);
						gameTimer(worldName);
					}
					if (currentTime > 0)
						time.put(worldName, currentTime - 1);
				}
				else {
					time.remove(worldName);
					Bukkit.getScheduler().cancelTask(tasks.get(worldName));
					for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
						p.sendMessage(ChatColor.DARK_PURPLE + local.getMessage("waiting"));
					}
				}
			}
		}, 0L, 20L);
	}

	public void gameTimer(final String worldName){
		tasks.put(worldName, getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			@SuppressWarnings("deprecation")
			public void run(){
				// verify that all players are still online
				List<TTTPlayer> offlinePlayers = new ArrayList<TTTPlayer>();
				for (TTTPlayer tp : players){
					if (tp.getGame().equals(worldName)){
						Player p = getServer().getPlayer(tp.getName());
						if (p != null){
							if (!getServer().getWorld("TTT_" + worldName).getPlayers().contains(p)){
								Bukkit.broadcastMessage("[TTT]" + tp.getName() + " " + local.getMessage("left-map") + " \"" + worldName + "\"");
								offlinePlayers.add(tp);
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
						Player tracker = getServer().getPlayer(p.getName());
						Player killer = getServer().getPlayer(p.getKiller());
						if (tracker != null || killer != null)
							if (!offlinePlayers.contains(tracker) && !offlinePlayers.contains(killer))
								tracker.setCompassTarget(killer.getLocation());
					}
				}

				// check if game is over
				boolean iLeft = false;
				boolean tLeft = false;
				for (TTTPlayer tp : players){
					if (tp.getGame().equals(worldName)){
						if (tp.getRole() == Role.INNOCENT){
							iLeft = true;
						}
						if (tp.getRole() == Role.TRAITOR){
							tLeft = true;
						}
					}
				}
				if (!(tLeft && iLeft)){
					List<Body> removeBodies = new ArrayList<Body>();
					List<Body> removeFoundBodies = new ArrayList<Body>(); 
					for (Body b : bodies){
						if (getTTTPlayer(b.getName()).isDead()){
							if (getTTTPlayer(b.getName()).getGame().equals(worldName)){
								removeBodies.add(b);
								if (foundBodies.contains(b))
									removeFoundBodies.add(b);
							}
						}
					}

					for (Body b : removeBodies)
						bodies.remove(b);

					for (Body b : removeFoundBodies)
						foundBodies.remove(b);

					removeBodies.clear();
					removeFoundBodies.clear();

					if (!tLeft)
						Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[TTT] " + local.getMessage("innocent-win").replace("%", "\"" + worldName + "\"") + "!");
					if (!iLeft)
						Bukkit.broadcastMessage(ChatColor.DARK_RED + "[TTT] " + local.getMessage("traitor-win").replace("%", "\"" + worldName + "\"") + "!");
					for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
						if (isPlayer(p.getName())){
							TTTPlayer tp = getTTTPlayer(p.getName());
							if (tp != null){
								if (tp.isDead()){
									p.setAllowFlight(false);
									for (Player pl : getServer().getOnlinePlayers()){
										pl.showPlayer(p);
									}
								}
								tp.destroy();
								p.getInventory().clear();
								File invF = new File(getDataFolder() + File.separator + "inventories" + File.separator + p.getName() + ".inv");
								if (invF.exists()){
									try {
										YamlConfiguration invY = new YamlConfiguration();
										invY.load(invF);
										ItemStack[] invI = new ItemStack[p.getInventory().getSize()];
										for (String k : invY.getKeys(false)){
											invI[Integer.parseInt(k)] = invY.getItemStack(k);
										}
										p.getInventory().setContents(invI);
										p.updateInventory();
										invF.delete();
									}
									catch (Exception ex){
										ex.printStackTrace();
										p.sendMessage(ChatColor.RED + "[TTT] " + local.getMessage("inv-load-error"));
									}
								}
							}
						}
						WorldUtils.teleportPlayer(p);
					}
					gameTime.remove(worldName);
					getServer().getScheduler().cancelTask(tasks.get(worldName));
					tasks.remove(tasks.get(worldName));
					getServer().unloadWorld("TTT_" + worldName, false);
					rollbackWorld(worldName);
				}
				else {
					int newTime = gameTime.get(worldName) - 1;
					gameTime.remove(worldName);
					gameTime.put(worldName, newTime);
					if (newTime % 60 == 0 && newTime >= 60){
						for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + Integer.toString(newTime / 60) + " " + local.getMessage("minutes") + " " + local.getMessage("left"));
						}
					}
					else if (newTime % 10 == 0 && newTime > 10 && newTime < 60){
						for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + Integer.toString(newTime) + " " + local.getMessage("seconds") + " " + local.getMessage("left"));
						}
					}
					else if (newTime < 10 && newTime > 0){
						for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + Integer.toString(newTime) + " " + local.getMessage("seconds") + " " + local.getMessage("left"));
						}
					}
					else if (newTime <= 0){
						List<Body> removeBodies = new ArrayList<Body>();
						List<Body> removeFoundBodies = new ArrayList<Body>(); 
						for (Body b : bodies){
							if (getTTTPlayer(b.getName()).isDead()){
								if (getTTTPlayer(b.getName()).getGame().equals(worldName)){
									removeBodies.add(b);
									if (foundBodies.contains(b))
										removeFoundBodies.add(b);
								}
							}
						}

						for (Body b : removeBodies)
							bodies.remove(b);

						for (Body b : removeFoundBodies)
							foundBodies.remove(b);

						removeBodies.clear();
						removeFoundBodies.clear();

						for (Player p : getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_GREEN + "[TTT] " + local.getMessage("innocent-win").replace("%", "\"" + worldName + "\"") + "!");
							if (getTTTPlayer(p.getName()).isDead()){
								p.setAllowFlight(false);
								for (Player pl : getServer().getOnlinePlayers()){
									pl.showPlayer(p);
								}
							}
							getTTTPlayer(p.getName()).destroy();
							p.getInventory().clear();
							File invF = new File(getDataFolder() + File.separator + "inventories" + File.separator + p.getName() + ".inv");
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
									p.sendMessage(ChatColor.RED + "[TTT] " + local.getMessage("inv-load-fail"));
								}
							}
							gameTime.remove(worldName);
							WorldUtils.teleportPlayer(p);
						}
						Bukkit.getScheduler().cancelTask(tasks.get(worldName));
						getServer().unloadWorld("TTT_" + worldName, false);
						rollbackWorld(worldName);
					}
				}
				// hide dead players
				for (TTTPlayer p : players){
					if (p.isDead()){
						if (getServer().getPlayer(p.getName()) != null){
							if (getServer().getWorld("TTT_" + worldName).getPlayers().contains(getServer().getPlayer(p.getName()))){
								getServer().getPlayer(p.getName()).setAllowFlight(true);
								for (TTTPlayer other : players){
									if (other.getGame().equals(worldName))
										getServer().getPlayer(other.getName()).hidePlayer(getServer().getPlayer(p.getName()));
								}
							}
						}
					}
				}
			}
		}, 0L, 20L));
	}

	public void rollbackWorld(final String worldName){
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run(){
				File folder = new File(worldName);
				if (folder.exists()){
					if (WorldUtils.isWorld(folder)){
						File newFolder = new File("TTT_" + worldName);
						try {
							FileUtils.copyDirectory(folder, newFolder);
							log.info("[TTT] " + local.getMessage("rollback") + " \"" + worldName + "\"!");
						}
						catch (IOException ex){
							log.info("[TTT] " + local.getMessage("folder-error") + " " + worldName);
							ex.printStackTrace();
						}
					}
					else
						log.info("[TTT] " + local.getMessage("cannot-load-world"));
				}
			}
		}, 100L);
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
					Block block = ((Chest)e.getInventory().getHolder()).getBlock();
					for (Body b : bodies){
						if (b.getLocation().equals(FixedLocation.getFixedLocation(block))){
							e.setCancelled(true);
							break;
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerChat(AsyncPlayerChatEvent e){
		for (Player p : getServer().getOnlinePlayers()){
			// check if sender is in TTT game
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

		if (getTTTPlayer(e.getPlayer().getName()) != null){
			TTTPlayer tPlayer = getTTTPlayer(e.getPlayer().getName());
			if (tPlayer.getRole() != null){
				if (tPlayer.getRole() == Role.DETECTIVE){
					final Player player = e.getPlayer();
					e.getPlayer().setDisplayName(ChatColor.BLUE + "[Detective] " + e.getPlayer().getDisplayName());
					getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
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

	public void removeArrow(Inventory inv){
		for (int i = 0; i < inv.getContents().length; i++){
			ItemStack is = inv.getItem(i);
			if (is != null){
				if (is.getType() == Material.ARROW){
					if (is.getAmount() == 1)
						inv.setItem(i, null);
					else if (is.getAmount() > 1)
						is.setAmount(is.getAmount() - 1);
					break;
				}
			}
		}
	}
}
