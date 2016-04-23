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

package net.caseif.ttt.util.constant;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;

import net.caseif.flint.round.LifecycleStage;

// lifecycle stages
public class Stage {

    public static final LifecycleStage WAITING = new LifecycleStage("waiting", -1);
    public static LifecycleStage PREPARING;
    public static LifecycleStage PLAYING;
    public static LifecycleStage ROUND_OVER;

    static {
        initialize();
    }

    private Stage() {
    }

    public static void initialize() {
        PREPARING = new LifecycleStage("preparing", TTTCore.config.get(ConfigKey.PREPTIME_SECONDS));
        PLAYING = new LifecycleStage("playing", TTTCore.config.get(ConfigKey.ROUNDTIME_SECONDS));
        ROUND_OVER = new LifecycleStage("round_over", TTTCore.config.get(ConfigKey.POSTTIME_SECONDS));
    }

}
