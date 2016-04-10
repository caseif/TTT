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

package net.caseif.ttt.util.config;

import static net.caseif.ttt.util.helper.data.DataVerificationHelper.isDouble;

import net.caseif.ttt.TTTBootstrap;
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.CycleMode;
import net.caseif.ttt.util.config.OperatingMode;
import net.caseif.ttt.util.helper.io.FileHelper;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Static utility class for config-related functionality.
 */
public final class TTTConfig {

    private static final ImmutableMap<String, String> LEGACY_NODES;

    // Round structure
    public final int PREPTIME_SECONDS;
    public final int ROUNDTIME_SECONDS;
    public final int POSTTIME_SECONDS;
    public final int MINIMUM_PLAYERS;
    public final int MAXIMUM_PLAYERS;
    public final boolean ALLOW_JOIN_AS_SPECTATOR;
    public final boolean BROADCAST_WIN_MESSAGES_TO_SERVER;

    // Traitor/Detective settings
    public final double TRAITOR_PCT;
    public final double DETECTIVE_PCT;
    public final int DETECTIVE_MIN_PLAYERS;
    public final int SCANNER_CHARGE_TIME;
    public final int KILLER_DNA_RANGE;
    public final int KILLER_DNA_BASETIME;

    // Title settings
    public final boolean SEND_TITLES;
    public final boolean LARGE_STATUS_TITLES;
    public final boolean LARGE_VICTORY_TITLES;

    // Weapon settings
    public final Material CROWBAR_ITEM;
    public final Material GUN_ITEM;
    public final int CROWBAR_DAMAGE;
    public final boolean REQUIRE_AMMO_FOR_GUNS;
    public final int INITIAL_AMMO;

    // Karma settings
    public final boolean KARMA_STRICT;
    public final int KARMA_STARTING;
    public final int KARMA_MAX;
    public final double KARMA_RATIO;
    public final int KARMA_KILL_PENALTY;
    public final int KARMA_ROUND_INCREMENT;
    public final int KARMA_CLEAN_BONUS;
    public final double KARMA_CLEAN_HALF;
    public final int KARMA_TRAITORDMG_RATIO;
    public final int KARMA_TRAITORKILL_BONUS;
    public final int KARMA_LOW_AUTOKICK;
    public final boolean KARMA_LOW_BAN;
    public final int KARMA_LOW_BAN_MINUTES;
    public final boolean KARMA_PERSIST;
    public final boolean KARMA_DAMAGE_REDUCTION;
    public final boolean KARMA_ROUND_TO_ONE;
    public final boolean KARMA_DEBUG;

    // Operating settings
    public final OperatingMode OPERATING_MODE;
    public final CycleMode MAP_CYCLE_MODE;
    public final int MAP_CYCLE_ROUND_LIMIT;
    public final int MAP_CYCLE_TIME_LIMIT;
    public final String RETURN_SERVER;

    // Plugin settings
    public final boolean VERBOSE_LOGGING;
    public final String LOCALE;
    public final boolean ENABLE_AUTO_UPDATE;
    public final boolean ENABLE_TELEMETRY;
    public final boolean ENABLE_METRICS;

    static {
        LEGACY_NODES = ImmutableMap.<String, String>builder()
                .put("setup-time", "preptime-seconds")
                .put("time-limit", "roundtime-seconds")

                .put("traitor-ratio", "traitor-pct")
                .put("detective-ratio", "detective-pct")
                .put("minimum-players-for-detective", "detective-min-players")

                .put("default-karma", "karma-starting")
                .put("max-karma", "karma-max")
                .put("damage-penalty", "karma-ratio")
                .put("kill-penalty", "karma-kill-penalty")
                .put("karma-heal", "karma-round-increment")
                .put("t-damage-reward", "karma-traitordmg-ratio")
                .put("tbonus", "karma-traitorkill-bonus")
                .put("karma-kick", "karma-low-autokick")
                .put("karma-ban", "karma-low-ban")
                .put("karma-ban-time", "karma-low-ban-minutes")
                .put("karma-persistance", "karma-persist")
                .put("damage-reduction", "karma-damage-reduction")

                .build();
    }

