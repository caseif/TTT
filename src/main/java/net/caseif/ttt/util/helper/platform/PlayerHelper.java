/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2017, Max Roncace <me@caseif.net>
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

package net.caseif.ttt.util.helper.platform;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.constant.MetadataKey;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.component.exception.OrphanedComponentException;
import org.bukkit.Bukkit;

/**
 * Static utility class for player-related functionality.
 */
public final class PlayerHelper {

    // Unfortunately, other plugins *cough* Multiverse *cough* sometimes feel
    // the need to override the player's gamemode when they change worlds. This
    // method flags players while this is occuring so another plugin component
    // can prevent this behavior.
    public static void watchPlayerGameMode(final Challenger challenger) {
        challenger.getMetadata().set(MetadataKey.Player.WATCH_GAME_MODE, true);
        Bukkit.getScheduler().runTaskLater(TTTCore.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    challenger.getMetadata().remove(MetadataKey.Player.WATCH_GAME_MODE);
                } catch (OrphanedComponentException ignored) {
                }
            }
        }, 2L);
    }

}
