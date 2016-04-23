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

package net.caseif.ttt.util.helper.gamemode;

import static net.caseif.ttt.util.constant.MetadataKey.Arena.ARENA_ROUND_TALLY;
import static net.caseif.ttt.util.constant.MetadataKey.Arena.ARENA_START_TIME;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;

import com.google.common.collect.Lists;
import net.caseif.flint.arena.Arena;

import java.util.Collections;
import java.util.List;

/**
 * Static utility class for arena-related functionality.
 */
public final class ArenaHelper {

    public static List<Arena> shuffledArenas;
    public static int arenaIndex = 0;

    static {
        updateShuffledArenas();
    }

    public static void updateShuffledArenas() {
        shuffledArenas = Lists.newArrayList(TTTCore.mg.getArenas());
        Collections.shuffle(shuffledArenas);
    }

    public static void applyNextArena() {
        TTTCore.setDedicatedArena(ArenaHelper.getNextArena());
        TTTCore.getDedicatedArena().getMetadata().set(ARENA_START_TIME, System.currentTimeMillis());
        TTTCore.getDedicatedArena().getMetadata().set(ARENA_ROUND_TALLY, 1);
    }

    private static Arena getNextArena() {
        assert TTTCore.mg.getArenas().size() > 0;

        incrementIndex();

        switch (TTTCore.config.get(ConfigKey.MAP_CYCLE_MODE)) {
            case SEQUENTIAL:
                return TTTCore.mg.getArenas().get(arenaIndex);
            case SHUFFLE:
                return shuffledArenas.get(arenaIndex);
            case RANDOM:
                int randIndex = (int) Math.floor(Math.random() * TTTCore.mg.getArenas().size());
                return TTTCore.mg.getArenas().get(randIndex);
            default:
                throw new AssertionError();
        }
    }

    private static void incrementIndex() {
        arenaIndex++;
        if (arenaIndex >= TTTCore.mg.getArenas().size()) {
            arenaIndex = 0;
        }
    }

    public static boolean shouldArenaCycle(Arena arena) {
        int timeLimit = TTTCore.config.get(ConfigKey.MAP_CYCLE_TIME_LIMIT);
        int roundLimit = TTTCore.config.get(ConfigKey.MAP_CYCLE_ROUND_LIMIT);

        if (!arena.getMetadata().has(ARENA_START_TIME)) {
            arena.getMetadata().set(ARENA_START_TIME, System.currentTimeMillis());
        }
        if (!arena.getMetadata().has(ARENA_ROUND_TALLY)) {
            arena.getMetadata().set(ARENA_ROUND_TALLY, 1);
        }

        if (timeLimit >= 0 && (TTTCore.config.get(ConfigKey.MAP_CYCLE_TIME_LIMIT) >= 0)
                && (System.currentTimeMillis() - arena.getMetadata().<Long>get(ARENA_START_TIME).get())
                >= (timeLimit * 60 * 1000)) { // I realize that was super-ugly
            return true; // time limit reached
        } else if (roundLimit >= 0 && arena.getMetadata().<Integer>get(ARENA_ROUND_TALLY).get() - 1 >= roundLimit) {
            return true; // round limit reached
        }

        return false;
    }

}
