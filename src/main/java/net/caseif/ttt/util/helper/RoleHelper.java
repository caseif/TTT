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
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.MiscUtil;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.challenger.Team;
import net.caseif.flint.round.Round;

import java.util.Random;

/**
 * Static utility class for role-related functionality.
 */
public class RoleHelper {

    public static void assignRoles(Round round) {
        int players = round.getChallengers().size();
        int tLimit = MiscUtil.clamp((int) (players * ConfigHelper.TRAITOR_RATIO), 1, players - 1);
        Team iTeam = round.getOrCreateTeam(Role.INNOCENT);
        Team tTeam = round.getOrCreateTeam(Role.TRAITOR);
        for (Challenger ch : round.getChallengers()) {
            iTeam.addChallenger(ch);
            MiscUtil.broadcast(round, TTTCore.locale.getLocalizable("info.global.round.event.started")
                    .withPrefix(Constants.Color.INFO.toString()));
        }
        while (tTeam.getChallengers().size() < tLimit) {
            Random randomGenerator = new Random();
            Challenger tPlayer = round.getChallengers().get(randomGenerator.nextInt(players));
            if (tPlayer.getTeam().get().getId().equals(Role.INNOCENT)) {
                tTeam.addChallenger(tPlayer);
            }
        }
        int dLimit = (int) (players * ConfigHelper.DETECTIVE_RATIO);
        if (players >= ConfigHelper.MINIMUM_PLAYERS_FOR_DETECTIVE && dLimit == 0) {
            dLimit += 1;
        }
        int detectiveCount = 0;
        while (detectiveCount < dLimit) {
            Random randomGenerator = new Random();
            Challenger dPlayer = round.getChallengers().get(randomGenerator.nextInt(players));
            if (!dPlayer.getMetadata().has(Role.DETECTIVE)) {
                dPlayer.getMetadata().set(Role.DETECTIVE, true);
                detectiveCount++;
            }
        }
    }

}
