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

package net.caseif.ttt.util.helper.gamemode;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.constant.Color;
import net.caseif.ttt.util.constant.MetadataKey;
import net.caseif.ttt.util.constant.Role;
import net.caseif.ttt.util.helper.data.DataVerificationHelper;

import com.google.common.collect.Lists;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.round.Round;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Static utility class for role-related functionality.
 */
public final class RoleHelper {

    private RoleHelper() {
    }

    public static void assignRoles(Round round) {
        int players = round.getChallengers().size();
        Team iTeam = round.getOrCreateTeam(Role.INNOCENT);
        Team tTeam = round.getOrCreateTeam(Role.TRAITOR);
        for (Challenger ch : round.getChallengers()) {
            ch.setTeam(iTeam);
        }

        int tLimit = DataVerificationHelper.clamp((int) (players * TTTCore.config.get(ConfigKey.TRAITOR_PCT)), 1,
                players - 1);
        tLimit = DataVerificationHelper.clamp(tLimit, 1, players - 1);
        List<Challenger> tList = Lists.newArrayList(round.getChallengers());
        Collections.shuffle(tList);
        for (int i = 0; i < tLimit; i++) {
            tList.get(i).setTeam(tTeam);
        }

        int dLimit = (int) (players * TTTCore.config.get(ConfigKey.DETECTIVE_PCT));
        dLimit = DataVerificationHelper.clamp(dLimit, 0, iTeam.getChallengers().size());
        if (players >= TTTCore.config.get(ConfigKey.DETECTIVE_MIN_PLAYERS) && dLimit == 0) {
            dLimit = 1;
        }
        List<Challenger> dList = Lists.newArrayList(iTeam.getChallengers());
        Collections.shuffle(dList);
        for (int i = 0; i < dLimit; i++) {
            dList.get(i).getMetadata().set(Role.DETECTIVE, true);
        }
    }

    public static String genRoleMessage(CommandSender sender, Challenger ch) {
        String color;
        String roleFrag;
        if (!ch.getTeam().isPresent()) {
            color = ChatColor.GRAY.toString();
            roleFrag = "unassigned";
        } else if (ch.getTeam().get().getId().equals(Role.TRAITOR)) {
            color = Color.TRAITOR;
            roleFrag = Role.TRAITOR;
        } else if (ch.getMetadata().containsKey(Role.DETECTIVE)) {
            color = Color.DETECTIVE;
            roleFrag = Role.DETECTIVE;
        } else {
            color = Color.INNOCENT;
            roleFrag = Role.INNOCENT;
        }

        String roleMsg = TTTCore.locale.getLocalizable("fragment." + roleFrag).withPrefix(color).localizeFor(sender)
                .toUpperCase();

        if (ch.isSpectating() && !ch.getMetadata().containsKey(MetadataKey.Player.PURE_SPECTATOR)) {
            roleMsg += TTTCore.locale.getLocalizable("fragment.deceased")
                    .withPrefix(" " + ChatColor.GRAY + "(").localizeFor(sender) + ")";
        }

        return roleMsg;
    }

    /**
     * Determines whether a given {@link Challenger challenger} is marked as a
     * Traitor.
     *
     * @param player the player to check
     * @return whether the player is a traitor
     */
    public static boolean isTraitor(Challenger player) {
        return player.getTeam().isPresent() && player.getTeam().get().getId().equals(Role.TRAITOR);
    }

}
