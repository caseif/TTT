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

package net.caseif.ttt.util.helper.gamemode;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.constant.Color;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

/**
 * Static utility class for ban-related functionality.
 */
public final class BanHelper {

    private BanHelper() {
    }

    /**
     * Bans the player by the specified UUID from using TTT for a set amount of time.
     *
     * @param player  the UUID of the player to ban
     * @param minutes the length of time to ban the player for
     */
    public static void ban(UUID player, int minutes) throws InvalidConfigurationException, IOException {
        File f = new File(TTTCore.getPlugin().getDataFolder(), "bans.yml");
        YamlConfiguration y = new YamlConfiguration();
        Player p = Bukkit.getPlayer(player);
        y.load(f);
        long unbanTime = minutes < 0 ? -1 : System.currentTimeMillis() / 1000L + (minutes * 60);
        y.set(player.toString(), unbanTime);
        y.save(f);
        if (p != null) {
            Optional<Challenger> ch = TTTCore.mg.getChallenger(p.getUniqueId());
            if (ch.isPresent()) {
                ch.get().removeFromRound();
            }
        }
    }

    public static boolean pardon(UUID uuid) throws InvalidConfigurationException, IOException {
        File f = new File(TTTCore.getPlugin().getDataFolder(), "bans.yml");
        YamlConfiguration y = new YamlConfiguration();
        Player p = Bukkit.getPlayer(uuid);
        y.load(f);
        if (y.contains(uuid.toString())) {
            y.set(uuid.toString(), null);
            y.save(f);
            if (TTTCore.config.get(ConfigKey.VERBOSE_LOGGING)) {
                TTTCore.log.info(TTTCore.locale.getLocalizable("info.personal.pardon")
                        .withReplacements(p != null ? p.getName() : uuid.toString()).localize());
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether the given UUID is banned from using TTT and notifies the
     * player if so.
     *
     * @param uuid The UUID to check
     * @return Whether the UUID is banend from using TTT
     */
    public static boolean checkBan(UUID uuid) {
        File f = new File(TTTCore.getPlugin().getDataFolder(), "bans.yml");
        YamlConfiguration y = new YamlConfiguration();
        try {
            y.load(f);
            if (y.isSet(uuid.toString())) {
                long unbanTime = y.getLong(uuid.toString());
                if (unbanTime != -1 && unbanTime <= System.currentTimeMillis() / 1000L) {
                    BanHelper.pardon(uuid);
                } else {
                    Localizable loc;
                    if (unbanTime == -1) {
                        loc = TTTCore.locale.getLocalizable("info.personal.ban.perm");
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(unbanTime * 1000L);
                        String year = Integer.toString(cal.get(Calendar.YEAR));
                        String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
                        String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                        String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
                        String min = Integer.toString(cal.get(Calendar.MINUTE));
                        String sec = Integer.toString(cal.get(Calendar.SECOND));
                        min = min.length() == 1 ? "0" + min : min;
                        sec = sec.length() == 1 ? "0" + sec : sec;
                        //TODO: localize time/date (UGH)
                        loc = TTTCore.locale.getLocalizable("info.personal.ban.temp.until").withReplacements(
                                hour + ":" + min + ":" + sec, month + "/" + day + "/" + year + ".");
                    }
                    loc.withPrefix(Color.ERROR);
                    loc.sendTo(Bukkit.getPlayer(uuid));
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            TTTCore.log.warning("Failed to load bans from disk!");
        }
        return false;
    }

}
