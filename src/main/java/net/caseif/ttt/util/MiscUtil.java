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

import static net.caseif.ttt.util.Constants.Color.DETECTIVE;
import static net.caseif.ttt.util.Constants.Color.INNOCENT;
import static net.caseif.ttt.util.Constants.Color.TRAITOR;

import net.caseif.ttt.Config;
import net.caseif.ttt.TTTCore;

import net.caseif.crosstitles.TitleUtil;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class MiscUtil {

    /**
     * Determines whether a given {@link Challenger challenger} is marked as a
     * Traitor.
     *
     * @param player the player to check
     * @return whether the player is a traitor
     */
    public static boolean isTraitor(Challenger player) {
        return player.getTeam().isPresent() && player.getTeam().get().getId().equals("traitor");
    }

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
            if (Config.VERBOSE_LOGGING) {
                TTTCore.log.info(p != null ? p.getName() : uuid.toString() + "'s ban has been lifted");
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            TTTCore.getInstance().logWarning("error.plugin.pardon", p != null ? p.getName() : uuid.toString());
        }
        return false;
    }

    public static String fromNullableString(String nullable) {
        return nullable == null ? "" : nullable;
    }

    public static void sendStatusTitle(Player player, String role) {
        if (Config.SEND_TITLES && TitleUtil.areTitlesSupported()) {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null!");
            }
            role = role.toLowerCase();
            String title = TTTCore.locale.getLocalizable("info.personal.status.role." + role + ".title")
                    .localizeFor(player);
            ChatColor color;
            if (role.equals("innocent")) {
                color = INNOCENT;
            } else if (role.equals("detective")) {
                color = DETECTIVE;
            } else {
                color = TRAITOR;
            }
            if (Config.SMALL_STATUS_TITLES) {
                TitleUtil.sendTitle(player, "", ChatColor.RESET, title, color);
            } else {
                TitleUtil.sendTitle(player, title, color);
            }
        }
    }

    public static void sendVictoryTitle(Round round, boolean traitorVictory) {
        if (Config.SEND_TITLES && TitleUtil.areTitlesSupported()) {
            if (round == null) {
                throw new IllegalArgumentException("Round cannot be null!");
            }
            Localizable loc = TTTCore.locale.getLocalizable("info.global.round.event.end."
                    + (traitorVictory ? "traitor" : "innocent") + ".min");
            ChatColor color = traitorVictory ? TRAITOR : INNOCENT;
            for (Challenger ch : round.getChallengers()) {
                Player pl = Bukkit.getPlayer(ch.getUniqueId());
                if (Config.SMALL_VICTORY_TITLES) {
                    TitleUtil.sendTitle(Bukkit.getPlayer(ch.getUniqueId()), "", ChatColor.RESET, loc.localizeFor(pl),
                            color);
                } else {
                    TitleUtil.sendTitle(Bukkit.getPlayer(ch.getUniqueId()), loc.localizeFor(pl), color);
                }
            }
        }
    }

    /**
     * Broadcasts a {@link Localizable} to a {@link Round}.
     *
     * @param round The {@link Round} to broadcast to
     * @param localizable The {@link Localizable} to broadcast
     */
    public static void broadcast(Round round, Localizable localizable) {
        for (Challenger ch : round.getChallengers()) {
            Player pl = Bukkit.getPlayer(ch.getUniqueId());
            assert pl != null;
            localizable.sendTo(pl);
        }
    }

}
