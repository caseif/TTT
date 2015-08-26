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
package net.caseif.ttt.util.helper;

import net.caseif.ttt.TTTCore;

import net.caseif.flint.challenger.Challenger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

/**
 * Static utility class for ban-related functionality.
 */
public final class BanHelper {

    /**
     * Bans the player by the specified UUID from using TTT for a set amount of time.
     *
     * @param player  the UUID of the player to ban
     * @param minutes the length of time to ban the player for.
     * @return whether the player was successfully banned
     */
    public static boolean ban(UUID player, int minutes) {
        File f = new File(TTTCore.getInstance().getDataFolder(), "bans.yml");
        YamlConfiguration y = new YamlConfiguration();
        Player p = Bukkit.getPlayer(player);
        try {
            y.load(f);
            long unbanTime = minutes < 0 ? -1 : System.currentTimeMillis() / 1000L + (minutes * 60);
            y.set(player.toString(), unbanTime);
            y.save(f);
            if (p != null) {
                Challenger ch = TTTCore.mg.getChallenger(p.getUniqueId()).get(); //TODO: figure out why I added a TODO
                ch.removeFromRound();
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            TTTCore.getInstance().logWarning("error.plugin.ban",
                    p != null ? p.getName() : player.toString());
        }
        return false;
    }

    public static boolean pardon(UUID uuid) {
        File f = new File(TTTCore.getInstance().getDataFolder(), "bans.yml");
        YamlConfiguration y = new YamlConfiguration();
        Player p = Bukkit.getPlayer(uuid);
        try {
            y.load(f);
            y.set(uuid.toString(), null);
            y.save(f);
            if (ConfigHelper.VERBOSE_LOGGING) {
                TTTCore.log.info(p != null ? p.getName() : uuid.toString() + "'s ban has been lifted");
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            TTTCore.getInstance().logWarning("error.plugin.pardon", p != null ? p.getName() : uuid.toString());
        }
        return false;
    }

}
