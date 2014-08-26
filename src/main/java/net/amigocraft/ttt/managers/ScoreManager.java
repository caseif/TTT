package net.amigocraft.ttt.managers;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.TTTPlayer;
import net.amigocraft.ttt.Variables;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.*;

import java.util.HashMap;

import static net.amigocraft.ttt.Variables.*;

public class ScoreManager {

	public static boolean ENTRY_SUPPORT = false;

	public static HashMap<String, ScoreManager> sbManagers = new HashMap<String, ScoreManager>();

	public static ScoreboardManager manager = Bukkit.getScoreboardManager();
	public Scoreboard innocent;
	public Scoreboard traitor;
	public Objective iObj;
	public Objective tObj;
	public String arenaName;
	public Team iTeamIA, iTeamIM, iTeamID, iTeamTA, iTeamTM, iTeamTD, iTeamDA, iTeamDM, iTeamDD, tTeamIA, tTeamIM, tTeamID, tTeamTA, tTeamTM, tTeamTD, tTeamDA, tTeamDM, tTeamDD;

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
		iObj.setDisplaySlot(Variables.SB_USE_SIDEBAR ? DisplaySlot.SIDEBAR : DisplaySlot.PLAYER_LIST);
		tObj.setDisplaySlot(Variables.SB_USE_SIDEBAR ? DisplaySlot.SIDEBAR : DisplaySlot.PLAYER_LIST);

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
				TTTPlayer t = (TTTPlayer) m;
				update(t);

				/*if (t.isSpectating()){
					if (t.isBodyFound())
						handleDeadPlayer(t);
				}
				else
					handleAlivePlayer(t);*/

				if (t.getTeam() != null){
					if (!t.isTraitor()){
						t.getBukkitPlayer().setScoreboard(innocent);
					}
					else {
						t.getBukkitPlayer().setScoreboard(traitor);
					}
				}
				else {
					t.getBukkitPlayer().setScoreboard(innocent);
				}

			}
		}

	}

	public void update(TTTPlayer t){

		if (ENTRY_SUPPORT){
			innocent.resetScores(t.getName());
			traitor.resetScores(t.getName());
		}
		else {
			innocent.resetScores(t.getName());
			traitor.resetScores(t.getName());
		}

		if (innocent.getPlayerTeam(Bukkit.getOfflinePlayer(t.getName())) != null){
			innocent.getPlayerTeam(Bukkit.getOfflinePlayer(t.getName())).removePlayer(Bukkit.getOfflinePlayer(t.getName()));
		}
		if (traitor.getPlayerTeam(Bukkit.getOfflinePlayer(t.getName())) != null){
			traitor.getPlayerTeam(Bukkit.getOfflinePlayer(t.getName())).removePlayer(Bukkit.getOfflinePlayer(t.getName()));
		}

		if (t.hasMetadata("detective")){
			if (!t.isSpectating()){
				iTeamDA.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamDA.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
			else if (!t.isBodyFound()){
				iTeamDM.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamDM.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
			else {
				iTeamDD.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamDD.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
		}
		else if (t.getTeam() == null || t.getTeam().equals("Innocent")){
			if (!t.isSpectating()){
				iTeamIA.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamIA.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
			else if (!t.isBodyFound()){
				iTeamIM.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamIM.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
			else {
				iTeamID.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamID.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
		}
		else if (t.getTeam().equals("Traitor")){
			if (!t.isSpectating()){
				iTeamTA.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamTA.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
			else if (!t.isBodyFound()){
				iTeamTM.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamTM.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
			else {
				iTeamTD.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				tTeamTD.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
			}
		}
		Score score1;
		Score score2;
		if (ENTRY_SUPPORT){
			score1 = iObj.getScore(t.getName());
			score2 = tObj.getScore(t.getName());
		}
		else {
			score1 = iObj.getScore(Bukkit.getOfflinePlayer(t.getName()));
			score2 = tObj.getScore(Bukkit.getOfflinePlayer(t.getName()));
		}
		score1.setScore(t.getDisplayKarma());
		score2.setScore(t.getDisplayKarma());
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
