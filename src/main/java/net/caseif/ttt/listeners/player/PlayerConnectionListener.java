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

package net.caseif.ttt.listeners.player;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.config.OperatingMode;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.platform.BungeeHelper;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player events related to the player's connection to the server.
 */
public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (TTTCore.config.get(ConfigKey.OPERATING_MODE) == OperatingMode.DEDICATED) {
            // goddamn do I miss lambdas
            Bukkit.getScheduler().runTask(TTTCore.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (!BungeeHelper.wasInitializationCalled()) {
                        BungeeHelper.initialize();
                    }
                    TTTCore.getDedicatedArena().getOrCreateRound().addChallenger(event.getPlayer().getUniqueId());
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!TTTCore.config.get(ConfigKey.KARMA_PERSIST)) {
            KarmaHelper.resetKarma(event.getPlayer().getUniqueId());
        }
    }

}
