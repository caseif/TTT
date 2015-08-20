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

import net.caseif.ttt.Config;

import net.caseif.flint.round.LifecycleStage;
import org.bukkit.ChatColor;

/**
 * Contains constant values for use throughout the plugin.
 */
public class Constants {

    // message formatting
    public static final ChatColor ARENA_COLOR = ChatColor.ITALIC;
    public static final ChatColor DESCRIPTION_COLOR = ChatColor.GREEN;
    public static final ChatColor DETECTIVE_COLOR = ChatColor.BLUE;
    public static final ChatColor ERROR_COLOR = ChatColor.RED;
    public static final ChatColor INFO_COLOR = ChatColor.DARK_PURPLE;
    public static final ChatColor INNOCENT_COLOR = ChatColor.DARK_GREEN;
    public static final ChatColor SPECIAL_COLOR = ChatColor.LIGHT_PURPLE;
    public static final ChatColor TRAITOR_COLOR = ChatColor.DARK_RED;
    public static final ChatColor USAGE_COLOR = ChatColor.GOLD;

    // lifecycle stages
    public static final LifecycleStage WAITING = new LifecycleStage("waiting", -1);
    public static final LifecycleStage PREPARING = new LifecycleStage("preparing", Config.SETUP_TIME);
    public static final LifecycleStage PLAYING = new LifecycleStage("playing", Config.TIME_LIMIT);

    public static final int MIN_FLINT_VERSION = 1;

}