    public TTTConfig() {
        // Round settings
        PREPTIME_SECONDS = getInt("preptime-seconds");
        ROUNDTIME_SECONDS = getInt("roundtime-seconds");
        POSTTIME_SECONDS = getInt("posttime-seconds");
        MINIMUM_PLAYERS = getInt("minimum-players");
        MAXIMUM_PLAYERS = getInt("maximum-players");
        ALLOW_JOIN_AS_SPECTATOR = getBoolean("allow-join-as-spectator");
        BROADCAST_WIN_MESSAGES_TO_SERVER = getBoolean("broadcast-win-messages-to-server");

        // Traitor/Detective settings
        TRAITOR_PCT = getDouble("traitor-pct");
        DETECTIVE_PCT = getDouble("detective-pct");
        DETECTIVE_MIN_PLAYERS = getInt("detective-min-players");
        SCANNER_CHARGE_TIME = getInt("scanner-charge-time");
        KILLER_DNA_RANGE = getInt("killer-dna-range");
        KILLER_DNA_BASETIME = getInt("killer-dna-basetime");

        // Title settings
        SEND_TITLES = getBoolean("send-titles");
        LARGE_STATUS_TITLES = getBoolean("large-status-titles");
        LARGE_VICTORY_TITLES = getBoolean("large-victory-titles");

        // Weapon settings
        CROWBAR_ITEM = getMaterial("crowbar-item", Material.IRON_SWORD);
        GUN_ITEM = getMaterial("gun-item", Material.IRON_BARDING);
        CROWBAR_DAMAGE = getInt("crowbar-damage");
        REQUIRE_AMMO_FOR_GUNS = getBoolean("require-ammo-for-guns");
        INITIAL_AMMO = getInt("initial-ammo");

        // Karma settings
        KARMA_STRICT = getBoolean("karma-strict");
        KARMA_STARTING = getInt("karma-starting");
        KARMA_MAX = getInt("karma-max");
        KARMA_RATIO = getDouble("karma-ratio");
        KARMA_KILL_PENALTY = getInt("karma-kill-penalty");
        KARMA_ROUND_INCREMENT = getInt("karma-round-increment");
        KARMA_CLEAN_BONUS = getInt("karma-clean-bonus");
        KARMA_CLEAN_HALF = getDouble("karma-clean-half");
        KARMA_TRAITORDMG_RATIO = getInt("karma-traitordmg-ratio");
        KARMA_TRAITORKILL_BONUS = getInt("karma-traitorkill-bonus");
        KARMA_LOW_AUTOKICK = getInt("karma-low-autokick");
        KARMA_LOW_BAN = getBoolean("karma-low-ban");
        KARMA_LOW_BAN_MINUTES = getInt("karma-low-ban-minutes");
        KARMA_PERSIST = getBoolean("karma-persist");
        KARMA_DAMAGE_REDUCTION = getBoolean("karma-damage-reduction");
        KARMA_ROUND_TO_ONE = getBoolean("karma-round-to-one");
        KARMA_DEBUG = getBoolean("karma-debug");

        // Operating settings
        OperatingMode localOperatingMode;
        try {
            localOperatingMode = OperatingMode.valueOf(getString("operating-mode").toUpperCase());
            if (localOperatingMode == OperatingMode.DEDICATED && TTTCore.mg.getArenas().size() == 0) {
                localOperatingMode = OperatingMode.STANDARD;

                TTTCore.log.warning(TTTCore.locale.getLocalizable("error.plugin.dedicated-no-arenas").localize());
                TTTCore.log.warning(TTTCore.locale.getLocalizable("error.plugin.dedicated-fallback").localize());
            }
        } catch (IllegalArgumentException ex) {
            TTTCore.getPlugin().getLogger()
                    .warning("Invalid value for config key 'operating-mode' - defaulting to STANDARD");
            localOperatingMode = OperatingMode.STANDARD;
        }
        OPERATING_MODE = localOperatingMode;

        MAP_CYCLE_ROUND_LIMIT = getInt("map-cycle-round-limit");
        MAP_CYCLE_TIME_LIMIT = getInt("map-cycle-time-limit");

        CycleMode localCycleMode;
        try {
            localCycleMode = CycleMode.valueOf(getString("map-cycle-mode").toUpperCase());
        } catch (IllegalArgumentException ex) {
            TTTCore.getPlugin().getLogger()
                    .warning("Invalid value for config key 'map-cycle-mode' - defaulting to SEQUENTIAL");
            localCycleMode = CycleMode.SEQUENTIAL;
        }
        MAP_CYCLE_MODE = localCycleMode;

        RETURN_SERVER = getString("return-server");

        // Plugin settings
        VERBOSE_LOGGING = getBoolean("verbose-logging");
        LOCALE = getString("locale");
        ENABLE_AUTO_UPDATE = getBoolean("enable-auto-update");
        ENABLE_TELEMETRY = getBoolean("enable-telemetry");
        ENABLE_METRICS = getBoolean("enable-metrics");
    }

