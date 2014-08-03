package net.amigocraft.ttt.listeners;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.amigocraft.mglib.api.Location3D;
import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.api.Stage;
import net.amigocraft.mglib.event.player.MGPlayerDeathEvent;
import net.amigocraft.mglib.event.player.PlayerJoinMinigameRoundEvent;
import net.amigocraft.mglib.event.player.PlayerLeaveMinigameRoundEvent;
import net.amigocraft.mglib.event.round.MinigameRoundEndEvent;
import net.amigocraft.mglib.event.round.MinigameRoundPrepareEvent;
import net.amigocraft.mglib.event.round.MinigameRoundTickEvent;
import net.amigocraft.ttt.Body;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.TTTPlayer;
import net.amigocraft.ttt.Variables;
import net.amigocraft.ttt.managers.KarmaManager;
import net.amigocraft.ttt.managers.ScoreManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MGListener implements Listener {

	@EventHandler
	public void onMinigameRoundPrepareEvent(MinigameRoundPrepareEvent e){
		e.getRound().broadcast(ChatColor.DARK_PURPLE + Main.locale
				.getMessage("round-starting"));
	}

	@EventHandler
	public void onPlayerJoinMinigameRound(PlayerJoinMinigameRoundEvent e){
		if (!ScoreManager.sbManagers.containsKey(e.getRound().getArena()))
			ScoreManager.sbManagers.put(e.getRound().getArena(), new ScoreManager(e.getRound().getArena()));
		File f = new File(Main.plugin.getDataFolder(), "bans.yml");
		YamlConfiguration y = new YamlConfiguration();
		try {
			y.load(f);
			if (y.isSet(e.getPlayer().getName())){
				int unbanTime = y.getInt(e.getPlayer().getName());
				if (unbanTime <= System.currentTimeMillis() / 1000){
					y.set(e.getPlayer().getName(), null);
					y.save(f);
					if (Variables.VERBOSE_LOGGING)
						Main.mg.log(e.getPlayer().getName() + "'s ban has been lifted", LogLevel.INFO);
				}
				else {
					String m = ChatColor.DARK_PURPLE + "[TTT] ";
					if (unbanTime == -1)
						m += Main.locale.getMessage("karma-permaban");
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(unbanTime * 1000);
					String year = Integer.toString(cal.get(Calendar.YEAR) + 1);
					String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
					String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
					String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
					String min = Integer.toString(cal.get(Calendar.MINUTE));
					String sec = Integer.toString(cal.get(Calendar.SECOND));
					m += Main.locale.getMessage("karma-ban") + " " +
							hour + ":" + min + ":" + sec + " on " + month + "/" + day + "/" +
							year + ".";
					e.getPlayer().getBukkitPlayer().sendMessage(m);
					e.getPlayer().removeFromRound();
					return;
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			Main.mg.log("Failed to load bans from disk!", LogLevel.WARNING);
		}
		
		e.getPlayer().getBukkitPlayer().setHealth(e.getPlayer().getBukkitPlayer().getMaxHealth());
		
		String addition = "";
		@SuppressWarnings("static-access")
		UUID uuid = Main.mg.getOnlineUUIDs().get(e.getPlayer().getName());
		if (Main.creator.contains(uuid))
			addition = ", " + ChatColor.DARK_RED + Main.locale.getMessage("creator") + "," + ChatColor.DARK_PURPLE;
		if (Main.alpha.contains(uuid) && Main.translators.contains(uuid))
			addition += ", " + ChatColor.DARK_RED + Main.locale.getMessage("alpha-tester") + ", " +
					Main.locale.getMessage("translator") + "," + ChatColor.DARK_PURPLE;
		else if (Main.testers.contains(uuid) && Main.translators.contains(uuid))
			addition += ", " + ChatColor.DARK_RED + Main.locale.getMessage("tester") + ", " +
					Main.locale.getMessage("translator") + "," + ChatColor.DARK_PURPLE;
		else if (Main.alpha.contains(uuid)){
			addition += ", " + ChatColor.DARK_RED + Main.locale.getMessage("alpha-tester") + "," + ChatColor.DARK_PURPLE;
		}
		else if (Main.testers.contains(uuid)){
			addition += ", " + ChatColor.DARK_RED + Main.locale.getMessage("tester") + "," +
					ChatColor.DARK_PURPLE;
		}
		else if (Main.translators.contains(uuid)){
			addition += ", " + ChatColor.DARK_RED + Main.locale.getMessage("translator") + "," +
					ChatColor.DARK_PURPLE;
		}
		Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[TTT] " + e.getPlayer().getName() + addition + " " +
				Main.locale.getMessage("joined-map") + " \"" + e.getRound().getArena() + "\"");

		e.getPlayer().getBukkitPlayer().sendMessage(ChatColor.GREEN + Main.locale.getMessage("success-join") + " " + e.getRound().getArena());
	}

	@EventHandler
	public void onPlayerLeaveMinigameRoundEvent(PlayerLeaveMinigameRoundEvent e){
		e.getPlayer().getBukkitPlayer().setScoreboard(Main.plugin.getServer().getScoreboardManager().getNewScoreboard());
		KarmaManager.saveKarma((TTTPlayer)e.getPlayer());
		((TTTPlayer)e.getPlayer()).setDisplayKarma(((TTTPlayer)e.getPlayer()).getKarma());
		if (!e.getRound().hasEnded())
				e.getRound().broadcast(ChatColor.DARK_PURPLE + e.getPlayer().getName() + " " +
						Main.locale.getMessage("left-game").replace("%", e.getPlayer().getArena()));
	}

	@SuppressWarnings({"deprecation"})
	@EventHandler
	public void onRoundTick(MinigameRoundTickEvent e){

		// manage scoreboards
		ScoreManager.sbManagers.get(e.getRound().getArena()).manage();

		if (e.getRound().getStage() == Stage.PREPARING){
			if((e.getRound().getRemainingTime() % 10) == 0 && e.getRound().getRemainingTime() > 0){
				e.getRound().broadcast(ChatColor.DARK_PURPLE + Main.locale.getMessage("begin")
						.replace("%", e.getRound().getRemainingTime() + " " + Main.locale.getMessage("seconds") + "!"));
			}
			else if (e.getRound().getRemainingTime() > 0 && e.getRound().getRemainingTime() < 10){
				e.getRound().broadcast(ChatColor.DARK_PURPLE + Main.locale.getMessage("begin")
						.replace("%", e.getRound().getRemainingTime() + " " + Main.locale.getMessage("seconds") + "!"));
			}
			else if (e.getRound().getRemainingTime() == 0){
				int players = e.getRound().getPlayers().size();
				int traitorNum = 0;
				int limit = (int)(players * Variables.TRAITOR_RATIO);
				if (limit == 0)
					limit = 1;
				List<String> innocents = new ArrayList<String>();
				List<String> traitors = new ArrayList<String>();
				List<String> detectives = new ArrayList<String>();
				for (MGPlayer p : e.getRound().getPlayerList()){
					innocents.add(p.getName());
					p.getBukkitPlayer().sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("begun"));
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
				int dLimit = (int)(players * Variables.DETECTIVE_RATIO);
				if (players >= Variables.MINIMUM_PLAYERS_FOR_DETECTIVE && dLimit == 0)
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
				cbMeta.setDisplayName("§5" + Main.locale.getMessage("crowbar"));
				crowbar.setItemMeta(cbMeta);
				ItemStack gun = new ItemStack(Material.ANVIL, 1);
				ItemMeta gunMeta = crowbar.getItemMeta();
				gunMeta.setDisplayName("§5" + Main.locale.getMessage("gun"));
				gun.setItemMeta(gunMeta);
				ItemStack ammo = new ItemStack(Material.ARROW, 28);
				ItemStack dnaScanner = new ItemStack(Material.COMPASS, 1);
				ItemMeta dnaMeta = dnaScanner.getItemMeta();
				dnaMeta.setDisplayName("§1" + Main.locale.getMessage("dna-scanner"));
				dnaScanner.setItemMeta(dnaMeta);
				for (String s : innocents){
					Player pl = Main.plugin.getServer().getPlayer(s);
					TTTPlayer t = (TTTPlayer)Main.mg.getMGPlayer(s);
					if (pl != null && t != null){
						t.setTeam("Innocent");
						pl.sendMessage(ChatColor.DARK_GREEN + Main.locale.getMessage("you-are-innocent"));
						pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo});
						pl.setHealth(20);
						pl.setFoodLevel(20);
					}
				}
				for (String s : traitors){
					Player pl = Main.plugin.getServer().getPlayer(s);
					TTTPlayer t = (TTTPlayer)Main.mg.getMGPlayer(s);
					if (pl != null && t != null){
						t.setTeam("Traitor");
						pl.sendMessage(ChatColor.DARK_RED + Main.locale.getMessage("you-are-traitor"));
						if (traitors.size() > 1){
							pl.sendMessage(ChatColor.DARK_RED + Main.locale.getMessage("allies"));
							for (String tr : traitors){
								if (!tr.equals(s))
									pl.sendMessage("- " + t.getName());
							}
						}
						else
							pl.sendMessage(ChatColor.DARK_RED + Main.locale.getMessage("alone"));
						pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo});
						pl.setHealth(20);
						pl.setFoodLevel(20);
					}
				}
				for (String s : detectives){
					Player pl = Main.plugin.getServer().getPlayer(s);
					TTTPlayer t = (TTTPlayer)Main.mg.getMGPlayer(s);
					if (pl != null && t != null){
						t.setTeam("Innocent");
						t.setMetadata("detective", true);
						pl.sendMessage(ChatColor.BLUE + Main.locale.getMessage("you-are-detective"));
						pl.getInventory().addItem(new ItemStack[]{crowbar, gun, ammo, dnaScanner});
						pl.setHealth(20);
						pl.setFoodLevel(20);
					}
				}

				if (Variables.DAMAGE_REDUCTION){
					for (MGPlayer mp : e.getRound().getPlayerList()){
						TTTPlayer t = (TTTPlayer)mp;
						t.calculateDamageReduction();
						String percentage = Main.locale.getMessage("full");
						if (t.getDamageReduction() < 1)
							percentage =
							Integer.toString((int)(t.getDamageReduction() * 100)) + "%";
						t.getBukkitPlayer().sendMessage(ChatColor.DARK_PURPLE +
								Main.locale.getMessage("karma-damage")
								.replace("%", Integer.toString(t.getKarma()))
								.replace("&", percentage));
					}
				}
			}
		}
		else if (e.getRound().getStage() == Stage.PLAYING){
			// check if game is over
			boolean iLeft = false;
			boolean tLeft = false;
			for (MGPlayer p : e.getRound().getPlayerList()){
				if (!tLeft || !iLeft){
					if (!p.isSpectating()){
						if (!iLeft && !p.getTeam().equals("Traitor"))
							iLeft = true;
						if (!tLeft && p.getTeam().equals("Traitor"))
							tLeft = true;
					}
				}
				else
					break;

				if (p.hasMetadata("detective") && p.getRound().getTime() % Variables.SCANNER_CHARGE_TIME == 0){ // manage DNA Scanners every n seconds
					Player tracker = Main.plugin.getServer().getPlayer(p.getName());
					if (p.hasMetadata("tracking")){
						Player killer = Main.plugin.getServer().getPlayer((String)p.getMetadata("tracking"));
						if (killer != null && Main.mg.isPlayer((String)p.getMetadata("tracking")))
							tracker.setCompassTarget(killer.getLocation());
						else {
							tracker.sendMessage(ChatColor.DARK_PURPLE +
									"The player you're tracking has left the round!");
							p.removeMetadata("tracking");
							tracker.setCompassTarget(
									Bukkit.getWorlds().get(1).getSpawnLocation());
						}
					}
					else {
						Random r = new Random();
						tracker.setCompassTarget(new Location(tracker.getWorld(),
								tracker.getLocation().getX() + r.nextInt(10) - 5,
								tracker.getLocation().getY(),
								tracker.getLocation().getZ() + r.nextInt(10) - 5));
					}
				}
			}
			if (!(tLeft && iLeft)){
				e.getRound().setMetadata("t-victory", tLeft);
				e.getRound().end();
				return;
			}

			Round r = e.getRound();
			int rTime = r.getRemainingTime();
			if (rTime % 60 == 0 && rTime >= 60){
				r.broadcast(ChatColor.DARK_PURPLE + Integer.toString(rTime / 60) +
						" " + Main.locale.getMessage("minutes") + " " +
						Main.locale.getMessage("left"));
			}
			else if (rTime % 10 == 0 && rTime > 10 && rTime < 60){
				r.broadcast(ChatColor.DARK_PURPLE + Integer.toString(rTime) + " " +
						Main.locale.getMessage("seconds") + " " +
						Main.locale.getMessage("left"));
			}
			else if (rTime < 10 && rTime > 0){
				r.broadcast(ChatColor.DARK_PURPLE + Integer.toString(rTime) + " " +
						Main.locale.getMessage("seconds") + " " +
						Main.locale.getMessage("left"));
			}
		}
	}

	@EventHandler
	public void onMinigameRoundEnd(MinigameRoundEndEvent e){
		List<Body> removeBodies = new ArrayList<Body>();
		List<Body> removeFoundBodies = new ArrayList<Body>();
		for (Body b : Main.bodies){
			removeBodies.add(b);
			if (Main.foundBodies.contains(b))
				removeFoundBodies.add(b);
		}

		for (Body b : removeBodies)
			Main.bodies.remove(b);

		for (Body b : removeFoundBodies)
			Main.foundBodies.remove(b);

		removeBodies.clear();
		removeFoundBodies.clear();

		KarmaManager.allocateKarma(e.getRound());

		if (!e.getRound().hasMetadata("t-victory") || (Boolean)e.getRound().getMetadata("t-victory") == Boolean.FALSE)
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[TTT] " +
					Main.locale.getMessage("innocent-win").replace("%", "\"" + e.getRound().getArena() + "\"") +
					"!");
		else
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "[TTT] " +
					Main.locale.getMessage("traitor-win").replace("%", "\"" + e.getRound().getArena() + "\"") +
					"!");
	}

	@EventHandler
	public void onMGPlayerDeath(MGPlayerDeathEvent e){
		TTTPlayer t = (TTTPlayer)e.getPlayer();
		t.getBukkitPlayer().setHealth(t.getBukkitPlayer().getMaxHealth());
		t.setSpectating(true);
		if (e.getKiller() != null && e.getKiller() instanceof Player){
			if (e.getKiller() instanceof Player){
				// set killer's karma
				TTTPlayer victim = t;
				TTTPlayer killer = (TTTPlayer)Main.mg.getMGPlayer(((Player)(e.getKiller())).getName());
				KarmaManager.handleKillKarma(killer, victim);
				t.setKiller(((Player)e.getKiller()).getName());
			}
			else if (e.getKiller() instanceof Projectile){
				if (((Projectile)e.getKiller()).getShooter() != null &&
						((Projectile)e.getKiller()).getShooter()
						instanceof Player){
					KarmaManager.handleKillKarma((TTTPlayer)Main.mg.getMGPlayer(
							((Player)((Projectile)e.getKiller()).getShooter()).getName()), t);
					t.setKiller(((Player)((Projectile)e.getKiller()).getShooter()).getName());
				}
			}
		}
		Block block = t.getBukkitPlayer().getLocation().getBlock();
		Main.mg.getRollbackManager().logBlockChange(block, e.getPlayer().getArena());
		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
		boolean trapped = false;
		for (BlockFace bf : faces)
			if (block.getRelative(bf).getType() == Material.CHEST){
				trapped = true;
				break;
			}
		block.setType(trapped ? Material.TRAPPED_CHEST : Material.CHEST);
		Chest chest = (Chest)block.getState();
		// player identifier
		ItemStack id = new ItemStack(Material.PAPER, 1);
		ItemMeta idMeta = id.getItemMeta();
		idMeta.setDisplayName(Main.locale.getMessage("id"));
		List<String> idLore = new ArrayList<String>();
		idLore.add(Main.locale.getMessage("body-of"));
		idLore.add(t.getName());
		idMeta.setLore(idLore);
		id.setItemMeta(idMeta);
		// role identifier
		ItemStack ti = new ItemStack(Material.WOOL, 1);
		ItemMeta tiMeta = ti.getItemMeta();
		if (t.hasMetadata("detective")){
			ti.setDurability((short)11);
			tiMeta.setDisplayName("§1" + Main.locale.getMessage("detective"));
			List<String> lore = new ArrayList<String>();
			lore.add(Main.locale.getMessage("detective-id"));
			tiMeta.setLore(lore);
		}
		else if (t.getTeam() == null && t.getTeam().equals("Innocent")){
			ti.setDurability((short)5);
			tiMeta.setDisplayName("§2" + Main.locale.getMessage("innocent"));
			List<String> tiLore = new ArrayList<String>();
			tiLore.add(Main.locale.getMessage("innocent-id"));
			tiMeta.setLore(tiLore);
		}
		else {
			ti.setDurability((short)14);
			tiMeta.setDisplayName("§4" + Main.locale.getMessage("traitor"));
			List<String> lore = new ArrayList<String>();
			lore.add(Main.locale.getMessage("traitor-id"));
			tiMeta.setLore(lore);
		}
		ti.setItemMeta(tiMeta);
		chest.getInventory().addItem(new ItemStack[]{id, ti});
		Main.bodies.add(new Body(t, Location3D.valueOf(block.getLocation()), System.currentTimeMillis()));
	}

}
