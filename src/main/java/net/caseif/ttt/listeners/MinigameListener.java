/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016, Max Roncace <me@caseif.net>
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

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.use.JoinCommand;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.Constants.Stage;
import net.caseif.ttt.util.RoundRestartDaemon;
import net.caseif.ttt.util.config.OperatingMode;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.gamemode.RoleHelper;
import net.caseif.ttt.util.helper.gamemode.RoundHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;

import com.google.common.eventbus.Subscribe;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.event.lobby.PlayerClickLobbySignEvent;
import net.caseif.flint.event.round.RoundChangeLifecycleStageEvent;
import net.caseif.flint.event.round.RoundEndEvent;
import net.caseif.flint.event.round.RoundTimerTickEvent;
import net.caseif.flint.event.round.challenger.ChallengerJoinRoundEvent;
import net.caseif.flint.event.round.challenger.ChallengerLeaveRoundEvent;
import net.caseif.flint.round.Round;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class MinigameListener {

    @Subscribe
    public void onChallengerJoinRound(ChallengerJoinRoundEvent event) {
        if (event.getRound().getLifecycleStage() == Stage.PLAYING
                || event.getRound().getLifecycleStage() == Stage.ROUND_OVER) {
            event.getChallenger().setSpectating(true);
            event.getChallenger().getMetadata().set(MetadataTag.PURE_SPECTATOR, true);
        }

        Player pl = Bukkit.getPlayer(event.getChallenger().getUniqueId());
        pl.setHealth(pl.getMaxHealth());
        pl.setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());

        if (!event.getChallenger().getMetadata().has(MetadataTag.PURE_SPECTATOR)) {
            pl.setGameMode(GameMode.SURVIVAL);
            KarmaHelper.applyKarma(event.getChallenger());

            RoundHelper.broadcast(event.getRound(),
                    TTTCore.locale.getLocalizable("info.global.arena.event.join").withPrefix(Color.INFO)
                            .withReplacements(event.getChallenger().getName() + TTTCore.clh.getContributorString(pl)));

            if (event.getRound().getLifecycleStage() == Stage.WAITING
                    && event.getRound().getChallengers().size() >= TTTCore.config.MINIMUM_PLAYERS) {
                event.getRound().nextLifecycleStage();
            }
        }

        if (!event.getRound().getMetadata().has(MetadataTag.SCOREBOARD_MANAGER)) {
            event.getRound().getMetadata()
                    .set(MetadataTag.SCOREBOARD_MANAGER, new ScoreboardManager(event.getRound()));
        }

        ScoreboardManager sm = event.getRound().getMetadata()
                .<ScoreboardManager>get(MetadataTag.SCOREBOARD_MANAGER).get();
        sm.applyScoreboard(event.getChallenger());
        sm.updateEntry(event.getChallenger());
    }

    @Subscribe
    public void onChallengerLeaveRound(ChallengerLeaveRoundEvent event) {
        Player pl = Bukkit.getPlayer(event.getChallenger().getUniqueId());
        pl.setScoreboard(TTTCore.getPlugin().getServer().getScoreboardManager().getNewScoreboard());

        if (event.getChallenger().getMetadata().has(MetadataTag.SEARCHING_BODY)) {
            pl.closeInventory();
        }

        pl.setDisplayName(event.getChallenger().getName());
        pl.setCompassTarget(LocationHelper.convert(event.getReturnLocation()).getWorld().getSpawnLocation());
        pl.setHealth(pl.getMaxHealth());

        if (!event.getRound().isEnding()) {
            if (!event.getChallenger().getMetadata().has(MetadataTag.PURE_SPECTATOR)) {
                KarmaHelper.saveKarma(event.getChallenger());
                RoundHelper.broadcast(event.getRound(), TTTCore.locale.getLocalizable("info.global.arena.event.leave")
                        .withPrefix(Color.INFO).withReplacements(event.getChallenger().getName(),
                                Color.ARENA + event.getChallenger().getRound().getArena().getName() + Color.INFO));

                if (event.getRound().getLifecycleStage() == Stage.PREPARING
                        && event.getRound().getChallengers().size() <= 1) {
                    event.getRound().setLifecycleStage(Stage.WAITING, true);
                    RoundHelper.broadcast(event.getRound(),
                            TTTCore.locale.getLocalizable("info.global.round.status.starting.stopped")
                                    .withPrefix(Color.ERROR));
                }
            }
        }

        event.getRound().getMetadata().<ScoreboardManager>get(MetadataTag.SCOREBOARD_MANAGER).get()
                .remove(event.getChallenger());

        if (event.getRound().getChallengers().isEmpty()) {
            event.getRound().end();
        }
    }

    @Subscribe
    public void onRoundChangeLifecycleStage(RoundChangeLifecycleStageEvent event) {
        if (event.getStageAfter() == Stage.PREPARING) {
            RoundHelper.broadcast(event.getRound(), TTTCore.locale.getLocalizable("info.global.round.event.starting")
                    .withPrefix(Color.INFO));
        } else if (event.getStageAfter() == Stage.PLAYING) {
            RoundHelper.startRound(event.getRound());
        } else if (event.getStageAfter() == Stage.ROUND_OVER) {
            RoundHelper.closeRound(event.getRound());
        }
    }

    @SuppressWarnings({"deprecation"})
    @Subscribe
    public void onRoundTick(RoundTimerTickEvent event) {
        Round r = event.getRound();

        r.getMetadata().<ScoreboardManager>get(MetadataTag.SCOREBOARD_MANAGER).get().updateTitle();

        if (r.getLifecycleStage() != Stage.WAITING) {
            if (event.getRound().getLifecycleStage() == Stage.PLAYING) {
                // check if game is over
                boolean iLeft = false;
                boolean tLeft = false;
                for (Challenger ch : event.getRound().getChallengers()) {
                    if (!(tLeft && iLeft)) {
                        if (!ch.isSpectating()) {
                            if (RoleHelper.isTraitor(ch)) {
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
                            && ch.getRound().getTime() % TTTCore.config.SCANNER_CHARGE_TIME == 0) {
                        Player tracker = TTTCore.getPlugin().getServer().getPlayer(ch.getName());
                        if (ch.getMetadata().has("tracking")) {
                            Player killer = TTTCore.getPlugin().getServer()
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
                        event.getRound().getMetadata().set(MetadataTag.TRAITOR_VICTORY, true);
                    }

                    event.getRound().setLifecycleStage(Stage.ROUND_OVER, true);
                    return;
                }
            }
        }
    }

    @Subscribe
    public void onRoundEnd(RoundEndEvent event) {
        if (event.getRound().getLifecycleStage() != Stage.ROUND_OVER) {
            RoundHelper.closeRound(event.getRound());
        }

        for (Entity ent : Bukkit.getWorld(event.getRound().getArena().getWorld()).getEntities()) {
            if (ent.getType() == EntityType.ARROW) {
                ent.remove();
            }
        }

        event.getRound().getMetadata().<ScoreboardManager>get(MetadataTag.SCOREBOARD_MANAGER).get().uninitialize();

        if (TTTCore.config.OPERATING_MODE == OperatingMode.CONTINUOUS
                || TTTCore.config.OPERATING_MODE == OperatingMode.DEDICATED) {
            // restart the round
            new RoundRestartDaemon(event.getRound().getArena()).runTask(TTTCore.getPlugin());
        }
    }

    @Subscribe
    public void onStageChange(RoundChangeLifecycleStageEvent event) {
        if (event.getStageBefore() == Stage.PREPARING && event.getStageAfter() == Stage.WAITING) {
            for (Challenger ch : event.getRound().getChallengers()) {
                Bukkit.getPlayer(ch.getUniqueId())
                        .setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }

            for (Team team : event.getRound().getTeams()) {
                event.getRound().removeTeam(team);
            }

            event.getRound().getMetadata().<ScoreboardManager>get(MetadataTag.SCOREBOARD_MANAGER).get()
                    .updateAllEntries();
        } else if (event.getStageAfter() == Stage.ROUND_OVER) {
            event.getRound().setConfigValue(ConfigNode.WITHHOLD_SPECTATOR_CHAT, false);
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
