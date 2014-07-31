package net.amigocraft.ttt.managers;

import java.util.HashMap;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.TTTPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class ScoreManager {

	public static HashMap<String, ScoreManager> sbManagers = new HashMap<String, ScoreManager>();

	public static ScoreboardManager manager = Bukkit.getScoreboardManager();
	public Scoreboard innocent;
	public Scoreboard traitor;
	public Objective iObj;
	public Objective tObj;
	public String arenaName;
	public Team iTeamI;
	public Team iTeamT;
	public Team iTeamD;
	public Team tTeamI;
	public Team tTeamT;
	public Team tTeamD;

	public ScoreManager(String arenaName){

		this.arenaName = arenaName;
		innocent = manager.getNewScoreboard();
		traitor = manager.getNewScoreboard();

		iObj = innocent.registerNewObjective("p", "dummy");
		tObj = traitor.registerNewObjective("p", "dummy");
		iObj.setDisplayName("Players");
		tObj.setDisplayName("Players");
		iObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		tObj.setDisplaySlot(DisplaySlot.SIDEBAR);

		iTeamI = innocent.registerNewTeam("i");
		iTeamT = innocent.registerNewTeam("t");
		iTeamD = innocent.registerNewTeam("d");
		tTeamI = traitor.registerNewTeam("i");
		tTeamT = traitor.registerNewTeam("t");
		tTeamD = traitor.registerNewTeam("d");

		iTeamD.setPrefix(ChatColor.DARK_BLUE + "");
		tTeamT.setPrefix(ChatColor.DARK_RED + "");
		tTeamD.setPrefix(ChatColor.DARK_BLUE + "");

	}

	@SuppressWarnings("deprecation")
	public void manage(){

		for (String e : innocent.getEntries())
			innocent.resetScores(e);
		for (String e : traitor.getEntries())
			traitor.resetScores(e);

		for (MGPlayer m : Main.mg.getRound(arenaName).getPlayerList()){
			if (m.getBukkitPlayer() != null){
				TTTPlayer t = (TTTPlayer)m;
				if (t.hasMetadata("detective")){
					iTeamD.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamD.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				else if (t.getTeam() == null || t.getTeam().equals("Innocent")){
					iTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				else if (t.getTeam().equals("Traitor")){
					iTeamT.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamT.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				if (t.isSpectating()){
					if (t.isBodyFound())
						handleDeadPlayer(t);
					else
						handleMIAPlayer(t);
				}
				else
					handleAlivePlayer(t);

				if (t.getTeam() != null){
					if (!t.isTraitor())
						t.getBukkitPlayer().setScoreboard(innocent);
					else
						t.getBukkitPlayer().setScoreboard(traitor);
				}
				else
					t.getBukkitPlayer().setScoreboard(innocent);
			}
		}
	}

	private void handleAlivePlayer(TTTPlayer t){
		t.getBukkitPlayer().setDisplayName(ChatColor.BOLD +
				t.getBukkitPlayer().getName().substring(0, Math.min(t.getBukkitPlayer().getName().length(), 14)));
		String s = t.getBukkitPlayer().getDisplayName();
		Score score1 = iObj.getScore(s);
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(s);
		score2.setScore(t.getDisplayKarma());
	}

	private void handleMIAPlayer(TTTPlayer t){
		t.getBukkitPlayer().setDisplayName(t.getBukkitPlayer().getName());
		String s = t.getName();
		Score score1 = iObj.getScore(s);
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(s);
		score2.setScore(t.getDisplayKarma());
	}

	private void handleDeadPlayer(TTTPlayer t){
		t.getBukkitPlayer().setDisplayName((t.isTraitor() ? ChatColor.DARK_RED + "" : "") + ChatColor.STRIKETHROUGH + t.getBukkitPlayer().getName());
		if (t.getBukkitPlayer().getDisplayName().length() > 16)
			t.getBukkitPlayer().setDisplayName(t.getBukkitPlayer().getDisplayName().substring(0, 16));
		String s = t.getBukkitPlayer().getDisplayName().substring(0, Math.min(t.getBukkitPlayer().getDisplayName().length(), 16));
		Score score1 = iObj.getScore(s);
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(s);
		score2.setScore(t.getDisplayKarma());
	}

	public static void uninitialize(){
		sbManagers = null;
		manager = null;
	}

}
