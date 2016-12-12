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

package net.caseif.ttt.util.helper.platform;

import static com.google.common.base.Preconditions.checkNotNull;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.constant.Color;
import net.caseif.ttt.util.constant.Role;

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

    private TitleHelper() {
    }

    @SuppressWarnings("deprecation")
    public static void sendStatusTitle(Player player, String role) {
        if (TTTCore.config.get(ConfigKey.SEND_TITLES)) {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null!");
            }
            role = role.toLowerCase();
            String title = TTTCore.locale.getLocalizable("info.personal.status.role." + role + ".title")
                    .localizeFor(player);
            String color;
            switch (role) {
                case Role.INNOCENT: {
                    color = Color.INNOCENT;
                    break;
                }
                case Role.DETECTIVE: {
                    color = Color.DETECTIVE;
                    break;
                }
                default: {
                    color = Color.TRAITOR;
                    break;
                }
            }
            if (TTTCore.config.get(ConfigKey.LARGE_STATUS_TITLES)) {
                player.sendTitle(ChatColor.getByChar(color.charAt(1)) + title, "");
            } else {
                player.sendTitle("", ChatColor.getByChar(color.charAt(1)) + title);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void sendVictoryTitle(Round round, boolean traitorVictory) {
        if (TTTCore.config.get(ConfigKey.SEND_TITLES)) {
            checkNotNull(round, "Round cannot be null!");
            Localizable loc = TTTCore.locale.getLocalizable("info.global.round.event.end."
                    + (traitorVictory ? Role.TRAITOR : Role.INNOCENT) + ".min");
            ChatColor color = ChatColor.getByChar(
                    (traitorVictory ? Color.TRAITOR : Color.INNOCENT).charAt(1)
            );
            for (Challenger ch : round.getChallengers()) {
                Player pl = Bukkit.getPlayer(ch.getUniqueId());
                if (TTTCore.config.get(ConfigKey.LARGE_VICTORY_TITLES)) {
                    Bukkit.getPlayer(ch.getUniqueId()).sendTitle(color + loc.localizeFor(pl), "");
                } else {
                    Bukkit.getPlayer(ch.getUniqueId()).sendTitle("", color + loc.localizeFor(pl));
                }
            }
        }
    }

}
