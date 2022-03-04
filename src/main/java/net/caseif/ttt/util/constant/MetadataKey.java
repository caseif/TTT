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

package net.caseif.ttt.util.constant;

public class MetadataKey {

    public static class Arena {

        public static final String START_TIME = "startTime";
        public static final String ROUND_TALLY = "roundTally";
        public static final String PROPERTY_CAT = "prop";
        public static final String PROPERTY_MIN_PLAYERS = "min-players";
        public static final String PROPERTY_MAX_PLAYERS = "max-players";
        public static final String EDITOR = "editor";

        private Arena() {
        }

    }

    public static class Player {

        public static final String BODY = "body";
        public static final String BODY_FOUND = "bodyFound";
        public static final String KARMA = "karma";
        public static final String DISPLAY_KARMA = "displayKarma";
        public static final String DAMAGE_REDUCTION = "damageRed";
        public static final String TEAM_KILLED = "hasTeamKilled";
        public static final String WATCH_GAME_MODE = "watchGm";
        public static final String PURE_SPECTATOR = "pureSpectator";
        public static final String TEAM_NAME = "teamName";
        public static final String SEARCHING_BODY = "searchingBody";

        private Player() {
        }

    }

    public static class Round {

        public static final String METADATA_INITIALIZED = "metaInitialized";
        public static final String BODY_LIST = "bodies";
        public static final String SCOREBOARD_MANAGER = "scoreboardManager";
        public static final String TRAITOR_VICTORY = "t-victory";
        public static final String ROUND_RESTARTING = "restarting";
        public static final String HASTE_TIME = "hasteTime";

        private Round() {
        }

    }

    private MetadataKey() {
    }

}
