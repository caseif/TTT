/*
 * TTT
 * Copyright (c) 2014, Maxim Roncacé <http://bitbucket.org/mproncace>
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
package net.amigocraft.ttt.managers;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.Config;
import net.amigocraft.ttt.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.*;

import java.util.HashMap;

import static net.amigocraft.ttt.Config.*;

public class ScoreManager {

	public static boolean ENTRY_SUPPORT = false;

	public static HashMap<String, ScoreManager> sbManagers = new HashMap<String, ScoreManager>();

	public static ScoreboardManager manager = Bukkit.getScoreboardManager();
	public Scoreboard innocent;
	public Scoreboard traitor;
	public Objective iObj;
	public Objective tObj;
	public String arenaName;
	public Team
			iTeamIA, iTeamIM, iTeamID, iTeamTA, iTeamTM, iTeamTD, iTeamDA, iTeamDM, iTeamDD,
			tTeamIA, tTeamIM, tTeamID, tTeamTA, tTeamTM, tTeamTD, tTeamDA, tTeamDM, tTeamDD;

	@SuppressWarnings("deprecation")
	public ScoreManager(String arenaName){

		try {
			Scoreboard.class.getMethod("getEntries");
			ENTRY_SUPPORT = true;
		}
		catch (NoSuchMethodException ex){}

		this.arenaName = arenaName;
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
		iTeamID = innocent.registerNewTeam("id");
		iTeamTA = innocent.registerNewTeam("ta");
		iTeamTM = innocent.registerNewTeam("tm");
		iTeamTD = innocent.registerNewTeam("td");
		iTeamDA = innocent.registerNewTeam("da");
		iTeamDM = innocent.registerNewTeam("dm");
		iTeamDD = innocent.registerNewTeam("dd");
		tTeamIA = traitor.registerNewTeam("ia");
		tTeamIM = traitor.registerNewTeam("im");
		tTeamID = traitor.registerNewTeam("id");
		tTeamTA = traitor.registerNewTeam("ta");
		tTeamTM = traitor.registerNewTeam("tm");
		tTeamTD = traitor.registerNewTeam("td");
		tTeamDA = traitor.registerNewTeam("da");
		tTeamDM = traitor.registerNewTeam("dm");
		tTeamDD = traitor.registerNewTeam("dd");

		iTeamIA.setPrefix(SB_I_INNOCENT_PREFIX + SB_ALIVE_PREFIX);
		iTeamIM.setPrefix(SB_I_INNOCENT_PREFIX + SB_MIA_PREFIX);
		iTeamID.setPrefix(SB_I_INNOCENT_PREFIX + SB_DEAD_PREFIX);

		iTeamTA.setPrefix(SB_I_TRAITOR_PREFIX + SB_ALIVE_PREFIX);
		iTeamTM.setPrefix(SB_I_TRAITOR_PREFIX + SB_MIA_PREFIX);
		iTeamTD.setPrefix(SB_I_TRAITOR_PREFIX + SB_DEAD_PREFIX);

		iTeamDA.setPrefix(SB_I_DETECTIVE_PREFIX + SB_ALIVE_PREFIX);
		iTeamDM.setPrefix(SB_I_DETECTIVE_PREFIX + SB_MIA_PREFIX);
		iTeamDD.setPrefix(SB_I_DETECTIVE_PREFIX + SB_DEAD_PREFIX);

		tTeamIA.setPrefix(SB_T_INNOCENT_PREFIX + SB_ALIVE_PREFIX);
		tTeamIM.setPrefix(SB_T_INNOCENT_PREFIX + SB_MIA_PREFIX);
		tTeamID.setPrefix(SB_T_INNOCENT_PREFIX + SB_DEAD_PREFIX);

		tTeamTA.setPrefix(SB_T_TRAITOR_PREFIX + SB_ALIVE_PREFIX);
		tTeamTM.setPrefix(SB_T_TRAITOR_PREFIX + SB_MIA_PREFIX);
		tTeamTD.setPrefix(SB_T_TRAITOR_PREFIX + SB_DEAD_PREFIX);

		tTeamDA.setPrefix(SB_T_DETECTIVE_PREFIX + SB_ALIVE_PREFIX);
		tTeamDM.setPrefix(SB_T_DETECTIVE_PREFIX + SB_MIA_PREFIX);
		tTeamDD.setPrefix(SB_T_DETECTIVE_PREFIX + SB_DEAD_PREFIX);

		for (MGPlayer m : Main.mg.getRound(arenaName).getPlayerList()){
			if (m.getBukkitPlayer() != null){
				update(m);

				/*if (t.isSpectating()){
					if (t.isBodyFound())
						handleDeadPlayer(t);
				}
				else
					handleAlivePlayer(t);*/

				if (m.getTeam() != null){
					if (!MiscUtil.isTraitor(m)){
						m.getBukkitPlayer().setScoreboard(innocent);
					}
					else {
						m.getBukkitPlayer().setScoreboard(traitor);
					}
				}
				else {
					m.getBukkitPlayer().setScoreboard(innocent);
				}

			}
		}

	}

	@SuppressWarnings("deprecated")
	public void update(MGPlayer player){

		if (ENTRY_SUPPORT){
			innocent.resetScores(player.getName());
			traitor.resetScores(player.getName());
		}
		else {
			innocent.resetScores(player.getName());
			traitor.resetScores(player.getName());
		}

		if (innocent.getPlayerTeam(Bukkit.getOfflinePlayer(player.getName())) != null){
			innocent.getPlayerTeam(Bukkit.getOfflinePlayer(player.getName()))
					.removePlayer(Bukkit.getOfflinePlayer(player.getName()));
		}
		if (traitor.getPlayerTeam(Bukkit.getOfflinePlayer(player.getName())) != null){
			traitor.getPlayerTeam(Bukkit.getOfflinePlayer(player.getName()))
					.removePlayer(Bukkit.getOfflinePlayer(player.getName()));
		}

		if (player.hasMetadata("detective")){
			if (!player.isSpectating()){
				iTeamDA.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamDA.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
			else if (!player.hasMetadata("bodyFound")){
				iTeamDM.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamDM.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
			else {
				iTeamDD.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamDD.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
		}
		else if (player.getTeam() == null || player.getTeam().equals("Innocent")){
			if (!player.isSpectating()){
				iTeamIA.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamIA.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
			else if (!player.hasMetadata("bodyFound")){
				iTeamIM.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamIM.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
			else {
				iTeamID.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamID.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
		}
		else if (player.getTeam().equals("Traitor")){
			if (!player.isSpectating()){
				iTeamTA.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamTA.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
			else if (!player.hasMetadata("bodyFound")){
				iTeamTM.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamTM.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
			else {
				iTeamTD.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
				tTeamTD.addPlayer(Bukkit.getOfflinePlayer(player.getName()));
			}
		}
		Score score1;
		Score score2;
		if (ENTRY_SUPPORT){
			score1 = iObj.getScore(player.getName());
			score2 = tObj.getScore(player.getName());
		}
		else {
			score1 = iObj.getScore(Bukkit.getOfflinePlayer(player.getName()));
			score2 = tObj.getScore(Bukkit.getOfflinePlayer(player.getName()));
		}
		score1.setScore((Integer)player.getMetadata("displayKarma"));
		score2.setScore((Integer)player.getMetadata("displayKarma"));
	}

	public static void uninitialize(){
		for (ScoreManager sm : sbManagers.values()){
			sm.iObj.unregister();
			sm.tObj.unregister();
		}
		sbManagers = null;
		manager = null;
	}

}
