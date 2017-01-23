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

package net.caseif.ttt.util.constant;

public class TelemetryKey {

    public static final String UUID = "uuid";
    public static final String VERSION = "version";
    public static final String PLATFORM = "platform";
    public static final String FLINT_API = "flintApi";
    public static final String OPERATING_MODE = "opMode";
    public static final String ARENA_COUNT = "arenas";
    public static final String ROUND_COUNT = "rounds";
    public static final String ROUND_MEAN_PLAYERS = "players";
    public static final String ROUND_DURATION_MEAN = "roundDur";
    public static final String ROUND_DURATION_STD_DEV = "roundDurSD";
    public static final String ROUND_INNOCENT_WINS = "iWins";
    public static final String ROUND_TRAITOR_WINS = "tWins";
    public static final String ROUND_FORFEITS = "forfeits";

    private TelemetryKey() {
    }

}
