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

package net.caseif.ttt.util.helper.platform;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.component.exception.OrphanedComponentException;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * Static utility class for player-related functionality.
 */
public final class PlayerHelper {

    private static final Method getOnlinePlayers;
    private static final boolean newGopMethod;

    static {
        try {
            getOnlinePlayers = Server.class.getMethod("getOnlinePlayers");
            newGopMethod = getOnlinePlayers.getReturnType().equals(Collection.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            return newGopMethod
                    ? (Collection<? extends Player>) getOnlinePlayers.invoke(Bukkit.getServer())
                    : Arrays.asList((Player[]) getOnlinePlayers.invoke(Bukkit.getServer()));
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Unfortunately, other plugins *cough* Multiverse *cough* sometimes feel the need to
    // override the player's gamemode when they change worlds. This method prevents that
    // from happening.
    public static void watchPlayerGameMode(final Challenger challenger) {
        challenger.getMetadata().set(Constants.MetadataTag.WATCH_GAME_MODE, true);
        Bukkit.getScheduler().runTaskLater(TTTCore.getPlugin(), new Runnable() {
            @Override
            public void run() {
                try {
                    challenger.getMetadata().remove(Constants.MetadataTag.WATCH_GAME_MODE);
                } catch (OrphanedComponentException ignored) {
                }
            }
        }, 2L);
    }

}
