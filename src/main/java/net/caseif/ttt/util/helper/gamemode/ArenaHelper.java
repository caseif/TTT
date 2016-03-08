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

import net.caseif.ttt.TTTCore;

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

    public static Arena getNextArena() {
        assert TTTCore.mg.getArenas().size() > 0;

        incrementIndex();

        switch (TTTCore.config.CYCLE_MODE) {
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
        int newIndex = arenaIndex + 1;
        if (newIndex > TTTCore.mg.getArenas().size()) {
            arenaIndex = 0;
        }
    }
}
