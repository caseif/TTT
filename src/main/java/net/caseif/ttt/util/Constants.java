/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncacé <mproncace@lapis.blue>
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

import net.caseif.ttt.util.helper.ConfigHelper;

import net.caseif.flint.round.LifecycleStage;
import org.bukkit.ChatColor;

/**
 * Contains constant values for use throughout the plugin.
 */
public final class Constants {

    public static final int MIN_FLINT_VERSION = 1;

    // message colors
    public static class Color {
        public static final String ARENA = ChatColor.ITALIC.toString();
        public static final String DESCRIPTION = ChatColor.GREEN.toString();
        public static final String DETECTIVE = ChatColor.BLUE.toString();
        public static final String ERROR = ChatColor.RED.toString();
        public static final String INFO = ChatColor.DARK_AQUA.toString();
        public static final String INNOCENT = ChatColor.DARK_GREEN.toString();
        public static final String SPECIAL = ChatColor.LIGHT_PURPLE.toString();
        public static final String TRAITOR = ChatColor.DARK_RED.toString();
        public static final String USAGE = ChatColor.GOLD.toString();
    }

    // lifecycle stages
    public static class Stage {
        public static final LifecycleStage WAITING = new LifecycleStage("waiting", -1);
        public static final LifecycleStage PREPARING = new LifecycleStage("preparing", ConfigHelper.PREPTIME_SECONDS);
        public static final LifecycleStage PLAYING = new LifecycleStage("playing", ConfigHelper.ROUNDTIME_SECONDS);
    }

    public static class Role {
        public static final String INNOCENT = "innocent";
        public static final String TRAITOR = "traitor";
        public static final String DETECTIVE = "detective";
    }

    public static class AliveStatus {
        public static final String ALIVE = "alive";
        public static final String MIA = "mia";
        public static final String CONFIRMED_DEAD = "dead";
    }

    public static class Contributor {
        public static final String DEVELOPER = "dev";
        public static final String ALPHA_TESTER = "alpha";
        public static final String TESTER = "tester";
        public static final String TRANSLATOR = "translator";
    }

    public static class PlayerTag {
        public static final String PURE_SPECTATOR = "pureSpectator";
    }

}
