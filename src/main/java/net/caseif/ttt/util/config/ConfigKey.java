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

package net.caseif.ttt.util.config;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.helper.platform.MaterialHelper;

import com.google.common.reflect.TypeToken;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"serial", "unchecked"})
public class ConfigKey<T> {

    private static final Set<ConfigKey<?>> keys = new HashSet<>();

    // Round settings
    public static final ConfigKey<Integer> MINIMUM_PLAYERS = new ConfigKey<>(Integer.class, "minimum-players");
    public static final ConfigKey<Integer> MAXIMUM_PLAYERS = new ConfigKey<>(Integer.class, "maximum-players");
    public static final ConfigKey<Integer> PREPTIME_SECONDS = new ConfigKey<>(Integer.class, "preptime-seconds");
    public static final ConfigKey<Integer> ROUNDTIME_SECONDS = new ConfigKey<>(Integer.class, "roundtime-seconds");
    public static final ConfigKey<Integer> POSTTIME_SECONDS = new ConfigKey<>(Integer.class, "posttime-seconds");
    public static final ConfigKey<Boolean> HASTE = new ConfigKey<>(Boolean.class, "haste");
    public static final ConfigKey<Integer> HASTE_STARTING_SECONDS
            = new ConfigKey<>(Integer.class, "haste-starting-seconds");
    public static final ConfigKey<Integer> HASTE_SECONDS_PER_DEATH
            = new ConfigKey<>(Integer.class, "haste-seconds-per-death");
    public static final ConfigKey<Boolean> ALLOW_JOIN_AS_SPECTATOR
            = new ConfigKey<>(Boolean.class, "allow-join-as-spectator");
    public static final ConfigKey<Boolean> BROADCAST_WIN_MESSAGES_TO_SERVER
            = new ConfigKey<>(Boolean.class, "broadcast-win-messages-to-server");

    // Traitor/Detective settings
    public static final ConfigKey<Double> TRAITOR_PCT = new ConfigKey<>(Double.class, "traitor-pct");
    public static final ConfigKey<Double> DETECTIVE_PCT = new ConfigKey<>(Double.class, "detective-pct");
    public static final ConfigKey<Integer> DETECTIVE_MIN_PLAYERS
            = new ConfigKey<>(Integer.class, "detective-min-players");
    public static final ConfigKey<Integer> SCANNER_CHARGE_TIME = new ConfigKey<>(Integer.class, "scanner-charge-time");
    public static final ConfigKey<Integer> KILLER_DNA_RANGE = new ConfigKey<>(Integer.class, "killer-dna-range");
    public static final ConfigKey<Integer> KILLER_DNA_BASETIME = new ConfigKey<>(Integer.class, "killer-dna-basetime");

