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
package net.caseif.ttt.managers;

import static net.caseif.ttt.util.MiscUtil.fromNullableString;

import net.caseif.ttt.Config;
import net.caseif.ttt.util.MiscUtil;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class ScoreManager {

    public static final boolean ENTRY_SUPPORT;

    //TODO: make this private and abstract mutation of it
    public static HashMap<String, ScoreManager> sbManagers = new HashMap<>();

    public static ScoreboardManager manager = Bukkit.getScoreboardManager();

    static {
        boolean support = false;
        try {
            Scoreboard.class.getMethod("getEntries");
            support = true;
        } catch (NoSuchMethodException ignored) {
        }
        ENTRY_SUPPORT = support;
    }

    public Scoreboard innocent;
    public Scoreboard traitor;
    public Objective iObj;
    public Objective tObj;
    public Round round;
    //TODO: fucking clean this next declaration up
    public Team
            iTeamIA, iTeamIM, iTeamID, iTeamTA, iTeamTM, iTeamTD, iTeamDA, iTeamDM, iTeamDD,
            tTeamIA, tTeamIM, tTeamID, tTeamTA, tTeamTM, tTeamTD, tTeamDA, tTeamDM, tTeamDD;

    @SuppressWarnings("deprecation")
    public ScoreManager(Round round) {

        this.round = round;
        innocent = manager.getNewScoreboard();
        traitor = manager.getNewScoreboard();

        iObj = innocent.registerNewObjective("p", "dummy");
        tObj = traitor.registerNewObjective("p", "dummy");
        iObj.setDisplayName("Players");
        tObj.setDisplayName("Players");
        iObj.setDisplaySlot(Config.SB_USE_SIDEBAR ? DisplaySlot.SIDEBAR : DisplaySlot.PLAYER_LIST);
        tObj.setDisplaySlot(Config.SB_USE_SIDEBAR ? DisplaySlot.SIDEBAR : DisplaySlot.PLAYER_LIST);

        iTeamIA = innocent.registerNewTeam("ia");
        iTeamIM = innocent.registerNewTeam("im");
        iTeamID = innocent.registerNewTeam("item.id.name");
        iTeamTA = innocent.registerNewTeam("ta");
        iTeamTM = innocent.registerNewTeam("tm");
        iTeamTD = innocent.registerNewTeam("td");
        iTeamDA = innocent.registerNewTeam("da");
        iTeamDM = innocent.registerNewTeam("dm");
        iTeamDD = innocent.registerNewTeam("dd");
        tTeamIA = traitor.registerNewTeam("ia");
        tTeamIM = traitor.registerNewTeam("im");
        tTeamID = traitor.registerNewTeam("item.id.name");
        tTeamTA = traitor.registerNewTeam("ta");
        tTeamTM = traitor.registerNewTeam("tm");
        tTeamTD = traitor.registerNewTeam("td");
        tTeamDA = traitor.registerNewTeam("da");
        tTeamDM = traitor.registerNewTeam("dm");
        tTeamDD = traitor.registerNewTeam("dd");

        iTeamIA.setPrefix(
                fromNullableString(Config.SB_I_INNOCENT_PREFIX) + fromNullableString(Config.SB_ALIVE_PREFIX)
        );
        iTeamIM.setPrefix(
                fromNullableString(Config.SB_I_INNOCENT_PREFIX) + fromNullableString(Config.SB_MIA_PREFIX)
        );
        iTeamID.setPrefix(
                fromNullableString(Config.SB_I_INNOCENT_PREFIX) + fromNullableString(Config.SB_DEAD_PREFIX)
        );

        iTeamTA.setPrefix(
                fromNullableString(Config.SB_I_TRAITOR_PREFIX) + fromNullableString(Config.SB_ALIVE_PREFIX)
        );
        iTeamTM.setPrefix(
                fromNullableString(Config.SB_I_TRAITOR_PREFIX) + fromNullableString(Config.SB_MIA_PREFIX)
        );
        iTeamTD.setPrefix(
                fromNullableString(Config.SB_I_TRAITOR_PREFIX) + fromNullableString(Config.SB_DEAD_PREFIX)
        );

        iTeamDA.setPrefix(
                fromNullableString(Config.SB_I_DETECTIVE_PREFIX) + fromNullableString(Config.SB_ALIVE_PREFIX)
        );
        iTeamDM.setPrefix(
                fromNullableString(Config.SB_I_DETECTIVE_PREFIX) + fromNullableString(Config.SB_MIA_PREFIX)
        );
        iTeamDD.setPrefix(
                fromNullableString(Config.SB_I_DETECTIVE_PREFIX) + fromNullableString(Config.SB_DEAD_PREFIX)
        );

        tTeamIA.setPrefix(
                fromNullableString(Config.SB_T_INNOCENT_PREFIX) + fromNullableString(Config.SB_ALIVE_PREFIX)
        );
        tTeamIM.setPrefix(
                fromNullableString(Config.SB_T_INNOCENT_PREFIX) + fromNullableString(Config.SB_MIA_PREFIX)
        );
        tTeamID.setPrefix(
                fromNullableString(Config.SB_T_INNOCENT_PREFIX) + fromNullableString(Config.SB_DEAD_PREFIX)
        );

        tTeamTA.setPrefix(
                fromNullableString(Config.SB_T_TRAITOR_PREFIX) + fromNullableString(Config.SB_ALIVE_PREFIX)
        );
        tTeamTM.setPrefix(
                fromNullableString(Config.SB_T_TRAITOR_PREFIX) + fromNullableString(Config.SB_MIA_PREFIX)
        );
        tTeamTD.setPrefix(
                fromNullableString(Config.SB_T_TRAITOR_PREFIX) + fromNullableString(Config.SB_DEAD_PREFIX)
        );

        tTeamDA.setPrefix(
                fromNullableString(Config.SB_T_DETECTIVE_PREFIX) + fromNullableString(Config.SB_ALIVE_PREFIX)
        );
        tTeamDM.setPrefix(
                fromNullableString(Config.SB_T_DETECTIVE_PREFIX) + fromNullableString(Config.SB_MIA_PREFIX)
        );
        tTeamDD.setPrefix(
                fromNullableString(Config.SB_T_DETECTIVE_PREFIX) + fromNullableString(Config.SB_DEAD_PREFIX)
        );

        for (Challenger ch : round.getChallengers()) {
            Player pl = Bukkit.getPlayer(ch.getUniqueId());
            update(ch);

            if (ch.getTeam().isPresent()) {
                if (!MiscUtil.isTraitor(ch)) {
                    pl.setScoreboard(innocent);
                } else {
                    pl.setScoreboard(traitor);
                }
            } else {
                pl.setScoreboard(innocent);
            }
        }

    }

    public static void uninitialize() {
        for (ScoreManager sm : sbManagers.values()) {
            sm.iObj.unregister();
            sm.tObj.unregister();
        }
        sbManagers = null;
        manager = null;
    }

    @SuppressWarnings("deprecation")
    public void update(Challenger challenger) {

        if (ENTRY_SUPPORT) {
            innocent.resetScores(challenger.getName());
            traitor.resetScores(challenger.getName());
        } else {
            innocent.resetScores(challenger.getName());
            traitor.resetScores(challenger.getName());
        }

        if (innocent.getPlayerTeam(Bukkit.getOfflinePlayer(challenger.getName())) != null) {
            innocent.getPlayerTeam(Bukkit.getOfflinePlayer(challenger.getName()))
                    .removePlayer(Bukkit.getOfflinePlayer(challenger.getName()));
        }
        if (traitor.getPlayerTeam(Bukkit.getOfflinePlayer(challenger.getName())) != null) {
            traitor.getPlayerTeam(Bukkit.getOfflinePlayer(challenger.getName()))
                    .removePlayer(Bukkit.getOfflinePlayer(challenger.getName()));
        }

        if (challenger.getMetadata().has("detective")) {
            if (!challenger.isSpectating()) {
                iTeamDA.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamDA.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            } else if (!challenger.getMetadata().has("bodyFound")) {
                iTeamDM.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamDM.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            } else {
                iTeamDD.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamDD.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            }
        } else if (challenger.getTeam().isPresent() || challenger.getTeam().get().getId().equals("i")) {
            if (!challenger.isSpectating()) {
                iTeamIA.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamIA.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            } else if (!challenger.getMetadata().has("bodyFound")) {
                iTeamIM.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamIM.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            } else {
                iTeamID.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamID.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            }
        } else if (challenger.getTeam().isPresent() && challenger.getTeam().get().getId().equals("t")) {
            if (!challenger.isSpectating()) {
                iTeamTA.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamTA.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            } else if (!challenger.getMetadata().has("bodyFound")) {
                iTeamTM.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamTM.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            } else {
                iTeamTD.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
                tTeamTD.addPlayer(Bukkit.getOfflinePlayer(challenger.getName()));
            }
        }
        Score score1;
        Score score2;
        if (ENTRY_SUPPORT) {
            score1 = iObj.getScore(challenger.getName());
            score2 = tObj.getScore(challenger.getName());
        } else {
            score1 = iObj.getScore(Bukkit.getOfflinePlayer(challenger.getName()));
            score2 = tObj.getScore(Bukkit.getOfflinePlayer(challenger.getName()));
        }
        score1.setScore(challenger.getMetadata().<Integer>get("displayKarma").get());
        score2.setScore(challenger.getMetadata().<Integer>get("displayKarma").get());
    }

}