    public static String getString(String key) {
        String value = TTTBootstrap.INSTANCE.getConfig().getString(key);
        if (value != null) {
            if (value.contains("Â§")) { // fix encoding mistakes on Windoofs
                value = value.replace("Â§", "§");
            }
            return value;
        }
        return "";
    }

    public static boolean getBoolean(String key) {
        return TTTBootstrap.INSTANCE.getConfig().getBoolean(key);
    }

    public static int getInt(String key) {
        return TTTBootstrap.INSTANCE.getConfig().getInt(key);
    }

    public static double getDouble(String key) {
        return TTTBootstrap.INSTANCE.getConfig().getDouble(key);
    }

    public static Material getMaterial(String key, Material fallback) {
        Material m = Material.getMaterial(TTTBootstrap.INSTANCE.getConfig().getString(key));
        return m != null ? m : fallback;
    }

    private static String getModernKey(String legacyKey) {
        return LEGACY_NODES.get(legacyKey);
    }

    private static Set<String> getLegacyKeys(final String modernKey) {
        return new HashSet<>(Collections2.filter(LEGACY_NODES.keySet(), new Predicate<String>() {
            @Override
            public boolean apply(String key) {
                return LEGACY_NODES.get(key).equals(modernKey);
            }
        }));
    }

    public static void addMissingKeys() throws InvalidConfigurationException, IOException {
        BufferedReader stockConfig
                = new BufferedReader(new InputStreamReader(TTTBootstrap.class.getResourceAsStream("/config.yml")));
        File userConfigFile = new File(TTTBootstrap.INSTANCE.getDataFolder(), "config.yml");
        YamlConfiguration userConfig = new YamlConfiguration();
        userConfig.load(userConfigFile);
        StringBuilder sb = new StringBuilder();
        final char newlineChar = '\n';
        String line;
        // Before reading this code, understand that this method reconstructs the user config from scratch using the
        // internal config as a foundation and substituting in user-changed values where possible.
        while ((line = stockConfig.readLine()) != null) { // iterate the lines of the internal config file
            if (!line.startsWith("#")) { // check that the line's not a comment
                if (line.contains(":")) { // check that it's not a list item or something
                    //TODO: this method doesn't support nested keys, but it doesn't need to atm anyway
                    String key = line.split(":")[0]; // derive the key
                    String internalValue = line.substring(key.length() + 1, line.length()).trim(); // derive the value
                    // get the value of the key as defined in the user config
                    String userValue = null;
                    if (userConfig.contains(key.trim())) {
                        userValue = userConfig.getString(key.trim());
                    } else {
                        Set<String> legacyKeys = getLegacyKeys(key.trim());
                        for (String leg : legacyKeys) {
                            if (userConfig.contains(leg)) {
                                userValue = userConfig.getString(leg);
                                break;
                            }
                        }
                        if (userValue == null) {
                            userValue = internalValue;
                        }
                    }

                    if (key.equals("gun-item") && userValue.equals("IRON_HORSE_ARMOR")) {
                        userValue = "IRON_BARDING";
                    }

                    // This seems counterintuitive, but bear with me: essentially, the internal value will be used if
                    // the key is missing from the user config (so that `userValue == internalValue`), or if the
                    // user-defined value is effectively the same as the internal value
                    // (i.e. `1 == 1` or `1.0 == 1.00`). This ensures that the user config is as clean as possible.
                    //
                    // I had to revisit this method maybe a year after writing it while porting the plugin to Flint, and
                    // it took me about 30 minutes to figure out how (and why) the hell it worked. Hence why I wrote
                    // this comment.
                    boolean equal;
                    try {
                        // try to parse it as a number and compare it
                        equal = NumberFormat.getInstance().parse(internalValue)
                                .equals(NumberFormat.getInstance().parse(userValue));
                    } catch (ParseException ex) {
                        // it's not a number, so just compare it as a string
                        equal = internalValue.equals(userValue);
                    }
                    if (!equal) { // if they're not effectively equal, reconstruct the line using the user-defined value
                        if (isDouble(userValue)) {
                            // clean the value-to-write up if it's a double
                            userValue = BigDecimal.valueOf(Double.parseDouble(userValue))
                                    .stripTrailingZeros().toPlainString();
                        }
                        sb.append(key).append(": ").append(userValue).append(newlineChar);
                        continue;
                    }
                }
            }
            // just copy the line directly if it hasn't already been reconstructed
            sb.append(line).append(newlineChar);
        }
        FileHelper.copyFile(userConfigFile, new File(userConfigFile.getParentFile(), "config.yml.old"));
        FileWriter w = new FileWriter(userConfigFile);
        w.append(sb.toString());
        w.flush();
    }
}
