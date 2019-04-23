/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2019, Max Roncace <me@caseif.net>
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

package net.caseif.ttt.listeners.minigame;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.use.JoinCommand;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.config.OperatingMode;
import net.caseif.ttt.util.constant.Color;
import net.caseif.ttt.util.constant.CommandRegex;
import net.caseif.ttt.util.constant.MetadataKey;
import net.caseif.ttt.util.constant.Stage;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.gamemode.RoundHelper;
import net.caseif.ttt.util.helper.platform.BungeeHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;
import net.caseif.ttt.util.helper.platform.PlayerHelper;

import com.google.common.eventbus.Subscribe;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.event.lobby.PlayerClickLobbySignEvent;
import net.caseif.flint.event.round.challenger.ChallengerJoinRoundEvent;
import net.caseif.flint.event.round.challenger.ChallengerLeaveRoundEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Listener for challenger events.
 */
public class ChallengerListener {

    @Subscribe
    public void onChallengerJoinRound(ChallengerJoinRoundEvent event) {
        if (event.getRound().getLifecycleStage() == Stage.PLAYING
                || event.getRound().getLifecycleStage() == Stage.ROUND_OVER) {
            event.getChallenger().setSpectating(true);
            PlayerHelper.watchPlayerGameMode(event.getChallenger());
            event.getChallenger().getMetadata().set(MetadataKey.Player.PURE_SPECTATOR, true);
        }

        Player pl = Bukkit.getPlayer(event.getChallenger().getUniqueId());
        pl.setHealth(pl.getMaxHealth());
        pl.setCompassTarget(Bukkit.getWorlds().get(1).getSpawnLocation());

        if (!event.getChallenger().getMetadata().containsKey(MetadataKey.Player.PURE_SPECTATOR)) {
            pl.setGameMode(GameMode.SURVIVAL);
            KarmaHelper.applyKarma(event.getChallenger());

            if (!event.getRound().getMetadata().containsKey(MetadataKey.Round.ROUND_RESTARTING)) {
                RoundHelper.broadcast(event.getRound(),
                        TTTCore.locale.getLocalizable("info.global.arena.event.join").withPrefix(Color.INFO)
                                .withReplacements(event.getChallenger().getName()
                                        + TTTCore.clh.getContributorString(pl)));
                if (TTTCore.config.get(ConfigKey.OPERATING_MODE) != OperatingMode.DEDICATED
                        || BungeeHelper.hasSupport()) {
                    TTTCore.locale.getLocalizable("info.personal.arena.join.leave-tip").withPrefix(Color.INFO)
                            .withReplacements(Color.EM + "/ttt leave" + Color.INFO).sendTo(pl);
                }
            }

            if (event.getRound().getLifecycleStage() == Stage.WAITING
                    && event.getRound().getChallengers().size()
                    >= event.getRound().getMetadata().<Integer>get(MetadataKey.Arena.PROPERTY_MIN_PLAYERS).get()) {
                event.getRound().nextLifecycleStage();
            }
        }

        if (!event.getRound().getMetadata().containsKey(MetadataKey.Round.SCOREBOARD_MANAGER)) {
            event.getRound().getMetadata()
                    .set(MetadataKey.Round.SCOREBOARD_MANAGER, new ScoreboardManager(event.getRound()));
        }

        ScoreboardManager sm = event.getRound().getMetadata()
                .<ScoreboardManager>get(MetadataKey.Round.SCOREBOARD_MANAGER).get();
        sm.applyScoreboard(event.getChallenger());
        sm.updateEntry(event.getChallenger());

        runCommands(TTTCore.config.get(ConfigKey.JOIN_CMDS), event.getChallenger());
    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void onChallengerLeaveRound(ChallengerLeaveRoundEvent event) {
        try {
            Player pl = Bukkit.getPlayer(event.getChallenger().getUniqueId());
            pl.setScoreboard(TTTCore.getPlugin().getServer().getScoreboardManager().getNewScoreboard());

            if (event.getChallenger().getMetadata().containsKey(MetadataKey.Player.SEARCHING_BODY)) {
                pl.closeInventory();
            }

            pl.setDisplayName(event.getChallenger().getName());
            pl.setCompassTarget(LocationHelper.convert(event.getReturnLocation()).getWorld().getSpawnLocation());
            pl.setHealth(pl.getMaxHealth());
        } catch (IllegalStateException ex) { // player is probably disconnecting; just ignore the event
        }

        if (!event.getRound().isEnding()) {
            if (!event.getChallenger().getMetadata().containsKey(MetadataKey.Player.PURE_SPECTATOR)) {
                KarmaHelper.saveKarma(event.getChallenger());
                RoundHelper.broadcast(event.getRound(), TTTCore.locale.getLocalizable("info.global.arena.event.leave")
                        .withPrefix(Color.INFO).withReplacements(event.getChallenger().getName(),
                                Color.EM + event.getChallenger().getRound().getArena().getDisplayName()
                                        + Color.INFO));

                if (event.getRound().getLifecycleStage() == Stage.PREPARING
                        && event.getRound().getChallengers().size() <= 1) {
                    event.getRound().setLifecycleStage(Stage.WAITING, true);
                    RoundHelper.broadcast(event.getRound(),
                            TTTCore.locale.getLocalizable("info.global.round.status.starting.stopped")
                                    .withPrefix(Color.ALERT));
                }
            }
        }

        event.getRound().getMetadata().<ScoreboardManager>get(MetadataKey.Round.SCOREBOARD_MANAGER).get()
                .remove(event.getChallenger());

        if (event.getRound().getChallengers().isEmpty()) {
            event.getRound().end();
        }

        runCommands(TTTCore.config.get(ConfigKey.LEAVE_CMDS), event.getChallenger());
    }

    // doesn't technically fit but nbd
    @Subscribe
    public void onPlayerClickLobbySign(PlayerClickLobbySignEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer());
        if (player.hasPermission("ttt.lobby.use")) {
            // lazy way of doing this, but it works
            new JoinCommand(player, new String[]{"join", event.getLobbySign().getArena().getId()}).handle();
        } else {
            TTTCore.locale.getLocalizable("error.perms.generic").withPrefix(Color.ALERT).sendTo(player);
        }
    }

    private void runCommands(List<String> commands, Challenger challenger) {
        for (String cmd : commands) {
            cmd = CommandRegex.PLAYER_WILDCARD.matcher(cmd).replaceAll(challenger.getName());
            cmd = CommandRegex.ARENA_WILDCARD.matcher(cmd).replaceAll(challenger.getRound().getArena().getId());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

}
