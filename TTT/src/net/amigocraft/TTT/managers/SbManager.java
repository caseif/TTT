package net.amigocraft.TTT.managers;

import java.util.HashMap;

import net.amigocraft.TTT.Body;
import net.amigocraft.TTT.Role;
import net.amigocraft.TTT.TTT;
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

public class SbManager {

	public static HashMap<String, SbManager> sbManagers = new HashMap<String, SbManager>();

	public static ScoreboardManager manager = Bukkit.getScoreboardManager();
	public Scoreboard innocent;
	public Scoreboard traitor;
	public Objective iObj;
	public Objective tObj;
	public String r;
	public Team iTeamI;
	public Team iTeamT;
	public Team iTeamD;
	public Team tTeamI;
	public Team tTeamT;
	public Team tTeamD;

	public SbManager(String r){

		this.r = r;
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
		tTeamI= traitor.registerNewTeam("i");
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
			if (t.getWorld().equalsIgnoreCase(r)){
				if (t.getRole() == Role.INNOCENT && iTeamI.hasPlayer(Bukkit.getOfflinePlayer(t.getName()))){
					iTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				else if (t.getRole() == Role.TRAITOR && iTeamT.hasPlayer(Bukkit.getOfflinePlayer(t.getName()))){
					iTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				else if (t.getRole() == Role.DETECTIVE && iTeamD.hasPlayer(Bukkit.getOfflinePlayer(t.getName()))){
					iTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
					tTeamI.addPlayer(Bukkit.getOfflinePlayer(t.getName()));
				}
				if (t.isDead()){
					for (Body b : TTT.foundBodies){
						if (b.getPlayer().getName().equalsIgnoreCase(t.getName())){
							handleDeadPlayer(t);
							break;
						}
						handleMIAPlayer(t);
					}
				}
				else
					handleAlivePlayer(t);
			}
		}

		for (Player p : Bukkit.getOnlinePlayers()){
			if (TTTPlayer.isPlayer(p.getName())){
				TTTPlayer t = TTTPlayer.getTTTPlayer(p.getName());
				if (t.getWorld().equalsIgnoreCase(r)){
					// set scoreboards
					if (t.getRole() != null){
						if (t.getRole() == Role.INNOCENT || t.getRole() == Role.DETECTIVE)
							p.setScoreboard(innocent);
						else if (t.getRole() == Role.TRAITOR)
							p.setScoreboard(traitor);
					}
					else
						p.setScoreboard(innocent);
				}
			}
		}
	}

	private void handleAlivePlayer(TTTPlayer t){
		String s = ChatColor.BOLD + t.getName();
		Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(s));
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(s));
		score2.setScore(t.getDisplayKarma());
	}

	private void handleMIAPlayer(TTTPlayer t){
		Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(t.getName()));
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(t.getName()));
		score2.setScore(t.getDisplayKarma());
	}

	private void handleDeadPlayer(TTTPlayer t){
		String s = ChatColor.STRIKETHROUGH + t.getName();
		String ts = s;
		Role role = t.getRole();
		if (role != null)
			if (role == Role.TRAITOR)
				ts = ChatColor.DARK_RED + s;
		Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(s));
		score1.setScore(t.getDisplayKarma());
		Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(ts));
		score2.setScore(t.getDisplayKarma());
	}

}
