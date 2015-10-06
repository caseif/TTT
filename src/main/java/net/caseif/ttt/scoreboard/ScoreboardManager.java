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
package net.caseif.ttt.scoreboard;

import static net.caseif.ttt.util.MiscUtil.fromNullableString;

import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.AliveStatus;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.MiscUtil;
import net.caseif.ttt.util.helper.ConfigHelper;
import net.caseif.ttt.util.helper.KarmaHelper;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScoreboardManager {

    //TODO: drop support for Bukkit 1.7.2 in the next major version (0.9)
    public static final boolean ENTRY_SUPPORT;

    //TODO: make this private and abstract mutation of it
    private static HashMap<String, ScoreboardManager> sbManagers = new HashMap<>();

    private static org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();

    static {
        boolean support = false;
        try {
            Scoreboard.class.getMethod("getEntries");
            support = true;
        } catch (NoSuchMethodException ignored) {
        }
        ENTRY_SUPPORT = support;
    }

    private Scoreboard innocent;
    private Scoreboard traitor;
    private Objective iObj;
    private Objective tObj;
    private Round round;

    private BiMap<TeamKey, Team> teams = HashBiMap.create();

    private static final ImmutableMap<String, String> ALIVE_PREFIXES = ImmutableMap.<String, String>builder()
            .put(AliveStatus.ALIVE, ConfigHelper.SB_ALIVE_PREFIX)
            .put(AliveStatus.MIA, ConfigHelper.SB_MIA_PREFIX)
            .put(AliveStatus.CONFIRMED_DEAD, ConfigHelper.SB_DEAD_PREFIX)
            .build();

    @SuppressWarnings("deprecation")
    public ScoreboardManager(Round round) {
        this.round = round;
        innocent = manager.getNewScoreboard();
        traitor = manager.getNewScoreboard();

        iObj = innocent.registerNewObjective("p", "dummy");
        iObj.setDisplayName("Players");
        iObj.setDisplaySlot(ConfigHelper.SB_USE_PLAYER_LIST ? DisplaySlot.PLAYER_LIST : DisplaySlot.SIDEBAR);

        tObj = traitor.registerNewObjective("p", "dummy");
        tObj.setDisplayName("Players");
        tObj.setDisplaySlot(ConfigHelper.SB_USE_PLAYER_LIST ? DisplaySlot.PLAYER_LIST : DisplaySlot.SIDEBAR);

        String[] roles = {Role.INNOCENT, Role.TRAITOR, Role.DETECTIVE};
        String[] aliveStatuses = {AliveStatus.ALIVE, AliveStatus.MIA, AliveStatus.CONFIRMED_DEAD};

        for (int i = 0; i <= 1; i++) {
            boolean traitorBoard = i == 1;
            Scoreboard sb = traitorBoard ? traitor : innocent;
            for (String role : roles) {
                for (String alive : aliveStatuses) {
                    Team team = sb.registerNewTeam(role.charAt(0) + "" + alive.charAt(0));
                    String rolePrefix = role.equals(Role.DETECTIVE)
                            ? Color.DETECTIVE
                            : (role.equals(Role.TRAITOR) && traitorBoard ? Color.TRAITOR : "");
                    String alivePrefix = fromNullableString(ALIVE_PREFIXES.get(alive));
                    team.setPrefix(rolePrefix + alivePrefix);
                    teams.put(new TeamKey(traitorBoard, role, alive), team);
                }
            }
        }

        for (Challenger ch : round.getChallengers()) {
            Player pl = Bukkit.getPlayer(ch.getUniqueId());
            update(ch);
            pl.setScoreboard(MiscUtil.isTraitor(ch) ? traitor : innocent);
        }

        sbManagers.put(round.getArena().getId(), this);
    }

    public static void uninitialize() {
        for (ScoreboardManager sm : sbManagers.values()) {
            sm.iObj.unregister();
            sm.tObj.unregister();
        }
        sbManagers = null;
        manager = null;
    }

    public static Optional<ScoreboardManager> get(Round round) {
        return Optional.fromNullable(sbManagers.get(round.getArena().getId()));
    }

    public static ScoreboardManager getOrCreate(Round round) {
        Optional<ScoreboardManager> sm = get(round);
        if (sm.isPresent()) {
            return sm.get();
        } else {
            return new ScoreboardManager(round);
        }
    }

    public void unregister() {
        iObj.unregister();
        tObj.unregister();
        sbManagers.remove(round.getArena().getId());
    }

    @SuppressWarnings("deprecation")
    public void update(Challenger challenger) {
        if (needsUpdate(challenger)) {
            if (!challenger.getMetadata().has(Constants.PlayerTag.PURE_SPECTATOR)) {
                if (ENTRY_SUPPORT) {
                    innocent.resetScores(challenger.getName());
                    traitor.resetScores(challenger.getName());
                } else {
                    innocent.resetScores(Bukkit.getPlayer(challenger.getUniqueId()));
                    traitor.resetScores(Bukkit.getPlayer(challenger.getUniqueId()));
                }

                if (ENTRY_SUPPORT) {
                    if (innocent.getEntryTeam(challenger.getName()) != null) {
                        innocent.getEntryTeam(challenger.getName()).removeEntry(challenger.getName());
                    }
                    if (traitor.getEntryTeam(challenger.getName()) != null) {
                        traitor.getEntryTeam(challenger.getName()).removeEntry(challenger.getName());
                    }
                } else {
                    if (innocent.getPlayerTeam(Bukkit.getPlayer(challenger.getUniqueId())) != null) {
                        innocent.getPlayerTeam(Bukkit.getPlayer(challenger.getUniqueId()))
                                .removePlayer(Bukkit.getPlayer(challenger.getUniqueId()));
                    }
                    if (traitor.getPlayerTeam(Bukkit.getPlayer(challenger.getUniqueId())) != null) {
                        traitor.getPlayerTeam(Bukkit.getPlayer(challenger.getUniqueId()))
                                .removePlayer(Bukkit.getPlayer(challenger.getUniqueId()));
                    }
                }

                for (Team team : getValidTeams(challenger)) {
                    if (ENTRY_SUPPORT) {
                        team.addEntry(challenger.getName());
                    } else {
                        team.addPlayer(Bukkit.getPlayer(challenger.getUniqueId()));
                    }
                }

                Score score1;
                Score score2;
                if (ENTRY_SUPPORT) {
                    score1 = iObj.getScore(challenger.getName());
                    score2 = tObj.getScore(challenger.getName());
                } else {
                    score1 = iObj.getScore(Bukkit.getPlayer(challenger.getUniqueId()));
                    score2 = tObj.getScore(Bukkit.getPlayer(challenger.getUniqueId()));
                }

                if (!challenger.getMetadata().has("displayKarma")) {
                    KarmaHelper.applyKarma(challenger);
                }
                int displayKarma = challenger.getMetadata().<Integer>get("displayKarma").get();
                score1.setScore(displayKarma);
                score2.setScore(displayKarma);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private ImmutableSet<Team> getValidTeams(Challenger ch) {
        String role = MiscUtil.isTraitor(ch)
                ? Role.TRAITOR
                : (ch.getMetadata().has(Role.DETECTIVE) ? Role.DETECTIVE : Role.INNOCENT);
        String aliveStatus = ch.isSpectating()
                ? (ch.getMetadata().has("bodyFound") ? AliveStatus.CONFIRMED_DEAD : AliveStatus.MIA)
                : AliveStatus.ALIVE;

        Set<Team> teams = new HashSet<>();
        for (Map.Entry<TeamKey, Team> e : this.teams.entrySet()) {
            if (e.getKey().getRole().equals(role) && e.getKey().getAliveStatus().equals(aliveStatus)) {
                if (ENTRY_SUPPORT) {
                    e.getValue().addEntry(ch.getName());
                } else {
                    e.getValue().addPlayer(Bukkit.getPlayer(ch.getUniqueId()));
                }
                if (teams.size() == 2) {
                    break;
                }
            }
        }
        return ImmutableSet.copyOf(teams);
    }

    private boolean needsUpdate(Challenger ch) {
        return needsUpdate(ch, iObj) || needsUpdate(ch, tObj);
    }

    private boolean needsUpdate(Challenger ch, Objective obj) {
        Player pl = Bukkit.getPlayer(ch.getUniqueId());

        @SuppressWarnings("deprecation")
        Set<Score> scores = ENTRY_SUPPORT
                ? obj.getScoreboard().getScores(ch.getName())
                : obj.getScoreboard().getScores(pl);
        boolean found = false;
        for (Score score : scores) {
            if (score.getObjective() == obj) {
                found = true;
                break;
            }
        }
        if (!found) {
            return true;
        }

        @SuppressWarnings("deprecation")
        Score score = ENTRY_SUPPORT ? obj.getScore(ch.getName()) : obj.getScore(pl);

        if (score.getScore() != ch.getMetadata().<Integer>get("displayKarma").or(0)) {
            return true;
        }

        if (ENTRY_SUPPORT) {
            for (Team team : getValidTeams(ch)) {
                if (!team.getEntries().contains(ch.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public void assignScoreboards() {
        for (Challenger ch : round.getChallengers()) {
            assignScoreboard(ch);
        }
    }

    public void assignScoreboard(Challenger ch) {
        Bukkit.getPlayer(ch.getUniqueId()).setScoreboard(MiscUtil.isTraitor(ch) ? traitor : innocent);
    }

}
