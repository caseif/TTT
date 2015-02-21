/*
 * The MIT License (MIT)
 *
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
package net.caseif.ttt.listeners;

import static net.caseif.ttt.util.Constants.ARENA_COLOR;
import static net.caseif.ttt.util.Constants.DETECTIVE_COLOR;
import static net.caseif.ttt.util.Constants.ERROR_COLOR;
import static net.caseif.ttt.util.Constants.INFO_COLOR;
import static net.caseif.ttt.util.Constants.INNOCENT_COLOR;
import static net.caseif.ttt.util.Constants.TRAITOR_COLOR;
import static net.caseif.ttt.util.MiscUtil.getMessage;

import net.caseif.ttt.Body;
import net.caseif.ttt.Config;
import net.caseif.ttt.Main;
import net.caseif.ttt.managers.KarmaManager;
import net.caseif.ttt.managers.ScoreManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.amigocraft.mglib.api.Location3D;
import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.api.Minigame;
import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.api.Stage;
import net.amigocraft.mglib.event.player.MGPlayerDeathEvent;
import net.amigocraft.mglib.event.player.PlayerJoinMinigameRoundEvent;
import net.amigocraft.mglib.event.player.PlayerLeaveMinigameRoundEvent;
import net.amigocraft.mglib.event.round.MinigameRoundEndEvent;
import net.amigocraft.mglib.event.round.MinigameRoundPrepareEvent;
import net.amigocraft.mglib.event.round.MinigameRoundStageChangeEvent;
import net.amigocraft.mglib.event.round.MinigameRoundStartEvent;
import net.amigocraft.mglib.event.round.MinigameRoundTickEvent;

import net.caseif.ttt.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MGListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoinRound(PlayerJoinMinigameRoundEvent e) {
		File f = new File(Main.plugin.getDataFolder(), "bans.yml");
		YamlConfiguration y = new YamlConfiguration();
		try {
			UUID uuid = Minigame.getOnlineUUIDs().get(e.getPlayer().getName());
			if (uuid == null) {
				// this bit is so it won't break when I'm testing, but offline servers will still get screwed up
				List<String> testAccounts = Arrays.asList("testing123", "testing456", "testing789");
				if (testAccounts.contains(e.getPlayer().getName().toLowerCase())) {
					uuid = e.getPlayer().getBukkitPlayer().getUniqueId();
				}
			}
			y.load(f);
			if (y.isSet(uuid.toString())) {
				long unbanTime = y.getLong(uuid.toString());
				if (unbanTime != -1 && unbanTime <= System.currentTimeMillis() / 1000) {
					MiscUtil.pardon(uuid);
				}
				else {
					String m;
					if (unbanTime == -1) {
						m = getMessage("info.personal.ban.perm", ERROR_COLOR);
					}
					else {
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(unbanTime * 1000);
						String year = Integer.toString(cal.get(Calendar.YEAR));
						String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
						String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
						String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
						String min = Integer.toString(cal.get(Calendar.MINUTE));
						String sec = Integer.toString(cal.get(Calendar.SECOND));
						min = min.length() == 1 ? "0" + min : min;
						sec = sec.length() == 1 ? "0" + sec : sec;
						m = getMessage(
								"info.personal.ban.temp.until",
								ERROR_COLOR,
								hour + ":" + min + ":" + sec + " on " + month + "/" + day + "/" + year + "."
						);
					}
					e.getPlayer().getBukkitPlayer().sendMessage(m);
					e.setCancelled(true);
					return;
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Main.mg.log("Failed to load bans from disk!", LogLevel.WARNING);
		}

		e.getPlayer().getBukkitPlayer().setHealth(e.getPlayer().getBukkitPlayer().getMaxHealth());

		KarmaManager.loadKarma(e.getPlayer().getName());
		e.getPlayer().setMetadata("karma", KarmaManager.playerKarma.get(e.getPlayer().getName()));
		e.getPlayer().setMetadata("displayKarma", KarmaManager.playerKarma.get(e.getPlayer().getName()));
		e.getPlayer().getBukkitPlayer().setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());

		if (ScoreManager.sbManagers.containsKey(e.getRound().getArena())) {
			ScoreManager.sbManagers.get(e.getRound().getArena()).update(e.getPlayer());
			e.getPlayer().getBukkitPlayer().setScoreboard(
					ScoreManager.sbManagers.get(e.getRound().getArena()).innocent
			);
		}

		String addition = "";
		@SuppressWarnings("static-access")
		UUID uuid = Main.mg.getOnlineUUIDs().get(e.getPlayer().getName());
		if (Main.devs.contains(uuid)) {
			addition = ", " + getMessage("fragment.special.dev", TRAITOR_COLOR, false) + "," + INFO_COLOR;
		}
		if (Main.alpha.contains(uuid) && Main.translators.contains(uuid)) {
			addition += ", " +
					getMessage("fragment.special.tester.alpha", TRAITOR_COLOR, false) + ", " +
					Main.locale.getMessage("fragment.special.translator") + "," + INFO_COLOR;
		}
		else if (Main.testers.contains(uuid) && Main.translators.contains(uuid)) {
			addition += ", " + getMessage("fragment.special.tester", TRAITOR_COLOR, false) + ", " +
					Main.locale.getMessage("fragment.special.translator") + "," + INFO_COLOR;
		}
		else if (Main.alpha.contains(uuid)) {
			addition += ", " + getMessage("fragment.special.tester.alpha", TRAITOR_COLOR, false) + "," + INFO_COLOR;
		}
		else if (Main.testers.contains(uuid)) {
			addition += ", " + getMessage("fragment.special.tester", TRAITOR_COLOR, false) + "," + INFO_COLOR;
		}
		else if (Main.translators.contains(uuid)) {
			addition += ", " + getMessage("fragment.special.translator", TRAITOR_COLOR, false) + "," + INFO_COLOR;
		}
		Bukkit.broadcastMessage(getMessage("info.global.arena.event.join", INFO_COLOR,
				e.getPlayer().getName() + addition, ARENA_COLOR + e.getRound().getDisplayName()));

		e.getPlayer().getBukkitPlayer().sendMessage(getMessage("info.personal.arena.join.success", INFO_COLOR,
				ARENA_COLOR + e.getRound().getDisplayName()));
	}

	@EventHandler
	public void onPlayerLeaveRound(PlayerLeaveMinigameRoundEvent e) {
		e.getPlayer().getBukkitPlayer().setScoreboard(
				Main.plugin.getServer().getScoreboardManager().getNewScoreboard()
		);
		e.getPlayer().getBukkitPlayer().setDisplayName(e.getPlayer().getBukkitPlayer().getName());
		KarmaManager.saveKarma(e.getPlayer());
		(e.getPlayer()).setMetadata("displayKarma", e.getPlayer().getMetadata("karma"));
		if (e.getRound().getStage() != Stage.RESETTING) {
			e.getRound().broadcast(getMessage("info.global.arena.event.leave", INFO_COLOR,
					e.getPlayer().getName(), ARENA_COLOR + e.getPlayer().getRound().getDisplayName()));
		}
		e.getPlayer().getBukkitPlayer().setCompassTarget(Bukkit.getWorlds().get(0).getSpawnLocation());
	}

	@EventHandler
	public void onMGPlayerDeath(MGPlayerDeathEvent e) {
		e.getPlayer().setPrefix(Config.SB_MIA_PREFIX);
		e.getPlayer().getBukkitPlayer().setHealth(e.getPlayer().getBukkitPlayer().getMaxHealth());
		e.getPlayer().setSpectating(true);
		if (ScoreManager.sbManagers.containsKey(e.getPlayer().getArena())) {
			ScoreManager.sbManagers.get(e.getPlayer().getArena()).update(e.getPlayer());
		}
		if (e.getKiller() != null && e.getKiller() instanceof Player) {
			// set killer's karma
			MGPlayer killer = Main.mg.getMGPlayer((e.getKiller()).getName());
			KarmaManager.handleKillKarma(killer, e.getPlayer());
			e.getPlayer().setMetadata("killer", e.getKiller().getName());
		}
		Block block = e.getPlayer().getBukkitPlayer().getLocation().getBlock();
		Main.mg.getRollbackManager().logBlockChange(block, e.getPlayer().getArena());
		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
		boolean trapped = false;
		for (BlockFace bf : faces) {
			if (block.getRelative(bf).getType() == Material.CHEST) {
				trapped = true;
				break;
			}
		}
		//TODO: Add check for doors and such
		block.setType(trapped ? Material.TRAPPED_CHEST : Material.CHEST);
		Chest chest = (Chest)block.getState();
		// player identifier
		ItemStack id = new ItemStack(Material.PAPER, 1);
		ItemMeta idMeta = id.getItemMeta();
		idMeta.setDisplayName(getMessage("item.id.name", ChatColor.RESET, false));
		List<String> idLore = new ArrayList<String>();
		idLore.add(getMessage("corpse.of", ChatColor.RESET, false, e.getPlayer().getName()));
		idLore.add(e.getPlayer().getName());
		idMeta.setLore(idLore);
		id.setItemMeta(idMeta);
		// role identifier
		ItemStack ti = new ItemStack(Material.WOOL, 1);
		ItemMeta tiMeta = ti.getItemMeta();
		if (e.getPlayer().hasMetadata("Detective")) {
			ti.setDurability((short)11);
			tiMeta.setDisplayName(getMessage("fragment.detective", DETECTIVE_COLOR, false));
			List<String> lore = new ArrayList<String>();
			lore.add(Main.locale.getMessage("item.id.detective"));
			tiMeta.setLore(lore);
		}
		else if (e.getPlayer().getTeam() == null || e.getPlayer().getTeam().equals("Innocent")) {
			ti.setDurability((short)5);
			tiMeta.setDisplayName(getMessage("fragment.innocent", INNOCENT_COLOR, false));
			List<String> tiLore = new ArrayList<String>();
			tiLore.add(getMessage("item.id.innocent", ChatColor.RESET, false));
			tiMeta.setLore(tiLore);
		}
		else {
			ti.setDurability((short)14);
			tiMeta.setDisplayName(getMessage("fragment.traitor", TRAITOR_COLOR, false));
			List<String> lore = new ArrayList<String>();
			lore.add(getMessage("item.id.traitor", ChatColor.RESET, false));
			tiMeta.setLore(lore);
		}
		ti.setItemMeta(tiMeta);
		chest.getInventory().addItem(id, ti);
		Main.bodies.add(
				new Body(
						e.getPlayer().getName(),
						e.getPlayer().getArena(),
						e.getPlayer().hasMetadata("Detective") ? "Detective" : e.getPlayer().getTeam(),
						Location3D.valueOf(block.getLocation()),
						System.currentTimeMillis()
				)
		);
	}

	@EventHandler
	public void onRoundPrepare(MinigameRoundPrepareEvent e) {
		e.getRound().broadcast(getMessage("info.global.round.event.starting", INFO_COLOR));
		if (!ScoreManager.sbManagers.containsKey(e.getRound().getArena())) {
			ScoreManager.sbManagers.put(e.getRound().getArena(), new ScoreManager(e.getRound().getArena()));
			for (MGPlayer mp : e.getRound().getPlayerList()) {
				ScoreManager.sbManagers.get(e.getRound().getArena()).update(mp);
			}
		}
	}

	@SuppressWarnings({"deprecation"})
	@EventHandler
	public void onRoundStart(MinigameRoundStartEvent e) {
		int players = e.getRound().getPlayers().size();
		int traitorCount = 0;
		int limit = (int)(players * Config.TRAITOR_RATIO);
		if (limit == 0) {
			limit = 1;
		}
		List<String> innocents = new ArrayList<String>();
		List<String> traitors = new ArrayList<String>();
		List<String> detectives = new ArrayList<String>();
		for (MGPlayer p : e.getRound().getPlayerList()) {
			innocents.add(p.getName());
			p.getBukkitPlayer().sendMessage(getMessage("info.global.round.event.started", INFO_COLOR));
		}
		while (traitorCount < limit) {
			Random randomGenerator = new Random();
			int index = randomGenerator.nextInt(players);
			String traitor = innocents.get(index);
			if (innocents.contains(traitor)) {
				innocents.remove(traitor);
				traitors.add(traitor);
				traitorCount += 1;
			}
		}
		int dLimit = (int)(players * Config.DETECTIVE_RATIO);
		if (players >= Config.MINIMUM_PLAYERS_FOR_DETECTIVE && dLimit == 0) {
			dLimit += 1;
		}
		int detectiveNum = 0;
		while (detectiveNum < dLimit) {
			Random randomGenerator = new Random();
			int index = randomGenerator.nextInt(innocents.size());
			String detective = innocents.get(index);
			innocents.remove(detective);
			detectives.add(detective);
			detectiveNum += 1;
		}
		ItemStack crowbar = new ItemStack(Config.CROWBAR_ITEM, 1);
		ItemMeta cbMeta = crowbar.getItemMeta();
		cbMeta.setDisplayName(getMessage("item.crowbar.name", INFO_COLOR, false));
		crowbar.setItemMeta(cbMeta);
		ItemStack gun = new ItemStack(Config.GUN_ITEM, 1);
		ItemMeta gunMeta = crowbar.getItemMeta();
		gunMeta.setDisplayName(getMessage("item.gun.name", INFO_COLOR, false));
		gun.setItemMeta(gunMeta);
		ItemStack ammo = new ItemStack(Material.ARROW, Config.INITIAL_AMMO);
		ItemStack dnaScanner = new ItemStack(Material.COMPASS, 1);
		ItemMeta dnaMeta = dnaScanner.getItemMeta();
		dnaMeta.setDisplayName(getMessage("item.dna-scanner.name", DETECTIVE_COLOR, false));
		dnaScanner.setItemMeta(dnaMeta);
		for (String s : innocents) {
			Player pl = Main.plugin.getServer().getPlayer(s);
			MGPlayer player = Main.mg.getMGPlayer(s);
			if (pl != null && player != null) {
				player.setTeam("Innocent");
				pl.sendMessage(getMessage("info.personal.status.role.innocent", INNOCENT_COLOR, false));
				MiscUtil.sendStatusTitle(pl, "innocent");
				pl.getInventory().addItem(crowbar, gun, ammo);
				pl.setHealth(20);
				pl.setFoodLevel(20);
				if (ScoreManager.sbManagers.containsKey(e.getRound().getArena())) {
					pl.setScoreboard(ScoreManager.sbManagers.get(e.getRound().getArena()).innocent);
					ScoreManager.sbManagers.get(e.getRound().getArena()).update(player);
				}
			}
		}
		for (String s : traitors) {
			Player pl = Main.plugin.getServer().getPlayer(s);
			MGPlayer player = Main.mg.getMGPlayer(s);
			if (pl != null && player != null) {
				player.setTeam("Traitor");
				pl.sendMessage(getMessage(traitors.size() > 1 ?
								"info.personal.status.role.traitor" :
								"info.personal.status.role.traitor.alone",
						TRAITOR_COLOR, false));
				MiscUtil.sendStatusTitle(pl, "traitor");
				if (traitors.size() > 1) {
					pl.sendMessage(getMessage("info.personal.status.role.traitor.allies",
							TRAITOR_COLOR, false));
					for (String tr : traitors) {
						if (!tr.equals(s)) {
							pl.sendMessage(TRAITOR_COLOR + "- " + tr);
						}
					}
				}
				pl.getInventory().addItem(crowbar, gun, ammo);
				pl.setHealth(20);
				pl.setFoodLevel(20);
				if (ScoreManager.sbManagers.containsKey(e.getRound().getArena())) {
					pl.setScoreboard(ScoreManager.sbManagers.get(e.getRound().getArena()).traitor);
					ScoreManager.sbManagers.get(e.getRound().getArena()).update(player);
				}
			}
		}
		for (String s : detectives) {
			Player pl = Main.plugin.getServer().getPlayer(s);
			MGPlayer player = Main.mg.getMGPlayer(s);
			if (pl != null && player != null) {
				player.setTeam("Innocent");
				player.setMetadata("fragment.detective", true);
				pl.sendMessage(getMessage("info.personal.status.role.detective", DETECTIVE_COLOR, false));
				MiscUtil.sendStatusTitle(pl, "detective");
				pl.getInventory().addItem(crowbar, gun, ammo, dnaScanner);
				pl.setHealth(20);
				pl.setFoodLevel(20);
				if (ScoreManager.sbManagers.containsKey(e.getRound().getArena())) {
					pl.setScoreboard(ScoreManager.sbManagers.get(e.getRound().getArena()).innocent);
					ScoreManager.sbManagers.get(e.getRound().getArena()).update(player);
				}
			}
		}

		for (MGPlayer mp : e.getRound().getPlayerList()) {
			if (Config.DAMAGE_REDUCTION) {
				KarmaManager.calculateDamageReduction(mp);
				String percentage = getMessage("fragment.full", INFO_COLOR, false);
				if ((Double)mp.getMetadata("damageRed") < 1) {
					percentage = Integer.toString((int)((Double)mp.getMetadata("damageRed") * 100)) + "%";
				}
				mp.getBukkitPlayer().sendMessage(getMessage("info.personal.status.karma-damage", INFO_COLOR,
						Integer.toString((Integer)mp.getMetadata("karma")), percentage));
			}
		}
	}

	@SuppressWarnings({"deprecation"})
	@EventHandler
	public void onRoundTick(MinigameRoundTickEvent e) {
		if (e.getRound().getStage() == Stage.PREPARING) {
			if (((e.getRound().getRemainingTime() % 10) == 0 ||
					e.getRound().getRemainingTime() < 10) && e.getRound().getRemainingTime() > 0) {
				e.getRound().broadcast(getMessage("info.global.round.status.starting.time", INFO_COLOR,
						getMessage("fragment.seconds", INFO_COLOR, false,
								Integer.toString(e.getRound().getRemainingTime()))));
			}
		}
		else if (e.getRound().getStage() == Stage.PLAYING) {
			// check if game is over
			boolean iLeft = false;
			boolean tLeft = false;
			for (MGPlayer p : e.getRound().getPlayerList()) {
				if (!tLeft || !iLeft) {
					if (!p.isSpectating()) {
						if (!iLeft && !p.getTeam().equals("Traitor")) {
							iLeft = true;
						}
						if (!tLeft && p.getTeam().equals("Traitor")) {
							tLeft = true;
						}
					}
				}
				else {
					break;
				}

				// manage DNA Scanners every n seconds
				if (p.hasMetadata("fragment.detective") && p.getRound().getTime() % Config.SCANNER_CHARGE_TIME == 0) {
					Player tracker = Main.plugin.getServer().getPlayer(p.getName());
					if (p.hasMetadata("tracking")) {
						Player killer = Main.plugin.getServer().getPlayer((String)p.getMetadata("tracking"));
						if (killer != null && Main.mg.isPlayer((String)p.getMetadata("tracking"))) {
							tracker.setCompassTarget(killer.getLocation());
						}
						else {
							tracker.sendMessage(getMessage("error.round.trackee-left", INFO_COLOR));
							p.removeMetadata("tracking");
							tracker.setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());
						}
					}
					else {
						Random r = new Random();
						tracker.setCompassTarget(
								new Location(
										tracker.getWorld(),
										tracker.getLocation().getX() + r.nextInt(10) - 5,
										tracker.getLocation().getY(),
										tracker.getLocation().getZ() + r.nextInt(10) - 5
								)
						);
					}
				}
			}
			if (!(tLeft && iLeft)) {
				e.getRound().setMetadata("t-victory", tLeft);
				e.getRound().end();
				return;
			}

			Round r = e.getRound();
			int rTime = r.getRemainingTime();
			if (rTime % 60 == 0 && rTime >= 60) {
				r.broadcast(getMessage("info.global.round.status.time.remaining", INFO_COLOR, false,
						getMessage("fragment.minutes", INFO_COLOR, false, Integer.toString(rTime / 60))));
			}
			else if ((rTime % 10 == 0 && rTime > 10 && rTime < 60) || (rTime < 10 && rTime > 0)) {
				r.broadcast(getMessage("info.global.round.status.time.remaining", INFO_COLOR, false,
						getMessage("fragment.seconds", INFO_COLOR, false, Integer.toString(rTime))));
			}
			//TODO: this loop is probably an unnecessary artifact but I'm leaving it for now in case removing it causes
			// odd side-effects
			for (MGPlayer mp : e.getRound().getPlayerList()) {
				if (!ScoreManager.sbManagers.containsKey(e.getRound().getArena())) {
					ScoreManager.sbManagers.put(e.getRound().getArena(), new ScoreManager(e.getRound().getArena()));
				}
			}
		}
	}

	@EventHandler
	public void onRoundEnd(MinigameRoundEndEvent e) {
		List<Body> removeBodies = new ArrayList<Body>();
		List<Body> removeFoundBodies = new ArrayList<Body>();
		for (Body b : Main.bodies) {
			removeBodies.add(b);
			if (Main.foundBodies.contains(b)) {
				removeFoundBodies.add(b);
			}
		}

		for (Body b : removeBodies) {
			Main.bodies.remove(b);
		}

		for (Body b : removeFoundBodies) {
			Main.foundBodies.remove(b);
		}

		removeBodies.clear();
		removeFoundBodies.clear();

		KarmaManager.allocateKarma(e.getRound());

		if (!e.getRound().hasMetadata("t-victory") || e.getRound().getMetadata("t-victory") == Boolean.FALSE) {
			Bukkit.broadcastMessage(getMessage("info.global.round.event.end.innocent", INNOCENT_COLOR,
					ARENA_COLOR + e.getRound().getDisplayName()));
			MiscUtil.sendVictoryTitle(e.getRound(), false);
		}
		else {
			Bukkit.broadcastMessage(getMessage("info.global.round.event.end.traitor", TRAITOR_COLOR,
					ARENA_COLOR + e.getRound().getDisplayName()));
			MiscUtil.sendVictoryTitle(e.getRound(), true);
		}
		for (Entity ent : Bukkit.getWorld(e.getRound().getWorld()).getEntities()) {
			if (ent.getType() == EntityType.ARROW) {
				ent.remove();
			}
		}
		ScoreManager.sbManagers.remove(e.getRound().getArena());
	}

	@EventHandler
	public void onStageChange(MinigameRoundStageChangeEvent e) {
		if ((e.getStageBefore() == Stage.PREPARING || e.getStageBefore() == Stage.PLAYING) &&
				(e.getStageAfter() == Stage.PREPARING)) {
			ScoreManager sm = ScoreManager.sbManagers.get(e.getRound().getArena());
			sm.iObj.unregister();
			sm.tObj.unregister();
			ScoreManager.sbManagers.remove(e.getRound().getArena());
			for (MGPlayer mp : e.getRound().getPlayerList()) {
				mp.getBukkitPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				mp.setTeam(null);
			}
		}
	}

}
