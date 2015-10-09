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
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.arena.JoinCommand;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.Constants.Stage;
import net.caseif.ttt.util.MiscUtil;
import net.caseif.ttt.util.helper.ConfigHelper;
import net.caseif.ttt.util.helper.InventoryHelper;
import net.caseif.ttt.util.helper.KarmaHelper;
import net.caseif.ttt.util.helper.LocationHelper;
import net.caseif.ttt.util.helper.RoleHelper;
import net.caseif.ttt.util.helper.TitleHelper;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.event.lobby.PlayerClickLobbySignEvent;
import net.caseif.flint.event.round.RoundChangeLifecycleStageEvent;
import net.caseif.flint.event.round.RoundEndEvent;
import net.caseif.flint.event.round.RoundTimerTickEvent;
import net.caseif.flint.event.round.challenger.ChallengerJoinRoundEvent;
import net.caseif.flint.event.round.challenger.ChallengerLeaveRoundEvent;
import net.caseif.flint.round.Round;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MinigameListener {

    @Subscribe
    public void onChallengerJoinRound(ChallengerJoinRoundEvent event) {
        if (event.getRound().getLifecycleStage() == Stage.PLAYING) {
            event.getChallenger().setSpectating(true);
            event.getChallenger().getMetadata().set(Constants.PlayerTag.PURE_SPECTATOR, true);
        }

        Bukkit.getPlayer(event.getChallenger().getUniqueId())
                .setHealth(Bukkit.getPlayer(event.getChallenger().getUniqueId()).getMaxHealth());

        Bukkit.getPlayer(event.getChallenger().getUniqueId())
                .setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());

        if (ScoreboardManager.get(event.getRound()).isPresent()) {
            ScoreboardManager.get(event.getRound()).get().assignScoreboard(event.getChallenger());
        }

        if (!event.getChallenger().getMetadata().has(Constants.PlayerTag.PURE_SPECTATOR)) {
            if (ScoreboardManager.get(event.getRound()).isPresent()) {
                ScoreboardManager.get(event.getRound()).get().update(event.getChallenger());
            }

            Player pl = Bukkit.getPlayer(event.getChallenger().getUniqueId());
            pl.setGameMode(GameMode.SURVIVAL);
            KarmaHelper.applyKarma(event.getChallenger());

            MiscUtil.broadcast(event.getRound(),
                    TTTCore.locale.getLocalizable("info.global.arena.event.join").withPrefix(Color.INFO)
                            .withReplacements(event.getChallenger().getName() + TTTCore.clh.getContributorString(pl)));

            if (event.getRound().getLifecycleStage() == Stage.WAITING
                    && event.getRound().getChallengers().size() >= ConfigHelper.MINIMUM_PLAYERS) {
                event.getRound().nextLifecycleStage();
            }
        }
    }

    @Subscribe
    public void onChallengerLeaveRound(ChallengerLeaveRoundEvent event) {
        Player pl = Bukkit.getPlayer(event.getChallenger().getUniqueId());
        pl.setScoreboard(
                TTTCore.getInstance().getServer().getScoreboardManager().getNewScoreboard()
        );

        pl.setDisplayName(event.getChallenger().getName());
        pl.setCompassTarget(LocationHelper.convert(event.getReturnLocation()).getWorld().getSpawnLocation());
        pl.setHealth(pl.getMaxHealth());

        if (!event.getRound().getMetadata().has("ending")) { //TODO: temp fix
            if (!event.getChallenger().getMetadata().has(Constants.PlayerTag.PURE_SPECTATOR)) {
                KarmaHelper.saveKarma(event.getChallenger());
                MiscUtil.broadcast(event.getRound(), TTTCore.locale.getLocalizable("info.global.arena.event.leave")
                        .withPrefix(Color.INFO).withReplacements(event.getChallenger().getName(),
                                Color.ARENA + event.getChallenger().getRound().getArena().getName() + Color.INFO));

                if (event.getRound().getLifecycleStage() == Stage.PREPARING
                        && event.getRound().getChallengers().size() <= 1) {
                    event.getRound().setLifecycleStage(Stage.WAITING);
                    MiscUtil.broadcast(event.getRound(),
                            TTTCore.locale.getLocalizable("info.global.round.status.starting.stopped")
                                    .withPrefix(Color.ERROR));
                }
            }
        }
    }

    @Subscribe
    public void onRoundPrepare(RoundChangeLifecycleStageEvent event) {
        if (event.getStageAfter() == Stage.PREPARING) {
            MiscUtil.broadcast(event.getRound(), TTTCore.locale.getLocalizable("info.global.round.event.starting")
                    .withPrefix(Color.INFO));
            ScoreboardManager.getOrCreate(event.getRound());
        } else if (event.getStageAfter() == Stage.PLAYING) {
            startRound(event.getRound());
        }
    }

    @SuppressWarnings("deprecation")
    public void startRound(Round round) {
        RoleHelper.assignRoles(round);
        for (Challenger ch : round.getChallengers()) {
            ScoreboardManager.getOrCreate(round).update(ch);
        }
        InventoryHelper.distributeItems(round);
        ScoreboardManager.getOrCreate(round).assignScoreboards();

        for (Challenger ch : round.getChallengers()) {
            assert ch.getTeam().isPresent();
            Player pl = TTTCore.getInstance().getServer().getPlayer(ch.getUniqueId());
            assert pl != null;

            pl.setHealth(pl.getMaxHealth());
            pl.setFoodLevel(20);

            if (ch.getTeam().get().getId().equals(Role.INNOCENT)) {
                if (ch.getMetadata().has(Role.DETECTIVE)) {
                    TTTCore.locale.getLocalizable("info.personal.status.role.detective")
                            .withPrefix(Color.DETECTIVE).sendTo(pl);
                    TitleHelper.sendStatusTitle(pl, Role.DETECTIVE);
                } else {
                    TTTCore.locale.getLocalizable("info.personal.status.role.innocent")
                            .withPrefix(Color.INNOCENT).sendTo(pl);
                    TitleHelper.sendStatusTitle(pl, Role.INNOCENT);
                }
            } else if (ch.getTeam().get().getId().equals(Role.TRAITOR)) {
                if (ch.getTeam().get().getChallengers().size() > 1) {
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor")
                            .withPrefix(Color.TRAITOR).sendTo(pl);
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor.allies")
                            .withPrefix(Color.TRAITOR).sendTo(pl);
                    for (Challenger traitor : ch.getTeam().get().getChallengers()) {
                        if (traitor != ch) { // don't list them as an ally to themselves
                            pl.sendMessage(Color.TRAITOR + "- " + traitor.getName());
                        }
                    }
                } else {
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor.alone")
                            .withPrefix(Color.TRAITOR).sendTo(pl);
                }
                TitleHelper.sendStatusTitle(pl, Role.TRAITOR);
            }

            if (ConfigHelper.KARMA_DAMAGE_REDUCTION) {
                KarmaHelper.applyDamageReduction(ch);
                double reduc = KarmaHelper.getDamageReduction(ch);
                String percentage = reduc < 1
                        ? (int) (reduc * 100) + "%"
                        : TTTCore.locale.getLocalizable("fragment.full")
                        .localizeFor(pl);
                TTTCore.locale.getLocalizable("info.personal.status.karma-damage")
                        .withPrefix(Color.INFO).withReplacements(KarmaHelper.getKarma(ch) + "", percentage)
                        .sendTo(pl);
            }
        }
        MiscUtil.broadcast(round, TTTCore.locale.getLocalizable("info.global.round.event.started")
                .withPrefix(Color.INFO));
    }

    @SuppressWarnings({"deprecation"})
    @Subscribe
    public void onRoundTick(RoundTimerTickEvent event) {
        Round r = event.getRound();
        if (r.getLifecycleStage() != Stage.WAITING) {
            long rTime = r.getRemainingTime();
            Localizable loc;
            Localizable time = null;
            if (rTime >= 60 && rTime % 60 == 0) {
                time = TTTCore.locale.getLocalizable("fragment.minutes" + (rTime / 60 == 1 ? ".singular" : ""))
                        .withReplacements(Long.toString(rTime / 60));
                ;
            } else if (rTime > 0 && rTime <= 30 && rTime % 10 == 0) {
                time = TTTCore.locale.getLocalizable("fragment.seconds" + (rTime == 1 ? ".singular" : ""))
                        .withReplacements(Long.toString(rTime));
            }
            if (time != null) {
                loc = TTTCore.locale.getLocalizable(
                        r.getLifecycleStage() == Stage.PREPARING
                                ? "info.global.round.status.starting.time"
                                : "info.global.round.status.time.remaining"
                ).withPrefix(Color.INFO);

                for (Challenger ch : r.getChallengers()) {
                    Player pl = Bukkit.getPlayer(ch.getUniqueId());
                    loc.withReplacements(time.localizeFor(pl)).sendTo(pl);
                }
            }

            if (event.getRound().getLifecycleStage() == Stage.PLAYING) {
                // check if game is over
                boolean iLeft = false;
                boolean tLeft = false;
                for (Challenger ch : event.getRound().getChallengers()) {
                    if (!(tLeft && iLeft)) {
                        if (!ch.isSpectating()) {
                            if (MiscUtil.isTraitor(ch)) {
                                tLeft = true;
                            } else {
                                iLeft = true;
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
                                    && TTTCore.mg.getChallenger(killer.getUniqueId()).isPresent()) {
                                tracker.setCompassTarget(killer.getLocation());
                            } else {
                                TTTCore.locale.getLocalizable("error.round.trackee-left")
                                        .withPrefix(Color.ERROR).sendTo(tracker);
                                ch.getMetadata().remove("tracking");
                                tracker.setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());
                            }
                        } else {
                            Random rand = new Random();
                            tracker.setCompassTarget(
                                    new Location(
                                            tracker.getWorld(),
                                            tracker.getLocation().getX() + rand.nextInt(10) - 5,
                                            tracker.getLocation().getY(),
                                            tracker.getLocation().getZ() + rand.nextInt(10) - 5
                                    )
                            );
                        }
                    }
                }
                if (!(tLeft && iLeft)) {
                    if (tLeft) {
                        event.getRound().getMetadata().set("t-victory", true);
                    }
                    event.getRound().getMetadata().set("ending", true); //TODO: temp fix
                    event.getRound().end();
                    return;
                }

                ScoreboardManager.getOrCreate(event.getRound());
            }
        }
    }

    @Subscribe
    public void onRoundEnd(RoundEndEvent event) {
        List<Body> removeBodies = new ArrayList<>();
        List<Body> removeFoundBodies = new ArrayList<>();
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

        KarmaHelper.allocateKarma(event.getRound());
        KarmaHelper.saveKarma(event.getRound());

        if (event.getRound().getLifecycleStage() == Stage.PLAYING) {
            boolean tVic = event.getRound().getMetadata().has("t-victory");

            String color = (tVic ? Color.TRAITOR : Color.INNOCENT);
            TTTCore.locale.getLocalizable("info.global.round.event.end." + (tVic ? Role.TRAITOR : Role.INNOCENT))
                    .withPrefix(color)
                    .withReplacements(Color.ARENA + event.getRound().getArena().getName() + color).broadcast();
            TitleHelper.sendVictoryTitle(event.getRound(), tVic);
        }

        for (Entity ent : Bukkit.getWorld(event.getRound().getArena().getWorld()).getEntities()) {
            if (ent.getType() == EntityType.ARROW) {
                ent.remove();
            }
        }
        Optional<ScoreboardManager> sbMan = ScoreboardManager.get(event.getRound());
        if (sbMan.isPresent()) {
            sbMan.get().unregister();
        }
    }

    @Subscribe
    public void onStageChange(RoundChangeLifecycleStageEvent event) {
        if (event.getStageBefore() == Stage.PLAYING && event.getStageAfter() == Stage.PREPARING) {
            ScoreboardManager.getOrCreate(event.getRound()).unregister();
            for (Challenger ch : event.getRound().getChallengers()) {
                Bukkit.getPlayer(ch.getUniqueId()).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                if (ch.getTeam().isPresent()) {
                    ch.getTeam().get().removeChallenger(ch);
                }
            }
        }
    }

    @Subscribe
    public void onPlayerClickLobbySign(PlayerClickLobbySignEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer());
        if (player.hasPermission("ttt.lobby.use")) {
            // lazy way of doing this, but it works
            new JoinCommand(player, new String[]{"join", event.getLobbySign().getArena().getId()}).handle();
        } else {
            TTTCore.locale.getLocalizable("error.perms.generic").withPrefix(Color.ERROR).sendTo(player);
        }
    }

}
