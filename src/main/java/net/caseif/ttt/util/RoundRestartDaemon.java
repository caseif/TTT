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

import net.caseif.ttt.util.config.OperatingMode;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.round.Round;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Daemon used to restart rounds after they've ended when the server is
 * operating in {@link OperatingMode.CONTINUOUS} or {OperatingMode.DEDICATED}
 * mode.
 */
public class RoundRestartDaemon extends BukkitRunnable {

    private Arena arena;

    private final Set<UUID> players = new HashSet<>();

    public RoundRestartDaemon(Arena arena) {
        super();
        this.arena = arena;
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    @Override
    public void run() {
        assert !arena.getRound().isPresent(); // it shouldn't contain a round before the daemon runs

        Round round = arena.createRound();
        for (UUID uuid : players) {
            round.addChallenger(uuid);
        }
    }

}
