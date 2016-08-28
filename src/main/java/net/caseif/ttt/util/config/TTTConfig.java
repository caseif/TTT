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
import net.caseif.ttt.util.helper.io.FileHelper;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static utility class for config-related functionality.
 */
public final class TTTConfig {

    private static final ImmutableMap<String, String> LEGACY_NODES;

    private final FileConfiguration config;

    private final Map<ConfigKey<?>, Object> map = new HashMap<>();

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

    public TTTConfig(FileConfiguration config) {
        this.config = config;

        for (ConfigKey<?> key : ConfigKey.getAllKeys()) {
            set(key);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ConfigKey<T> key) {
        return (T) map.get(key);
    }

    @SuppressWarnings("unchecked")
    private void set(ConfigKey<?> key) {
        if (key.getType() == Integer.class) {
            map.put(key, getInt(key.getConfigKey()));
        } else if (key.getType() == Double.class) {
            map.put(key, getDouble(key.getConfigKey()));
        } else if (key.getType() == Boolean.class) {
            map.put(key, getBoolean(key.getConfigKey()));
        } else if (key.getType() == String.class) {
            map.put(key, getString(key.getConfigKey()));
        } else if (key.getType() == List.class) {
            map.put(key, getList(key.getConfigKey()));
        } else if (key.getType() == Material.class) {
            map.put(key, getMaterial(key.getConfigKey(), ((ConfigKey<Material>) key).getDefault()));
        } else if (key.getType() == OperatingMode.class) {
            map.put(key, getOperatingMode(key.getConfigKey(), ((ConfigKey<OperatingMode>) key).getDefault()));
        } else if (key.getType() == CycleMode.class) {
            map.put(key, getCycleMode(key.getConfigKey(), ((ConfigKey<CycleMode>) key).getDefault()));
        }
    }

    private String getString(String key) {
        String value = config.getString(key);
        if (value != null) {
            if (value.contains("Â§")) { // fix encoding mistakes on Windoofs
                value = value.replace("Â§", "§");
            }
            return value;
        }
        return "";
    }

    private boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    private int getInt(String key) {
        return config.getInt(key);
    }

    private double getDouble(String key) {
        return config.getDouble(key);
    }

    private List<?> getList(String key) {
        return config.getList(key);
    }

    private Material getMaterial(String key, Material fallback) {
        Material m = Material.getMaterial(config.getString(key));
        return m != null ? m : fallback;
    }

    private OperatingMode getOperatingMode(String key, OperatingMode fallback) {
        OperatingMode mode;
        try {
            mode = OperatingMode.valueOf(getString(key).toUpperCase());
            if (mode == OperatingMode.DEDICATED && TTTCore.mg.getArenas().size() == 0) {
                mode = OperatingMode.STANDARD;

                TTTCore.log.warning(TTTCore.locale.getLocalizable("error.plugin.dedicated-no-arenas").localize());
                TTTCore.log.warning(TTTCore.locale.getLocalizable("error.plugin.dedicated-fallback").localize());
            }
        } catch (IllegalArgumentException ex) {
            TTTCore.getPlugin().getLogger().warning(TTTCore.locale.getLocalizable("error.plugin.config.fallback")
                    .withReplacements(key, fallback.toString()).localize());
            mode = OperatingMode.STANDARD;
        }
        return mode;
    }

    private CycleMode getCycleMode(String key, CycleMode fallback) {
        CycleMode mode;
        try {
            mode = CycleMode.valueOf(getString(key).toUpperCase());
        } catch (IllegalArgumentException ex) {
            TTTCore.getPlugin().getLogger().warning(TTTCore.locale.getLocalizable("error.plugin.config.fallback")
                    .withReplacements(key, fallback.toString()).localize());
            mode = CycleMode.SHUFFLE;
        }
        return mode;
    }

    private Set<String> getLegacyKeys(final String modernKey) {
        return new HashSet<>(Collections2.filter(LEGACY_NODES.keySet(), new Predicate<String>() {
            @Override
            public boolean apply(String key) {
                return LEGACY_NODES.get(key).equals(modernKey);
            }
        }));
    }

    // this is essentially a ghetto-ass YAML parser that doesn't support nesting in any form
    public void addMissingKeys() throws InvalidConfigurationException, IOException {
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
        String currentList = null;
        StringBuilder buffer = null; // if this is null while we're in a list, the list isn't present in user config
        while ((line = stockConfig.readLine()) != null) { // iterate the lines of the internal config file
            line = line.trim();
            if (!line.startsWith("#") && !line.isEmpty()) { // check that the line's not a comment or spacer
                if (currentList != null && !line.startsWith("-")) { // implying we've read through the entire list
                    if (buffer != null) { // list is in user config - we need to write the user values manually
                        for (String item : userConfig.getStringList(currentList)) {
                            sb.append("- ").append(item).append(newlineChar);
                        }
                        sb.append(buffer);
                    }
                    currentList = null;
                    buffer = null;
                }
                if (buffer != null) { // list is already in user config - don't need to read the stock values
                    continue;
                }
                if (currentList == null && line.contains(":")) { // rudimentary validation
                    //TODO: this method doesn't support nested keys, but it doesn't need to atm anyway
                    String key = line.split(":")[0]; // derive the key
                    if (TTTCore.getPlugin().getConfig().isList(key)) {
                        currentList = key;
                        if (userConfig.isList(key)) {
                            buffer = new StringBuilder();
                        }
                        sb.append(line).append('\n');
                        continue;
                    }

                    String internalValue = line.substring(key.length() + 1, line.length()).trim(); // derive the value
                    // get the value of the key as defined in the user config
                    String userValue = null;
                    if (userConfig.contains(key.trim())) {
                        userValue = userConfig.getString(key.trim());
                    } else {
                        Set<String> legacyKeys = getLegacyKeys(key.trim());
                        for (String lgcy : legacyKeys) {
                            if (userConfig.contains(lgcy)) {
                                userValue = userConfig.getString(lgcy);
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
                            // clean up the value-to-write if it's a double
                            userValue = BigDecimal.valueOf(Double.parseDouble(userValue))
                                    .stripTrailingZeros().toPlainString();
                        }
                        sb.append(key).append(": ").append(userValue).append(newlineChar);
                        continue;
                    }
                }
            }
            // just copy the line directly if it hasn't already been reconstructed
            (buffer != null ? buffer : sb).append(line).append(newlineChar);
        }
        FileHelper.copyFile(userConfigFile, new File(userConfigFile.getParentFile(), "config.yml.old"));
        FileWriter w = new FileWriter(userConfigFile);
        w.append(sb.toString());
        w.flush();
    }

}
