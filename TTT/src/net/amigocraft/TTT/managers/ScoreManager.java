package net.amigocraft.TTT.managers;

import java.util.HashMap;

import net.amigocraft.TTT.Role;
import net.amigocraft.TTT.TTTPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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
	public String worldName;
	public Team iTeamI;
	public Team iTeamT;
	public Team iTeamD;
	public Team tTeamI;
	public Team tTeamT;
	public Team tTeamD;

	public ScoreManager(String worldName){

		this.worldName = worldName;
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

	public void manage(){

		for (OfflinePlayer o : innocent.getPlayers())
			innocent.resetScores(o);
		for (OfflinePlayer o : traitor.getPlayers())
			traitor.resetScores(o);

		for (TTTPlayer t : TTTPlayer.players){
			if (t.getWorld().equalsIgnoreCase(worldName)){
				if (t.getRole() == Role.INNOCENT){
					iTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				else if (t.getRole() == Role.TRAITOR){
					iTeamT.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamT.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				else if (t.getRole() == Role.DETECTIVE){
					iTeamD.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamD.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				if (t.isDead()){
					if (t.isBodyFound())
						handleDeadPlayer(t);
					else
						handleMIAPlayer(t);
				}
				else
					handleAlivePlayer(t);
			}
		}

		for (Player p : Bukkit.getOnlinePlayers()){
			if (TTTPlayer.isPlayer(p.getName())){
				TTTPlayer t = TTTPlayer.getTTTPlayer(p.getName());
				if (t.getWorld().equalsIgnoreCase(worldName)){
					// set scoreboards
					if (t.getRole() != null){
						if (!t.isTraitor())
							p.setScoreboard(innocent);
						else
							p.setScoreboard(traitor);
					}
					else
						p.setScoreboard(innocent);
				}
			}
		}
	}

	private void handleAlivePlayer(TTTPlayer t){
		String s = "§l" + t.getName();
		if (t.getRole() != null){
			int prefix = t.getRole() != Role.INNOCENT ? 2 : 0;
			if (prefix + s.length() > 16)
				s = s.substring(0, 16 - prefix);
		}
		Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(s));
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(s));
		score2.setScore(t.getDisplayKarma());
	}

	private void handleMIAPlayer(TTTPlayer t){
		String s = t.getName();
		if (t.getRole() != null){
			int prefix = t.getRole() != Role.INNOCENT ? 2 : 0;
			if (prefix + s.length() > 16)
				s = s.substring(0, 16 - prefix);
		}
		Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(s));
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(s));
		score2.setScore(t.getDisplayKarma());
	}

	private void handleDeadPlayer(TTTPlayer t){
		String s = "§m" + t.getName();
		if (t.isTraitor())
			s = "§4§m" + t.getName();
		if (t.getRole() != null){
			int prefix = t.getRole() != Role.INNOCENT ? 2 : 0;
			if (prefix + s.length() > 16)
				s = s.substring(0, 16 - prefix);
		}
		Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(s));
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(s));
		score2.setScore(t.getDisplayKarma());
	}

}
