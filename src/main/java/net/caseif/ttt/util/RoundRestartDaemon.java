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
import static net.caseif.ttt.util.Constants.MetadataTag.ROUND_RESTARTING;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;
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

    private final boolean willCycle;
    private final boolean willRestart;

    public RoundRestartDaemon(Round round) {
        super();

        this.willRestart = round.getChallengers().size() > 0;

        this.arena = round.getArena();

        if (TTTCore.config.get(ConfigKey.OPERATING_MODE) == OperatingMode.DEDICATED) {
            arena.getMetadata().set(ARENA_ROUND_TALLY, arena.getMetadata().<Integer>get(ARENA_ROUND_TALLY).get() + 1);
            this.willCycle = ArenaHelper.shouldArenaCycle(arena);
        } else {
            this.willCycle = false;
        }

        if (this.willRestart) {
            for (Challenger ch : round.getChallengers()) {
                players.add(ch.getUniqueId());
            }
        }
    }

    @Override
    public void run() {
        assert !arena.getRound().isPresent(); // it shouldn't contain a round before the daemon runs

        if (willCycle) {
            cycleArena();
        }

        if (this.willRestart) {
            Round round = arena.createRound();
            round.getMetadata().set(ROUND_RESTARTING, true);
            for (UUID uuid : players) {
                round.addChallenger(uuid);
            }
            round.getMetadata().remove(ROUND_RESTARTING);
        }
    }

    private void cycleArena() {
        this.arena.getMetadata().remove(ARENA_START_TIME);
        this.arena.getMetadata().remove(ARENA_ROUND_TALLY);
        ArenaHelper.applyNextArena();
        this.arena = TTTCore.getDedicatedArena();
    }

}
