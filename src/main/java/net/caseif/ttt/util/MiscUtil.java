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
import net.caseif.ttt.Main;

import java.io.File;
import java.util.UUID;

import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.MGPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class MiscUtil {

	/**
	 * Determines whether a given {@link MGPlayer player} is marked as a
	 * Traitor.
	 *
	 * @param player the player to check
	 * @return whether the player is a traitor
	 */
	public static boolean isTraitor(MGPlayer player) {
		return player.getTeam() != null && player.getTeam().equals("Traitor");
	}

	/**
	 * Bans the player by the specified UUID from using TTT for a set amount of
	 * time.
	 *
	 * @param player  the UUID of the player to ban
	 * @param minutes the length of time to ban the player for.
	 * @return whether the player was successfully banned
	 */
	public static boolean ban(UUID player, int minutes) {
		File f = new File(Main.plugin.getDataFolder(), "bans.yml");
		YamlConfiguration y = new YamlConfiguration();
		try {
			y.load(f);
			long unbanTime = minutes < 0 ? -1 : System.currentTimeMillis() / 1000L + (minutes * 60);
			y.set(player.toString(), unbanTime);
			y.save(f);
			Player p = Bukkit.getPlayer(player);
			if (p != null) {
				MGPlayer m = Main.mg.getMGPlayer(p.getName());
				m.removeFromRound();
			}
			return true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Main.mg.log(getMessage("error.plugin.ban", null, false, player.toString()), LogLevel.WARNING);
		}
		return false;
	}

	public static boolean pardon(UUID player) {
		File f = new File(Main.plugin.getDataFolder(), "bans.yml");
		YamlConfiguration y = new YamlConfiguration();
		try {
			y.load(f);
			y.set(player.toString(), null);
			y.save(f);
			if (Config.VERBOSE_LOGGING) {
				Player p = Bukkit.getPlayer(player);
				if (p != null) {
					Main.mg.log(p.getName() + "'s ban has been lifted", LogLevel.INFO);
				}
				else {
					Main.mg.log(player.toString() + "'s ban has been lifted", LogLevel.INFO);
				}
			}
			return true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Main.mg.log(getMessage("error.plugin.pardon", null, false, player.toString()), LogLevel.WARNING);
		}
		return false;
	}

	public static String getMessage(String key, ChatColor color, boolean prefix, String... replacements) {
		if (color != null) {
			for (int i = 0; i < replacements.length; i++) {
				if (!replacements[i].equals(ChatColor.stripColor(replacements[i])) &&
						!replacements[i].endsWith(color.toString())) {
					replacements[i] = replacements[i] + color.toString();
				}
			}
		}
		return (color != null ? color : "") + (prefix ? "[TTT] " : "") + Main.locale.getMessage(key, replacements);
	}

	public static String getMessage(String key, ChatColor color, String... replacements) {
		return getMessage(key, color, true, replacements);
	}

	public static String fromNullableString(String nullable) {
		return nullable == null ? "" : nullable;
	}

}
