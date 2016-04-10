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

package net.caseif.ttt.util;

import net.caseif.ttt.TTTBootstrap;
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;

import net.caseif.flint.round.LifecycleStage;
import org.bukkit.ChatColor;

/**
 * Contains constant values for use throughout the plugin.
 */
public final class Constants {

    public static final int MIN_FLINT_VERSION = 2;
    public static final String CODENAME = "Bruno";

    public static final int TTT_CURSEFORGE_PROJECT_ID = 52474;
    public static final int STEEL_CURSEFORGE_PROJECT_ID = 95203;

    private Constants() {
    }

    // message colors
    public static class Color {
        public static final String INFO = (TTTBootstrap.STEEL && TTTCore.HALLOWEEN
                ? ChatColor.GOLD
                : ChatColor.DARK_AQUA).toString();
        public static final String ERROR = ChatColor.RED.toString();

        public static final String FADED = ChatColor.GRAY.toString();
        public static final String FLAIR = (TTTBootstrap.STEEL && TTTCore.HALLOWEEN
                ? ChatColor.DARK_AQUA
                : ChatColor.GOLD).toString();
        public static final String LABEL = ChatColor.GREEN.toString();
        public static final String SPECIAL = ChatColor.LIGHT_PURPLE.toString();

        public static final String ARENA = ChatColor.AQUA.toString();

        public static final String DETECTIVE = ChatColor.BLUE.toString();
        public static final String INNOCENT = ChatColor.DARK_GREEN.toString();
        public static final String TRAITOR = ChatColor.DARK_RED.toString();
    }

    public static class Text {
        public static final String DIVIDER;

        static {
            final int dividerLength = 36;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dividerLength; i++) {
                sb.append("-");
            }
            DIVIDER = sb.toString();
        }
    }

    // lifecycle stages
    public static class Stage {
        public static final LifecycleStage WAITING = new LifecycleStage("waiting", -1);
        public static LifecycleStage PREPARING;
        public static LifecycleStage PLAYING;
        public static LifecycleStage ROUND_OVER;

        static {
            initialize();
        }

        public static void initialize() {
            PREPARING = new LifecycleStage("preparing", TTTCore.config.get(ConfigKey.PREPTIME_SECONDS));
            PLAYING = new LifecycleStage("playing", TTTCore.config.get(ConfigKey.ROUNDTIME_SECONDS));
            ROUND_OVER = new LifecycleStage("round_over", TTTCore.config.get(ConfigKey.POSTTIME_SECONDS));
        }
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
    }

    public static class MetadataTag {
        public static final String PURE_SPECTATOR = "pureSpectator";

        public static final String BODY = "body";
        public static final String BODY_LIST = "bodies";
        public static final String BODY_FOUND = "bodyFound";
        public static final String SEARCHING_BODY = "searchingBody";

        public static final String KARMA = "karma";
        public static final String DISPLAY_KARMA = "displayKarma";
        public static final String DAMAGE_REDUCTION = "damageRed";
        public static final String TEAM_KILLED = "hasTeamKilled";

        public static final String TEAM_NAME = "teamName";
        public static final String SCOREBOARD_MANAGER = "scoreboardManager";

        public static final String TRAITOR_VICTORY = "t-victory";

        public static final String ARENA_START_TIME = "startTime";
        public static final String ARENA_ROUND_TALLY = "roundTally";

        public static final String ROUND_RESTARTING = "restarting";

        public static final String ROUND_DURATION = "duration";
        public static final String ROUND_RESULT = "result";
        public static final String ROUND_PLAYER_COUNT = "players";
    }

    public static class TelemetryKey {
        public static final String UUID = "uuid";
        public static final String VERSION = "version";
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
    }

}
