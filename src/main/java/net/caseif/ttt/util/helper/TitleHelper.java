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

import static com.google.common.base.Preconditions.checkNotNull;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants;

import net.caseif.crosstitles.TitleUtil;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Static utility class for title-related functionality.
 */
public final class TitleHelper {

    public static void sendStatusTitle(Player player, String role) {
        if (ConfigHelper.SEND_TITLES && TitleUtil.areTitlesSupported()) {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null!");
            }
            role = role.toLowerCase();
            String title = TTTCore.locale.getLocalizable("info.personal.status.role." + role + ".title")
                    .localizeFor(player);
            ChatColor color;
            switch (role) {
                case Constants.Role.INNOCENT: {
                    color = Constants.Color.INNOCENT;
                    break;
                }
                case Constants.Role.DETECTIVE: {
                    color = Constants.Color.DETECTIVE;
                    break;
                }
                default: {
                    color = Constants.Color.TRAITOR;
                    break;
                }
            }
            if (ConfigHelper.SMALL_STATUS_TITLES) {
                TitleUtil.sendTitle(player, "", ChatColor.RESET, title, color);
            } else {
                TitleUtil.sendTitle(player, title, color);
            }
        }
    }

    public static void sendVictoryTitle(Round round, boolean traitorVictory) {
        if (ConfigHelper.SEND_TITLES && TitleUtil.areTitlesSupported()) {
            checkNotNull(round, "Round cannot be null!");
            Localizable loc = TTTCore.locale.getLocalizable("info.global.round.event.end."
                    + (traitorVictory ? Constants.Role.TRAITOR : Constants.Role.INNOCENT) + ".min");
            ChatColor color = traitorVictory ? Constants.Color.TRAITOR : Constants.Color.INNOCENT;
            for (Challenger ch : round.getChallengers()) {
                Player pl = Bukkit.getPlayer(ch.getUniqueId());
                if (ConfigHelper.SMALL_VICTORY_TITLES) {
                    TitleUtil.sendTitle(Bukkit.getPlayer(ch.getUniqueId()), "", ChatColor.RESET, loc.localizeFor(pl),
                            color);
                } else {
                    TitleUtil.sendTitle(Bukkit.getPlayer(ch.getUniqueId()), loc.localizeFor(pl), color);
                }
            }
        }
    }

}
