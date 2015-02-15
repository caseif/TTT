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
package net.caseif.ttt;

import net.caseif.ttt.util.FileUtil;
import net.caseif.ttt.util.NumUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Config {

	public static final double DETECTIVE_RATIO;
	public static final int MAXIMUM_PLAYERS;
	public static final double TRAITOR_RATIO;
	public static final int MINIMUM_PLAYERS_FOR_DETECTIVE;
	public static final int SCANNER_CHARGE_TIME;
	public static final int CROWBAR_DAMAGE;
	public static final boolean GUNS_OUTSIDE_ARENAS;
	public static final boolean REQUIRE_AMMO_FOR_GUNS;
	public static final int INITIAL_AMMO;
	public static final boolean KARMA_PERSISTENCE;
	public static final int DEFAULT_KARMA;
	public static final int MAX_KARMA;
	public static final int KARMA_HEAL;
	public static final int KARMA_CLEAN_BONUS;
	public static final double KARMA_CLEAN_HALF;
	public static final double DAMAGE_PENALTY;
	public static final int KILL_PENALTY;
	public static final int T_DAMAGE_REWARD;
	public static final int TBONUS;
	public static final boolean KARMA_ROUND_TO_ONE;
	public static final boolean KARMA_STRICT;
	public static final int KARMA_KICK;
	public static final boolean KARMA_BAN;
	public static final int KARMA_BAN_TIME;
	public static final boolean KARMA_DEBUG;
	public static final boolean VERBOSE_LOGGING;
	public static final boolean DAMAGE_REDUCTION;
	public static final boolean ENABLE_AUTO_UPDATE;
	public static final boolean ENABLE_METRICS;
	public static final String LOCALE;
	public static final int MINIMUM_PLAYERS;
	public static final int TIME_LIMIT;
	public static final int SETUP_TIME;
	public static final String SB_ALIVE_PREFIX;
	public static final String SB_MIA_PREFIX;
	public static final String SB_DEAD_PREFIX;
	public static final String SB_I_INNOCENT_PREFIX;
	public static final String SB_I_TRAITOR_PREFIX;
	public static final String SB_I_DETECTIVE_PREFIX;
	public static final String SB_T_INNOCENT_PREFIX;
	public static final String SB_T_TRAITOR_PREFIX;
	public static final String SB_T_DETECTIVE_PREFIX;
	public static final boolean SB_USE_SIDEBAR;

	static {
		TIME_LIMIT = getInt("time-limit");
		SETUP_TIME = getInt("setup-time");
		MINIMUM_PLAYERS = getInt("minimum-players");
		MAXIMUM_PLAYERS = getInt("maximum-players");
		TRAITOR_RATIO = getDouble("traitor-ratio");
		DETECTIVE_RATIO = getDouble("detective-ratio");
		MINIMUM_PLAYERS_FOR_DETECTIVE = getInt("minimum-players-for-detective");
		SCANNER_CHARGE_TIME = getInt("scanner-charge-time");
		CROWBAR_DAMAGE = getInt("crowbar-damage");
		GUNS_OUTSIDE_ARENAS = getBoolean("guns-outside-arenas");
		REQUIRE_AMMO_FOR_GUNS = getBoolean("require-ammo-for-guns");
		INITIAL_AMMO = getInt("initial-ammo");
		KARMA_PERSISTENCE = getBoolean("karma-persistence");
		DEFAULT_KARMA = getInt("default-karma");
		MAX_KARMA = getInt("max-karma");
		KARMA_HEAL = getInt("karma-heal");
		KARMA_CLEAN_BONUS = getInt("karma-clean-bonus");
		KARMA_CLEAN_HALF = getDouble("karma-clean-half");
		DAMAGE_PENALTY = getDouble("damage-penalty");
		KILL_PENALTY = getInt("kill-penalty");
		T_DAMAGE_REWARD = getInt("t-damage-reward");
		TBONUS = getInt("tbonus");
		KARMA_ROUND_TO_ONE = getBoolean("karma-round-to-one");
		KARMA_STRICT = getBoolean("karma-strict");
		KARMA_KICK = getInt("info.personal.kick.karma");
		KARMA_BAN = getBoolean("info.personal.ban.temp.karma");
		KARMA_BAN_TIME = getInt("karma-ban-time");
		DAMAGE_REDUCTION = getBoolean("damage-reduction");
		KARMA_DEBUG = getBoolean("karma-debug");
		VERBOSE_LOGGING = getBoolean("verbose-logging");
		ENABLE_AUTO_UPDATE = getBoolean("enable-auto-update");
		ENABLE_METRICS = getBoolean("enable-metrics");
		LOCALE = getString("locale");
		SB_ALIVE_PREFIX = getString("sb-alive-prefix");
		SB_MIA_PREFIX = getString("sb-mia-prefix");
		SB_DEAD_PREFIX = getString("sb-dead-prefix");
		SB_I_INNOCENT_PREFIX = getString("sb-i-innocent-prefix");
		SB_I_TRAITOR_PREFIX = getString("sb-i-traitor-prefix");
		SB_I_DETECTIVE_PREFIX = getString("sb-i-detective-prefix");
		SB_T_INNOCENT_PREFIX = getString("sb-t-innocent-prefix");
		SB_T_TRAITOR_PREFIX = getString("sb-t-traitor-prefix");
		SB_T_DETECTIVE_PREFIX = getString("sb-t-detective-prefix");
		SB_USE_SIDEBAR = getBoolean("sb-use-sidebar");
	}

	public static String getString(String key) {
		String value = Main.plugin.getConfig().getString(key);
		if (value != null) {
			if (value.contains("Â§")) { // fix encoding mistakes on Windoofs
				value = value.replace("Â§", "§");
			}
			return value;
		}
		return "";
	}

	public static boolean getBoolean(String key) {
		return Main.plugin.getConfig().getBoolean(key);
	}

	public static int getInt(String key) {
		return Main.plugin.getConfig().getInt(key);
	}

	public static double getDouble(String key) {
		return Main.plugin.getConfig().getDouble(key);
	}

	public static void addMissingKeys() throws InvalidConfigurationException, IOException {
		BufferedReader is = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/config.yml")));
		File configYml = new File(Main.plugin.getDataFolder(), "config.yml");
		YamlConfiguration yml = new YamlConfiguration();
		yml.load(configYml);
		StringBuilder sb = new StringBuilder();
		final char NEWLINE_CHAR = '\n';
		String line;
		while ((line = is.readLine()) != null) {
			if (!line.startsWith("#")) {
				if (line.contains(":")) {
					//TODO: this method doesn't support nested keys, but it doesn't need to atm anyway
					String key = line.split(":")[0];
					String value = line.substring(key.length() + 1, line.length()).trim();
					String newValue = yml.contains(key.trim()) ? yml.getString(key.trim()) : value;
					boolean equal = false;
					try {
						equal = NumberFormat.getInstance().parse(value)
								.equals(NumberFormat.getInstance().parse(newValue));
					}
					catch (ParseException ex) {
						equal = value.equals(newValue);
					}
					if (!equal) {
						String writeValue = yml.getString(key.trim());
						if (NumUtil.isDouble(writeValue)) {
							writeValue = BigDecimal.valueOf(Double.parseDouble(writeValue))
									.stripTrailingZeros().toPlainString();
						}
						sb.append(key).append(": ").append(writeValue).append(NEWLINE_CHAR);
						continue;
					}
				}
			}
			sb.append(line).append(NEWLINE_CHAR);
		}
		FileUtil.copyFile(configYml, new File(configYml.getParentFile(), "config.yml.old"));
		FileWriter w = new FileWriter(configYml);
		w.append(sb.toString());
		w.flush();
	}
}