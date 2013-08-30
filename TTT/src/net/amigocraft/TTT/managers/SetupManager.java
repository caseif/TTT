package net.amigocraft.TTT.managers;

import static net.amigocraft.TTT.TTTPlayer.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.amigocraft.TTT.Role;
import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.Stage;
import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetupManager {

	private static TTT plugin = TTT.plugin;

	private static HashMap<String, Integer> tasks = new HashMap<String, Integer>();

	private static List<String> checkPlayers = new ArrayList<String>();

	public static void setupTimer(final String worldName){

		ScoreManager.sbManagers.put(worldName, new ScoreManager(worldName));

		tasks.put(worldName, plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable(){

			public void run(){

				Round r = Round.getRound(worldName);

				// verify that all players are still online
				List<TTTPlayer> offlinePlayers = new ArrayList<TTTPlayer>();
				for (TTTPlayer tp : players){
					if (tp.getWorld().equals(worldName)){
						Player p = plugin.getServer().getPlayer(tp.getName());
						if (p != null){
							if (!plugin.getServer().getWorld("TTT_" + worldName).getPlayers().contains(p)){
								if (checkPlayers.contains(tp.getName())){
									if (plugin.getConfig().getBoolean("verbose-logging"))
										TTT.log.info(tp.getName() +
												" was missing from TTT world for 2 ticks, removing...");
									checkPlayers.remove(tp.getName());
									offlinePlayers.add(tp);
									Bukkit.broadcastMessage("[TTT] " + tp.getName() + " " +
											TTT.local.getMessage("left-map") + " \"" + worldName + "\"");
								}
								else
									checkPlayers.add(tp.getName());
							}
						}
					}
				}
				for (TTTPlayer p : offlinePlayers){
					p.destroy();
				}
				LobbyManager.updateSigns(worldName);

				// manage scoreboards
				ScoreManager.sbManagers.get(worldName).manage();

				int currentTime = r.getTime();
				int playerCount = 0; 
				for (TTTPlayer tp : players){
					if (tp.getWorld().equals(worldName))
						playerCount += 1;
				}
				if (playerCount >= plugin.getConfig().getInt("minimum-players")){
					if((currentTime % 10) == 0 && currentTime > 0){
						for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("begin")
									.replace("%", currentTime + " " + TTT.local.getMessage("seconds") + "!"));
						}
					}
					else if (currentTime > 0 && currentTime < 10){
						for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
							p.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("begin")
									.replace("%", currentTime + " " + TTT.local.getMessage("seconds") + "!"));
						}
					}
					else if (currentTime <= 0){
						int players = plugin.getServer().getWorld("TTT_" + worldName).getPlayers().size();
						int traitorNum = 0;
						int limit = (int)(players * plugin.getConfig().getDouble("traitor-ratio"));
						if (limit == 0)
							limit = 1;
						List<String> innocents = new ArrayList<String>();
						List<String> traitors = new ArrayList<String>();
						List<String> detectives = new ArrayList<String>();
						for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
							innocents.add(p.getName());
							p.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("begun"));
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
						int dLimit = (int)(players * plugin.getConfig().getDouble("detective-ratio"));
						if (players >= plugin.getConfig().getInt("minimum-players-for-detective") && dLimit == 0)
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
						cbMeta.setDisplayName("§5" + TTT.local.getMessage("crowbar"));
						crowbar.setItemMeta(cbMeta);
						ItemStack gun = new ItemStack(Material.ANVIL, 1);
						ItemMeta gunMeta = crowbar.getItemMeta();
						gunMeta.setDisplayName("§5" + TTT.local.getMessage("gun"));
						gun.setItemMeta(gunMeta);
						ItemStack ammo = new ItemStack(Material.ARROW, 28);
						ItemStack dnaScanner = new ItemStack(Material.COMPASS, 1);
						ItemMeta dnaMeta = dnaScanner.getItemMeta();
						dnaMeta.setDisplayName("§1" + TTT.local.getMessage("dna-scanner"));
						dnaScanner.setItemMeta(dnaMeta);
						for (String s : innocents){
							Player pl = plugin.getServer().getPlayer(s);
							TTTPlayer t = TTTPlayer.getTTTPlayer(s);
							if (pl != null && t != null){
								t.setRole(Role.INNOCENT);
								pl.sendMessage(ChatColor.DARK_GREEN + TTT.local.getMessage("you-are-innocent"));
								pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo});
								pl.setHealth(20);
								pl.setFoodLevel(20);
							}
						}
						for (String s : traitors){
							Player pl = plugin.getServer().getPlayer(s);
							TTTPlayer t = TTTPlayer.getTTTPlayer(s);
							if (pl != null && t != null){
								t.setRole(Role.TRAITOR);
								pl.sendMessage(ChatColor.DARK_RED + TTT.local.getMessage("you-are-traitor"));
								if (traitors.size() > 1){
									pl.sendMessage(ChatColor.DARK_RED + TTT.local.getMessage("allies"));
									for (String tr : traitors){
										if (!tr.equals(s))
											pl.sendMessage("- " + t.getName());
									}
								}
								else
									pl.sendMessage(ChatColor.DARK_RED + TTT.local.getMessage("alone"));
								pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo});
								pl.setHealth(20);
								pl.setFoodLevel(20);
							}
						}
						for (String s : detectives){
							Player pl = plugin.getServer().getPlayer(s);
							TTTPlayer t = TTTPlayer.getTTTPlayer(s);
							if (pl != null && t != null){
								t.setRole(Role.DETECTIVE);
								pl.sendMessage(ChatColor.BLUE + TTT.local.getMessage("you-are-detective"));
								pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo, dnaScanner});
								pl.setHealth(20);
								pl.setFoodLevel(20);
							}
						}

						if (TTT.plugin.getConfig().getBoolean("damage-reduction")){
							for (Player p : plugin.getServer().getOnlinePlayers()){
								if (TTTPlayer.isPlayer(p.getName())){
									TTTPlayer t = TTTPlayer.getTTTPlayer(p.getName());
									t.calculateDamageReduction();
									String percentage = TTT.local.getMessage("full");
									if (t.getDamageReduction() < 1)
										percentage =
										Integer.toString((int)(t.getDamageReduction() * 100)) + "%";
									p.sendMessage(ChatColor.DARK_PURPLE +
											TTT.local.getMessage("karma-damage")
											.replace("%", Integer.toString(t.getKarma()))
											.replace("&", percentage));
								}
							}
						}

						r.setTime(plugin.getConfig().getInt("time-limit"));
						r.setStage(Stage.PLAYING);
						new RoundManager().gameTimer(worldName);
						plugin.getServer().getScheduler().cancelTask(tasks.get(worldName));
						tasks.remove(worldName);
						LobbyManager.updateSigns(worldName);
					}
					if (currentTime > 0)
						r.tickDown();
				}
				else {
					r.setTime(0);
					r.setStage(Stage.WAITING);
					for (Player p : plugin.getServer().getWorld("TTT_" + worldName).getPlayers()){
						p.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("waiting"));
					}
					plugin.getServer().getScheduler().cancelTask(tasks.get(worldName));
					tasks.remove(worldName);
				}
			}
		}, 0L, 20L).getTaskId());
	}
}
