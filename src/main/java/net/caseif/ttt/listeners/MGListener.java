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

import net.caseif.ttt.Body;
import net.caseif.ttt.util.helper.ConfigHelper;
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.manager.KarmaManager;
import net.caseif.ttt.manager.ScoreManager;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.MiscUtil;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.event.round.RoundChangeLifecycleStageEvent;
import net.caseif.flint.event.round.RoundEndEvent;
import net.caseif.flint.event.round.RoundTimerTickEvent;
import net.caseif.flint.event.round.challenger.ChallengerJoinRoundEvent;
import net.caseif.flint.event.round.challenger.ChallengerLeaveRoundEvent;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Location3D;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MGListener {

    @Subscribe
    public void onPlayerJoinRound(ChallengerJoinRoundEvent event) {
        //TODO: move checks to when the player is being added to the round by TTT
        File f = new File(TTTCore.getInstance().getDataFolder(), "bans.yml");
        YamlConfiguration y = new YamlConfiguration();
        try {
            y.load(f);
            if (y.isSet(event.getChallenger().getUniqueId().toString())) {
                long unbanTime = y.getLong(event.getChallenger().getUniqueId().toString());
                if (unbanTime != -1 && unbanTime <= System.currentTimeMillis() / 1000L) {
                    MiscUtil.pardon(event.getChallenger().getUniqueId());
                } else {
                    Localizable loc;
                    if (unbanTime == -1) {
                        loc = TTTCore.locale.getLocalizable("info.personal.ban.perm");
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(unbanTime * 1000L);
                        String year = Integer.toString(cal.get(Calendar.YEAR));
                        String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
                        String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                        String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
                        String min = Integer.toString(cal.get(Calendar.MINUTE));
                        String sec = Integer.toString(cal.get(Calendar.SECOND));
                        min = min.length() == 1 ? "0" + min : min;
                        sec = sec.length() == 1 ? "0" + sec : sec;
                        //TODO: localize time/date (UGH)
                        loc = TTTCore.locale.getLocalizable("info.personal.ban.temp.until").withReplacements(
                                hour + ":" + min + ":" + sec + " on " + month + "/" + day + "/" + year + ".");
                    }
                    loc.withPrefix(Color.ERROR.toString());
                    loc.sendTo(Bukkit.getPlayer(event.getChallenger().getUniqueId()));
                    event.getChallenger().removeFromRound();
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            TTTCore.log.warning("Failed to load bans from disk!");
        }

        Bukkit.getPlayer(event.getChallenger().getUniqueId())
                .setHealth(Bukkit.getPlayer(event.getChallenger().getUniqueId()).getMaxHealth());

        KarmaManager.loadKarma(event.getChallenger().getUniqueId());
        event.getChallenger().getMetadata().set("karma",
                KarmaManager.playerKarma.get(event.getChallenger().getUniqueId()));
        event.getChallenger().getMetadata().set("displayKarma",
                KarmaManager.playerKarma.get(event.getChallenger().getUniqueId()));
        Bukkit.getPlayer(event.getChallenger().getUniqueId())
                .setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());

        if (ScoreManager.sbManagers.containsKey(event.getRound().getArena().getId())) {
            ScoreManager.sbManagers.get(event.getRound().getArena().getId()).update(event.getChallenger());
            Bukkit.getPlayer(event.getChallenger().getUniqueId()).setScoreboard(
                    ScoreManager.sbManagers.get(event.getRound().getArena().getId()).innocent
            );
        }

        String addition = "";
        UUID uuid = event.getChallenger().getUniqueId();
        Player pl = Bukkit.getPlayer(uuid);
        assert pl != null;
        if (TTTCore.devs.contains(uuid)) {
            addition += ", " + TTTCore.locale.getLocalizable("fragment.special.dev")
                    .withPrefix(Color.TRAITOR.toString()).localizeFor(pl) + "," + Color.INFO;
        } else if (TTTCore.alpha.contains(uuid) && TTTCore.translators.contains(uuid)) {
            addition += ", " + TTTCore.locale.getLocalizable("fragment.special.tester.alpha")
                    .withPrefix(Color.TRAITOR.toString()).localizeFor(pl) + ", "
                    + TTTCore.locale.getLocalizable("fragment.special.translator").localizeFor(pl) + ","
                    + Color.INFO;
        } else if (TTTCore.testers.contains(uuid) && TTTCore.translators.contains(uuid)) {
            addition += ", " + TTTCore.locale.getLocalizable("fragment.special.tester")
                    .withPrefix(Color.TRAITOR.toString()).localizeFor(pl) + ", "
                    + TTTCore.locale.getLocalizable("fragment.special.translator").localizeFor(pl) + ","
                    + Color.INFO;
        } else if (TTTCore.alpha.contains(uuid)) {
            addition += ", " + TTTCore.locale.getLocalizable("fragment.special.tester.alpha")
                    .withPrefix(Color.TRAITOR.toString()).localizeFor(pl) + "," + Color.INFO;
        } else if (TTTCore.testers.contains(uuid)) {
            addition += ", " + TTTCore.locale.getLocalizable("fragment.special.tester")
                    .withPrefix(Color.TRAITOR.toString()).localizeFor(pl) + "," + Color.INFO;
        } else if (TTTCore.translators.contains(uuid)) {
            addition += ", " + TTTCore.locale.getLocalizable("fragment.special.translator")
                    .withPrefix(Color.TRAITOR.toString()).localizeFor(pl) + "," + Color.INFO;
        }
        TTTCore.locale.getLocalizable("info.global.arena.event.join").withPrefix(Color.INFO.toString())
                .withReplacements(event.getChallenger().getName() + addition,
                        Color.ARENA + event.getRound().getArena().getName() + Color.INFO)
                .broadcast();

        TTTCore.locale.getLocalizable("info.personal.arena.join.success").withPrefix(Color.INFO.toString())
                .withReplacements(Color.ARENA + event.getRound().getArena().getName() + Color.INFO).sendTo(pl);
    }

    @Subscribe
    public void onPlayerLeaveRound(ChallengerLeaveRoundEvent event) {
        Bukkit.getPlayer(event.getChallenger().getUniqueId()).setScoreboard(
                TTTCore.getInstance().getServer().getScoreboardManager().getNewScoreboard()
        );
        Bukkit.getPlayer(event.getChallenger().getUniqueId())
                .setDisplayName(event.getChallenger().getName());
        KarmaManager.saveKarma(event.getChallenger());
        //TODO: determine whether the round is ending (I'll probably tackle this in Flint 1.1)
        MiscUtil.broadcast(event.getRound(), TTTCore.locale.getLocalizable("info.global.arena.event.leave")
                .withPrefix(Color.INFO.toString()).withReplacements(event.getChallenger().getName(),
                        Color.ARENA + event.getChallenger().getRound().getArena().getName()));
        Bukkit.getPlayer(event.getChallenger().getUniqueId())
                .setCompassTarget(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    //TODO: find a place to call this from
    public void onChallengerDeath(Challenger ch, Challenger killer) {
        Player pl = Bukkit.getPlayer(ch.getUniqueId());
        //ch.setPrefix(Config.SB_MIA_PREFIX); //TODO
        pl.setHealth(pl.getMaxHealth());
        ch.setSpectating(true);
        if (ScoreManager.sbManagers.containsKey(ch.getRound().getArena().getId())) {
            ScoreManager.sbManagers.get(ch.getRound().getArena().getId()).update(ch);
        }
        if (killer != null) {
            // set killer's karma
            KarmaManager.handleKillKarma(killer, ch);
            ch.getMetadata().set("killer", killer.getUniqueId());
        }
        Block block = pl.getLocation().getBlock();
        //TttPluginCore.mg.getRollbackManager().logBlockChange(block, ch.getArena()); //TODO (probably Flint 1.1)
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        boolean trapped = false;
        for (BlockFace bf : faces) {
            if (block.getRelative(bf).getType() == Material.CHEST) {
                trapped = true;
                break;
            }
        }
        //TODO: Add check for doors and such
        //TODO: move this code to another method
        block.setType(trapped ? Material.TRAPPED_CHEST : Material.CHEST);
        Chest chest = (Chest) block.getState();
        // player identifier
        ItemStack id = new ItemStack(Material.PAPER, 1);
        ItemMeta idMeta = id.getItemMeta();
        idMeta.setDisplayName(TTTCore.locale.getLocalizable("item.id.name").localize());
        List<String> idLore = new ArrayList<>();
        idLore.add(TTTCore.locale.getLocalizable("corpse.of").withReplacements(ch.getName()).localize());
        idLore.add(ch.getName());
        idMeta.setLore(idLore);
        id.setItemMeta(idMeta);
        // role identifier
        ItemStack ti = new ItemStack(Material.WOOL, 1);
        ItemMeta tiMeta = ti.getItemMeta();
        //TODO: make this DRYer
        if (ch.getMetadata().has(Role.DETECTIVE)) {
            ti.setDurability((short) 11);
            tiMeta.setDisplayName(TTTCore.locale.getLocalizable("fragment.detective")
                    .withPrefix(Color.DETECTIVE.toString()).localize());
            List<String> lore = new ArrayList<>();
            lore.add(TTTCore.locale.getLocalizable("item.id.detective").localize());
            tiMeta.setLore(lore);
        } else if (!MiscUtil.isTraitor(ch)) {
            ti.setDurability((short) 5);
            tiMeta.setDisplayName(TTTCore.locale.getLocalizable("fragment.innocent")
                    .withPrefix(Color.INNOCENT.toString()).localize());
            List<String> tiLore = new ArrayList<>();
            tiLore.add(TTTCore.locale.getLocalizable("item.id.innocent").localize());
            tiMeta.setLore(tiLore);
        } else {
            ti.setDurability((short) 14);
            tiMeta.setDisplayName(TTTCore.locale.getLocalizable("fragment.traitor")
                    .withPrefix(Color.TRAITOR.toString()).localize());
            List<String> lore = new ArrayList<>();
            lore.add(TTTCore.locale.getLocalizable("item.id.traitor").localize());
            tiMeta.setLore(lore);
        }
        ti.setItemMeta(tiMeta);
        chest.getInventory().addItem(id, ti);
        TTTCore.bodies.add(
                new Body(
                        ch.getRound(),
                        new Location3D(block.getX(), block.getY(), block.getZ()),
                        ch.getUniqueId(),
                        killer != null ? killer.getUniqueId() : null,
                        ch.getMetadata().has(Role.DETECTIVE)
                                ? Role.DETECTIVE
                                : (ch.getTeam().isPresent() ? ch.getTeam().get().getId() : null),
                        System.currentTimeMillis()
                )
        );
    }

    @Subscribe
    public void onRoundPrepare(RoundChangeLifecycleStageEvent event) {
        if (event.getStageAfter() == Constants.Stage.PREPARING) {
            MiscUtil.broadcast(event.getRound(), TTTCore.locale.getLocalizable("info.global.round.event.starting")
                    .withPrefix(Color.INFO.toString()));
            if (!ScoreManager.sbManagers.containsKey(event.getRound().getArena().getId())) {
                ScoreManager.sbManagers.put(event.getRound().getArena().getId(), new ScoreManager(event.getRound()));
                for (Challenger ch : event.getRound().getChallengers()) {
                    ScoreManager.sbManagers.get(event.getRound().getArena().getId()).update(ch);
                }
            }
        } else if (event.getStageAfter() == Constants.Stage.PLAYING) {
            startRound(event.getRound());
        }
    }

    //TODO: this method isn't DRY
    @SuppressWarnings("deprecation")
    public void startRound(Round round) {
        int players = round.getChallengers().size();
        int traitorCount = 0;
        int limit = (int) (players * ConfigHelper.TRAITOR_RATIO);
        if (limit == 0) {
            limit = 1;
        }
        List<UUID> innocents = new ArrayList<>();
        List<UUID> traitors = new ArrayList<>();
        List<UUID> detectives = new ArrayList<>();
        for (Challenger ch : round.getChallengers()) {
            innocents.add(ch.getUniqueId());
            TTTCore.locale.getLocalizable("info.global.round.event.started").withPrefix(Color.INFO.toString())
                    .sendTo(Bukkit.getPlayer(ch.getUniqueId()));
        }
        while (traitorCount < limit) {
            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(players);
            UUID traitor = innocents.get(index);
            if (innocents.contains(traitor)) {
                innocents.remove(traitor);
                traitors.add(traitor);
                traitorCount += 1;
            }
        }
        int dLimit = (int) (players * ConfigHelper.DETECTIVE_RATIO);
        if (players >= ConfigHelper.MINIMUM_PLAYERS_FOR_DETECTIVE && dLimit == 0) {
            dLimit += 1;
        }
        int detectiveNum = 0;
        while (detectiveNum < dLimit) {
            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(innocents.size());
            UUID detective = innocents.get(index);
            innocents.remove(detective);
            detectives.add(detective);
            detectiveNum += 1;
        }
        ItemStack crowbar = new ItemStack(ConfigHelper.CROWBAR_ITEM, 1);
        ItemMeta cbMeta = crowbar.getItemMeta();
        cbMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.crowbar.name").localize());
        crowbar.setItemMeta(cbMeta);
        ItemStack gun = new ItemStack(ConfigHelper.GUN_ITEM, 1);
        ItemMeta gunMeta = crowbar.getItemMeta();
        cbMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.gun.name").localize());
        gun.setItemMeta(gunMeta);
        ItemStack ammo = new ItemStack(Material.ARROW, ConfigHelper.INITIAL_AMMO);
        ItemStack dnaScanner = new ItemStack(Material.COMPASS, 1);
        ItemMeta dnaMeta = dnaScanner.getItemMeta();
        cbMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.dna-scanner.name").localize());
        dnaScanner.setItemMeta(dnaMeta);
        for (UUID uuid : innocents) {
            Player pl = Bukkit.getPlayer(uuid);
            Optional<Challenger> challenger = TTTCore.mg.getChallenger(uuid);
            if (pl != null && challenger.isPresent()) {
                challenger.get().getRound().getOrCreateTeam(Role.INNOCENT).addChallenger(challenger.get());
                TTTCore.locale.getLocalizable("info.personal.status.role.innocent")
                        .withPrefix(Color.INNOCENT.toString()).sendTo(pl);
                MiscUtil.sendStatusTitle(pl, Role.INNOCENT);
                pl.getInventory().addItem(crowbar, gun, ammo);
                pl.setHealth(20);
                pl.setFoodLevel(20);
                if (ScoreManager.sbManagers.containsKey(round.getArena().getId())) {
                    pl.setScoreboard(ScoreManager.sbManagers.get(round.getArena().getId()).innocent);
                    ScoreManager.sbManagers.get(round.getArena().getId()).update(challenger.get());
                }
            }
        }
        for (UUID uuid : traitors) {
            Player pl = TTTCore.getInstance().getServer().getPlayer(uuid);
            Optional<Challenger> challenger = TTTCore.mg.getChallenger(uuid);
            if (pl != null && challenger != null) {
                challenger.get().getRound().getOrCreateTeam(Role.TRAITOR).addChallenger(challenger.get());
                TTTCore.locale.getLocalizable(traitors.size() > 1
                        ? "info.personal.status.role.traitor"
                        : "info.personal.status.role.traitor.alone").withPrefix(Color.TRAITOR.toString())
                        .sendTo(pl);
                MiscUtil.sendStatusTitle(pl, Role.TRAITOR);
                if (traitors.size() > 1) {
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor.allies")
                            .withPrefix(Color.TRAITOR.toString()).sendTo(pl);
                    for (UUID tr : traitors) {
                        if (!tr.equals(uuid)) {
                            pl.sendMessage(Color.TRAITOR + "- " + tr);
                        }
                    }
                }
                pl.getInventory().addItem(crowbar, gun, ammo);
                pl.setHealth(20);
                pl.setFoodLevel(20);
                if (ScoreManager.sbManagers.containsKey(round.getArena().getId())) {
                    pl.setScoreboard(ScoreManager.sbManagers.get(round.getArena().getId()).traitor);
                    ScoreManager.sbManagers.get(round.getArena().getId()).update(challenger.get());
                }
            }
        }
        for (UUID uuid : detectives) {
            Player pl = TTTCore.getInstance().getServer().getPlayer(uuid);
            Optional<Challenger> challenger = TTTCore.mg.getChallenger(uuid);
            if (pl != null && challenger.isPresent()) {
                // detectives are technically innocents so we put them on the innocent team
                challenger.get().getRound().getOrCreateTeam(Role.INNOCENT).addChallenger(challenger.get());
                challenger.get().getMetadata().set(Role.DETECTIVE, true);
                TTTCore.locale.getLocalizable("info.personal.status.role.detective")
                        .withPrefix(Color.DETECTIVE.toString()).sendTo(pl);
                MiscUtil.sendStatusTitle(pl, Role.DETECTIVE);
                pl.getInventory().addItem(crowbar, gun, ammo, dnaScanner);
                pl.setHealth(20);
                pl.setFoodLevel(20);
                if (ScoreManager.sbManagers.containsKey(round.getArena().getId())) {
                    pl.setScoreboard(ScoreManager.sbManagers.get(round.getArena().getId()).innocent);
                    ScoreManager.sbManagers.get(round.getArena().getId()).update(challenger.get());
                }
            }
        }

        for (Challenger ch : round.getChallengers()) {
            if (ConfigHelper.DAMAGE_REDUCTION) {
                Player pl = Bukkit.getPlayer(ch.getUniqueId());
                KarmaManager.calculateDamageReduction(ch);
                String percentage;
                if (ch.getMetadata().has("damageRed") && ch.getMetadata().<Double>get("damageRed").get() < 1) {
                    percentage = (int) (ch.getMetadata().<Double>get("damageRed").get() * 100) + "%";
                } else {
                    percentage = TTTCore.locale.getLocalizable("fragment.full")
                            .withPrefix(Color.INFO.toString()).localizeFor(pl);
                }
                TTTCore.locale.getLocalizable("info.personal.status.karma-damage")
                        .withPrefix(Color.INFO.toString())
                        .withReplacements(ch.getMetadata().<Integer>get("karma").get() + "", percentage).sendTo(pl);
            }
        }
    }

    @SuppressWarnings({"deprecation"})
    @Subscribe
    public void onRoundTick(RoundTimerTickEvent event) {
        if (event.getRound().getLifecycleStage() == Constants.Stage.PREPARING) {
            if ((event.getRound().getRemainingTime() % 10) == 0) {
                for (Challenger ch : event.getRound().getChallengers()) {
                    Player pl = Bukkit.getPlayer(ch.getUniqueId());
                    assert pl != null;
                    TTTCore.locale.getLocalizable("info.global.round.status.starting.time")
                            .withPrefix(Color.INFO.toString())
                            .withReplacements(
                                    TTTCore.locale.getLocalizable("fragment.seconds").localizeFor(pl),
                                    event.getRound().getRemainingTime() + "")
                            .sendTo(pl);
                }
            }
        } else if (event.getRound().getLifecycleStage() == Constants.Stage.PLAYING) {
            // check if game is over
            boolean iLeft = false;
            boolean tLeft = false;
            for (Challenger ch : event.getRound().getChallengers()) {
                if (!tLeft || !iLeft) {
                    if (!ch.isSpectating()) {
                        if (!iLeft && !MiscUtil.isTraitor(ch)) {
                            iLeft = true;
                        }
                        if (!tLeft && MiscUtil.isTraitor(ch)) {
                            tLeft = true;
                        }
                    }
                } else {
                    break;
                }

                // manage DNA Scanners every n seconds
                if (ch.getMetadata().has(Role.DETECTIVE)
                        && ch.getRound().getTime() % ConfigHelper.SCANNER_CHARGE_TIME == 0) {
                    Player tracker = TTTCore.getInstance().getServer().getPlayer(ch.getName());
                    if (ch.getMetadata().has("tracking")) {
                        Player killer = TTTCore.getInstance().getServer()
                                .getPlayer(ch.getMetadata().<UUID>get("tracking").get());
                        if (killer != null
                                && TTTCore.mg.getChallenger(ch.getMetadata().<UUID>get("tracking").get()).isPresent()) {
                            tracker.setCompassTarget(killer.getLocation());
                        } else {
                            TTTCore.locale.getLocalizable("error.round.trackee-left")
                                    .withPrefix(Color.INFO.toString()).sendTo(tracker);
                            ch.getMetadata().remove("tracking");
                            tracker.setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());
                        }
                    } else {
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
                event.getRound().getMetadata().set("t-victory", tLeft);
                event.getRound().end();
                return;
            }

            Round r = event.getRound();
            long rTime = r.getRemainingTime();
            if ((rTime % 60 == 0 && rTime >= 60) || (rTime % 10 == 0 && rTime > 10 && rTime < 60)) {
                for (Challenger ch : r.getChallengers()) {
                    Player pl = Bukkit.getPlayer(ch.getUniqueId());
                    TTTCore.locale.getLocalizable("info.global.round.status.time.remaining")
                            .withPrefix(Color.INFO.toString())
                            .withReplacements(TTTCore.locale
                                    .getLocalizable(rTime < 60 ? "fragment.minutes" : "fragment.seconds")
                                    .withReplacements(Long.toString(rTime / 60)).localizeFor(pl))
                            .sendTo(pl);
                }
            }

            if (!ScoreManager.sbManagers.containsKey(event.getRound().getArena().getId())) {
                ScoreManager.sbManagers.put(event.getRound().getArena().getId(), new ScoreManager(event.getRound()));
            }
        }
    }

    @Subscribe
    public void onRoundEnd(RoundEndEvent event) {
        List<Body> removeBodies = new ArrayList<Body>();
        List<Body> removeFoundBodies = new ArrayList<Body>();
        for (Body b : TTTCore.bodies) {
            removeBodies.add(b);
            if (TTTCore.foundBodies.contains(b)) {
                removeFoundBodies.add(b);
            }
        }

        for (Body b : removeBodies) {
            TTTCore.bodies.remove(b);
        }

        for (Body b : removeFoundBodies) {
            TTTCore.foundBodies.remove(b);
        }

        removeBodies.clear();
        removeFoundBodies.clear();

        KarmaManager.allocateKarma(event.getRound());

        boolean tVic = event.getRound().getMetadata().has("t-victory")
                && event.getRound().getMetadata().<Boolean>get("t-victory").get();

        TTTCore.locale.getLocalizable("info.global.round.event.end." + (tVic ? Role.TRAITOR : Role.INNOCENT))
                .withPrefix(Color.INNOCENT.toString())
                .withReplacements(Color.ARENA + event.getRound().getArena().getName()).broadcast();
        MiscUtil.sendVictoryTitle(event.getRound(), tVic);

        for (Entity ent : Bukkit.getWorld(event.getRound().getArena().getWorld()).getEntities()) {
            if (ent.getType() == EntityType.ARROW) {
                ent.remove();
            }
        }
        ScoreManager.sbManagers.remove(event.getRound().getArena().getId());
    }

    @Subscribe
    public void onStageChange(RoundChangeLifecycleStageEvent event) {
        if ((event.getStageBefore() == Constants.Stage.PREPARING || event.getStageBefore() == Constants.Stage.PLAYING)
                && (event.getStageAfter() == Constants.Stage.PREPARING)) {
            ScoreManager sm = ScoreManager.sbManagers.get(event.getRound().getArena().getId());
            sm.iObj.unregister();
            sm.tObj.unregister();
            ScoreManager.sbManagers.remove(event.getRound().getArena().getId());
            for (Challenger ch : event.getRound().getChallengers()) {
                Bukkit.getPlayer(ch.getUniqueId()).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                if (ch.getTeam().isPresent()) {
                    ch.getTeam().get().removeChallenger(ch);
                }
            }
        }
    }

}
