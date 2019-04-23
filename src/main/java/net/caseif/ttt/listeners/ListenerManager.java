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

package net.caseif.ttt.listeners;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.listeners.minigame.ChallengerListener;
import net.caseif.ttt.listeners.minigame.RoundListener;
import net.caseif.ttt.listeners.player.PlayerConnectionListener;
import net.caseif.ttt.listeners.player.PlayerInteractListener;
import net.caseif.ttt.listeners.player.PlayerUpdateListener;
import net.caseif.ttt.listeners.player.SpecialPlayerListener;
import net.caseif.ttt.listeners.wizard.WizardListener;
import net.caseif.ttt.listeners.world.BlockListener;
import net.caseif.ttt.listeners.world.EntityListener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * Central manager for event listeners.
 */
public class ListenerManager {

    public static void registerEventListeners() {
        // register minigame listeners
        TTTCore.mg.getEventBus().register(new ChallengerListener());
        TTTCore.mg.getEventBus().register(new RoundListener());

        // register player listeners
        registerListener(new PlayerConnectionListener());
        registerListener(new PlayerInteractListener());
        registerListener(new PlayerUpdateListener());

        // register world listeners
        registerListener(new BlockListener());
        registerListener(new EntityListener());

        // register wizard listener
        registerListener(new WizardListener());
    }

    public static void registerSpecialEventListeners() {
        registerListener(new SpecialPlayerListener());
    }

    private static void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, TTTCore.getPlugin());
    }

}
