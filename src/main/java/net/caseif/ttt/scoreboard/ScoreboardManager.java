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

import static net.caseif.ttt.util.helper.misc.MiscHelper.fromNullableString;

import net.caseif.ttt.util.Constants.AliveStatus;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.misc.MiscHelper;

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

//TODO: this is kind of a clusterf--- of a system and really needs to be rewritten from scratch at some point
public class ScoreboardManager {

    private static HashMap<String, ScoreboardManager> sbManagers = new HashMap<>();

    private static org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();

    private Scoreboard innocent;
    private Scoreboard traitor;
    private Objective iObj;
    private Objective tObj;
    private Round round;

    private BiMap<TeamKey, Team> teams = HashBiMap.create();

    private static final ImmutableMap<String, String> ALIVE_PREFIXES = ImmutableMap.<String, String>builder()
            .put(AliveStatus.ALIVE, "")
            .put(AliveStatus.MIA, "§7")
            .put(AliveStatus.CONFIRMED_DEAD, "§m")
            .build();

    @SuppressWarnings("deprecation")
    public ScoreboardManager(Round round) {
        this.round = round;
        innocent = manager.getNewScoreboard();
        traitor = manager.getNewScoreboard();

        iObj = innocent.registerNewObjective("p", "dummy");
        iObj.setDisplayName("Players");
        iObj.setDisplaySlot(DisplaySlot.SIDEBAR);

        tObj = traitor.registerNewObjective("p", "dummy");
        tObj.setDisplayName("Players");
        tObj.setDisplaySlot(DisplaySlot.SIDEBAR);

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
            pl.setScoreboard(MiscHelper.isTraitor(ch) ? traitor : innocent);
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
            if (!challenger.getMetadata().has(MetadataTag.PURE_SPECTATOR)) {
                innocent.resetScores(challenger.getName());
                traitor.resetScores(challenger.getName());

                if (innocent.getEntryTeam(challenger.getName()) != null) {
                    innocent.getEntryTeam(challenger.getName()).removeEntry(challenger.getName());
                }
                if (traitor.getEntryTeam(challenger.getName()) != null) {
                    traitor.getEntryTeam(challenger.getName()).removeEntry(challenger.getName());
                }

                for (Team team : getValidTeams(challenger)) {
                    team.addEntry(challenger.getName());
                }

                Score score1;
                Score score2;
                score1 = iObj.getScore(challenger.getName());
                score2 = tObj.getScore(challenger.getName());

                if (!challenger.getMetadata().has(MetadataTag.DISPLAY_KARMA)) {
                    KarmaHelper.applyKarma(challenger);
                }
                int displayKarma = challenger.getMetadata().<Integer>get(MetadataTag.DISPLAY_KARMA).get();
                score1.setScore(displayKarma);
                score2.setScore(displayKarma);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private ImmutableSet<Team> getValidTeams(Challenger ch) {
        String role = MiscHelper.isTraitor(ch)
                ? Role.TRAITOR
                : (ch.getMetadata().has(Role.DETECTIVE) ? Role.DETECTIVE : Role.INNOCENT);
        String aliveStatus = ch.isSpectating()
                ?
                (ch.getMetadata().has(MetadataTag.BODY_FOUND)
                        ? AliveStatus.CONFIRMED_DEAD
                        : AliveStatus.MIA)
                : AliveStatus.ALIVE;

        Set<Team> teams = new HashSet<>();
        for (Map.Entry<TeamKey, Team> e : this.teams.entrySet()) {
            if (e.getKey().getRole().equals(role) && e.getKey().getAliveStatus().equals(aliveStatus)) {
                teams.add(e.getValue());
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
        @SuppressWarnings("deprecation")
        Set<Score> scores = obj.getScoreboard().getScores(ch.getName());
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
        Score score = obj.getScore(ch.getName());

        if (score.getScore() != ch.getMetadata().<Integer>get(MetadataTag.DISPLAY_KARMA).or(0)) {
            return true;
        }

        for (Team team : getValidTeams(ch)) {
            if (!team.getEntries().contains(ch.getName())) {
                return true;
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
        Bukkit.getPlayer(ch.getUniqueId()).setScoreboard(MiscHelper.isTraitor(ch) ? traitor : innocent);
    }

}