    // Command settings
    public static final ConfigKey<List<String>> JOIN_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-join-cmds");
    public static final ConfigKey<List<String>> LEAVE_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-leave-cmds");
    public static final ConfigKey<List<String>> WIN_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-win-cmds");
    public static final ConfigKey<List<String>> WIN_I_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-win-innocent-cmds");
    public static final ConfigKey<List<String>> WIN_IND_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-win-innocentnd-cmds");
    public static final ConfigKey<List<String>> WIN_D_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-win-detective-cmds");
    public static final ConfigKey<List<String>> WIN_T_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-win-traitor-cmds");
    public static final ConfigKey<List<String>> LOSE_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-lose-cmds");
    public static final ConfigKey<List<String>> LOSE_I_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-lose-innocent-cmds");
    public static final ConfigKey<List<String>> LOSE_IND_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-lose-innocentnd-cmds");
    public static final ConfigKey<List<String>> LOSE_D_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-lose-detective-cmds");
    public static final ConfigKey<List<String>> LOSE_T_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "player-lose-traitor-cmds");
    public static final ConfigKey<List<String>> PREPARE_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "round-prepare-cmds");
    public static final ConfigKey<List<String>> START_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "round-start-cmds");
    public static final ConfigKey<List<String>> COOLDOWN_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "round-cooldown-cmds");
    public static final ConfigKey<List<String>> END_CMDS
            = new ConfigKey<>(new TypeToken<List<String>>(){}, "round-end-cmds");

    // Title settings
    public static final ConfigKey<Boolean> SEND_TITLES = new ConfigKey<>(Boolean.class, "send-titles");
    public static final ConfigKey<Boolean> LARGE_STATUS_TITLES = new ConfigKey<>(Boolean.class, "large-status-titles");
    public static final ConfigKey<Boolean> LARGE_VICTORY_TITLES
            = new ConfigKey<>(Boolean.class, "large-victory-titles");

    // Weapon settings
    public static final ConfigKey<Material> CROWBAR_ITEM =
            new ConfigKey<>(Material.class, "crowbar-item"
                    + (TTTCore.getInstance().isLegacyMinecraftVersion() ? "-legacy" : ""),
                    Material.IRON_SWORD);
    public static final ConfigKey<Material> GUN_ITEM =
            new ConfigKey(Material.class, "gun-item"
                    + (TTTCore.getInstance().isLegacyMinecraftVersion() ? "-legacy" : ""),
                    MaterialHelper.instance().IRON_HORSE_ARMOR);
    public static final ConfigKey<Integer> CROWBAR_DAMAGE = new ConfigKey<>(Integer.class, "crowbar-damage");
    public static final ConfigKey<Boolean> REQUIRE_AMMO_FOR_GUNS
            = new ConfigKey<>(Boolean.class, "require-ammo-for-guns");
    public static final ConfigKey<Integer> INITIAL_AMMO = new ConfigKey<>(Integer.class, "initial-ammo");

    // Karma settings
    public static final ConfigKey<Boolean> KARMA_STRICT = new ConfigKey<>(Boolean.class, "karma-strict");
    public static final ConfigKey<Integer> KARMA_STARTING = new ConfigKey<>(Integer.class, "karma-starting");
    public static final ConfigKey<Integer> KARMA_MAX = new ConfigKey<>(Integer.class, "karma-max");
    public static final ConfigKey<Double> KARMA_RATIO = new ConfigKey<>(Double.class, "karma-ratio");
    public static final ConfigKey<Integer> KARMA_KILL_PENALTY = new ConfigKey<>(Integer.class, "karma-kill-penalty");
    public static final ConfigKey<Integer> KARMA_ROUND_INCREMENT
            = new ConfigKey<>(Integer.class, "karma-round-increment");
    public static final ConfigKey<Integer> KARMA_CLEAN_BONUS = new ConfigKey<>(Integer.class, "karma-clean-bonus");
    public static final ConfigKey<Double> KARMA_CLEAN_HALF = new ConfigKey<>(Double.class, "karma-clean-half");
    public static final ConfigKey<Integer> KARMA_TRAITORDMG_RATIO
            = new ConfigKey<>(Integer.class, "karma-traitordmg-ratio");
    public static final ConfigKey<Integer> KARMA_TRAITORKILL_BONUS
            = new ConfigKey<>(Integer.class, "karma-traitorkill-bonus");
    public static final ConfigKey<Integer> KARMA_LOW_AUTOKICK = new ConfigKey<>(Integer.class, "karma-low-autokick");
    public static final ConfigKey<Boolean> KARMA_LOW_BAN = new ConfigKey<>(Boolean.class, "karma-low-ban");
    public static final ConfigKey<Integer> KARMA_LOW_BAN_MINUTES
            = new ConfigKey<>(Integer.class, "karma-low-ban-minutes");
    public static final ConfigKey<Boolean> KARMA_PERSIST = new ConfigKey<>(Boolean.class, "karma-persist");
    public static final ConfigKey<Boolean> KARMA_DAMAGE_REDUCTION
            = new ConfigKey<>(Boolean.class, "karma-damage-reduction");
    public static final ConfigKey<Boolean> KARMA_ROUND_TO_ONE = new ConfigKey<>(Boolean.class, "karma-round-to-one");
    public static final ConfigKey<Boolean> KARMA_DEBUG = new ConfigKey<>(Boolean.class, "karma-debug");

    // Operating settings
    public static final ConfigKey<OperatingMode> OPERATING_MODE
            = new ConfigKey<>(OperatingMode.class, "operating-mode", OperatingMode.STANDARD);
    public static final ConfigKey<CycleMode> MAP_CYCLE_MODE
            = new ConfigKey<>(CycleMode.class, "map-cycle-mode", CycleMode.SHUFFLE);
    public static final ConfigKey<Integer> MAP_CYCLE_ROUND_LIMIT
            = new ConfigKey<>(Integer.class, "map-cycle-round-limit");
    public static final ConfigKey<Integer> MAP_CYCLE_TIME_LIMIT
            = new ConfigKey<>(Integer.class, "map-cycle-time-limit");
    public static final ConfigKey<String> RETURN_SERVER = new ConfigKey<>(String.class, "return-server");

    // Plugin settings
    public static final ConfigKey<Boolean> VERBOSE_LOGGING = new ConfigKey<>(Boolean.class, "verbose-logging");
    public static final ConfigKey<String> LOCALE = new ConfigKey<>(String.class, "locale");
    public static final ConfigKey<Boolean> ENABLE_AUTO_UPDATE = new ConfigKey<>(Boolean.class, "enable-auto-update");
    public static final ConfigKey<Boolean> ENABLE_TELEMETRY = new ConfigKey<>(Boolean.class, "enable-telemetry");
    public static final ConfigKey<Boolean> ENABLE_METRICS = new ConfigKey<>(Boolean.class, "enable-metrics");

    private TypeToken<T> type;
    private String configKey;
    private T defaultValue;

    private ConfigKey(TypeToken<T> type, String configKey, T defaultValue) {
        this.type = type;
        this.configKey = configKey;
        this.defaultValue = defaultValue;

        keys.add(this);
    }

    private ConfigKey(TypeToken<T> type, String configKey) {
        this(type, configKey, null);
    }

    private ConfigKey(Class<T> type, String configKey, T defaultValue) {
        this(TypeToken.of(type), configKey, defaultValue);
    }

    private ConfigKey(Class<T> type, String configKey) {
        this(type, configKey, null);
    }

    TypeToken<T> getType() {
        return type;
    }

    String getConfigKey() {
        return configKey;
    }

    T getDefault() {
        return defaultValue;
    }

    static Set<ConfigKey<?>> getAllKeys() {
        return keys;
    }

}
