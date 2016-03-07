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

package net.caseif.ttt.util;

import static net.caseif.ttt.util.Constants.MetadataTag.ARENA_ROUND_TALLY;
import static net.caseif.ttt.util.Constants.MetadataTag.ARENA_START_TIME;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.OperatingMode;
import net.caseif.ttt.util.helper.gamemode.ArenaHelper;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Daemon used to restart rounds after they've ended when the server is
 * operating in {@link OperatingMode#CONTINUOUS} or {OperatingMode#DEDICATED}
 * mode.
 */
public class RoundRestartDaemon extends BukkitRunnable {

    private Arena arena;

    private final Set<UUID> players = new HashSet<>();

    boolean willCycle = false;

    public RoundRestartDaemon(Arena arena) {
        super();

        assert arena.getRound().isPresent();

        this.arena = arena;
        arena.getMetadata().set(ARENA_ROUND_TALLY, arena.getMetadata().<Integer>get(ARENA_ROUND_TALLY).get() + 1);
        willCycle = TTTCore.config.OPERATING_MODE == OperatingMode.DEDICATED && shouldArenaBeCycled();

        for (Challenger ch : arena.getRound().get().getChallengers()) {
            players.add(ch.getUniqueId());
        }
    }

    @Override
    public void run() {
        assert !arena.getRound().isPresent(); // it shouldn't contain a round before the daemon runs

        if (willCycle) {
            cycleArena();
        }

        Round round = arena.createRound();
        for (UUID uuid : players) {
            round.addChallenger(uuid);
        }
    }

    private boolean shouldArenaBeCycled() {
        int timeLimit = TTTCore.config.CYCLE_TIME_LIMIT;
        int roundLimit = TTTCore.config.CYCLE_ROUND_LIMIT;

        if ((TTTCore.config.CYCLE_TIME_LIMIT >= 0)
                && (System.currentTimeMillis() - arena.getMetadata().<Long>get(ARENA_START_TIME).get())
                >= (timeLimit * 1000)) { // I realize that was super-ugly
            return true; // time limit reached
        } else if (roundLimit >= 0 && arena.getMetadata().<Integer>get(ARENA_ROUND_TALLY).get() >= roundLimit) {
            return true; // round limit reached
        }

        return false;
    }

    private void cycleArena() {
        this.arena = ArenaHelper.getNextArena();
        TTTCore.setDedicatedArena(arena);
    }

}
