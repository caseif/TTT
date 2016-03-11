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

package net.caseif.ttt.command.handler.arena;

import static net.caseif.ttt.util.Constants.MetadataTag.ARENA_ROUND_TALLY;
import static net.caseif.ttt.util.Constants.MetadataTag.ARENA_START_TIME;
import static net.caseif.ttt.util.Constants.Text.DIVIDER;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.config.OperatingMode;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.round.Round;
import org.bukkit.command.CommandSender;

/**
 * Command to display information about an arena.
 */
public class ArenaInfoCommand extends CommandHandler {

    public ArenaInfoCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (args.length == 1 && TTTCore.config.OPERATING_MODE != OperatingMode.DEDICATED) {
            TTTCore.locale.getLocalizable("error.command.too-few-args").withPrefix(Color.ERROR).sendTo(sender);
            return;
        }

        String arenaName = args.length > 1 ? args[1] : TTTCore.getDedicatedArena().getName();
        Optional<Arena> arenaOpt = TTTCore.mg.getArena(arenaName);
        if (!arenaOpt.isPresent()) {
            TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ERROR).sendTo(sender);
            return;
        }
        Arena arena = arenaOpt.get();

        sender.sendMessage(DIVIDER);
        TTTCore.locale.getLocalizable("info.personal.arena-info.header").withPrefix(Color.LABEL)
                .withReplacements(Color.ARENA + arena.getName()).sendTo(sender);
        TTTCore.locale.getLocalizable("info.personal.arena-info.has-round").withPrefix(Color.INFO)
                .withReplacements(
                        TTTCore.locale.getLocalizable(arena.getRound().isPresent() ? "fragment.yes" : "fragment.no")
                                .withPrefix(Color.FLAIR)
                ).sendTo(sender);
        if (arena.getRound().isPresent()) {
            Round round = arena.getRound().get();
            TTTCore.locale.getLocalizable("info.personal.arena-info.player-count").withPrefix(Color.INFO)
                    .withReplacements(Color.FLAIR + round.getChallengers().size()).sendTo(sender);
            TTTCore.locale.getLocalizable("info.personal.arena-info.stage").withPrefix(Color.INFO)
                    .withReplacements(
                            TTTCore.locale.getLocalizable("fragment.stage." + round.getLifecycleStage().getId())
                                    .withPrefix(Color.FLAIR).localizeFor(sender).toUpperCase()
                    ).sendTo(sender);
            if (round.getLifecycleStage() != Constants.Stage.WAITING) {
                TTTCore.locale.getLocalizable("info.personal.arena-info.time").withPrefix(Color.INFO)
                        .withReplacements(
                                TTTCore.locale.getLocalizable("fragment.seconds"
                                        + (round.getRemainingTime() == 1 ? ".singular" : ""))
                                .withPrefix(Color.FLAIR).withReplacements(round.getRemainingTime() + "")
                        ).sendTo(sender);
            }
        }
        if (TTTCore.config.OPERATING_MODE == OperatingMode.DEDICATED) {
            long elapsed = System.currentTimeMillis() - arena.getMetadata().<Long>get(ARENA_START_TIME).get();
            long remainingTime = Math.max(0, TTTCore.config.CYCLE_TIME_LIMIT - (elapsed / 1000 / 60));

            TTTCore.locale.getLocalizable("info.personal.arena-info.cycle-time").withPrefix(Color.INFO)
                    .withReplacements(
                            TTTCore.locale.getLocalizable("fragment.minutes" + (remainingTime == 1 ? ".singular" : ""))
                                    .withPrefix(Color.FLAIR).withReplacements(remainingTime + "")
                    ).sendTo(sender);
            int remainingRounds = TTTCore.config.CYCLE_ROUND_LIMIT
                    - arena.getMetadata().<Integer>get(ARENA_ROUND_TALLY).get();
            TTTCore.locale.getLocalizable("info.personal.arena-info.cycle-rounds").withPrefix(Color.INFO)
                    .withReplacements(Color.FLAIR + remainingRounds).sendTo(sender);

            if (remainingTime == 0 || remainingRounds == 0) {
                TTTCore.locale.getLocalizable("info.personal.arena-info.cycle-after-"
                        + (arena.getRound().isPresent() ? "current" : "next")).withPrefix(Color.INFO).sendTo(sender);
            }
        }
        sender.sendMessage(DIVIDER);
    }

}
