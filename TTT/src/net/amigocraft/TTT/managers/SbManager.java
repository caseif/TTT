package net.amigocraft.TTT.managers;

import java.util.ArrayList;
import java.util.List;

import net.amigocraft.TTT.Body;
import net.amigocraft.TTT.Role;
import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class SbManager {
	
	public static ScoreboardManager manager = Bukkit.getScoreboardManager();
	public static Scoreboard innocent = manager.getNewScoreboard();
	public static Scoreboard traitor = manager.getNewScoreboard();
	
	public static void manage(String r){
		
		Objective iObj = innocent.getObjective("p");
		Objective tObj = innocent.getObjective("p");
		iObj.setDisplayName("Players");
		tObj.setDisplayName("Players");
		
		List<String> alivePlayers = new ArrayList<String>();
		List<String> miaPlayers = new ArrayList<String>();
		List<String> deadPlayers = new ArrayList<String>();
		for (Player p : Bukkit.getOnlinePlayers()){
			if (TTTPlayer.isPlayer(p.getName())){
				TTTPlayer t = TTTPlayer.getTTTPlayer(p.getName());
				if (t.getWorld().equalsIgnoreCase(r)){
					if (t.isDead()){
						for (Body b : TTT.foundBodies)
							if (b.getPlayer().getName().equalsIgnoreCase(p.getName()))
								deadPlayers.add(p.getName());
							else
								miaPlayers.add(p.getName());
					}
					else
						alivePlayers.add(p.getName());
				}
			}
		}
		
		for (String s : deadPlayers){
			Role role = TTTPlayer.getTTTPlayer(s).getRole();
			if (role != null)
				if (role == Role.TRAITOR)
					s = ChatColor.RED + s;
				else if (role == Role.DETECTIVE)
					s = ChatColor.DARK_BLUE + s;
			Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(s));
			score1.setScore(0);
			Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(s));
			score2.setScore(0);
			//TODO: Set score to karma
		}
		
		Score deadLabel1 = iObj.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_PURPLE + "Confirmed Dead"));
		deadLabel1.setScore(deadPlayers.size());
		Score deadLabel2 = tObj.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_PURPLE + "Confirmed Dead"));
		deadLabel2.setScore(deadPlayers.size());
		
		for (String s : miaPlayers){
			Role role = TTTPlayer.getTTTPlayer(s).getRole();
			String ts = s;
			if (role != null)
				if (role == Role.TRAITOR)
					ts = ChatColor.RED + s;
				else if (role == Role.DETECTIVE)
					s = ChatColor.DARK_BLUE + s;
			Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(s));
			score1.setScore(0);
			Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(ts));
			score2.setScore(0);
			//TODO: Set score to karma
		}
		
		Score miaLabel1 = iObj.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_PURPLE + "Missing in Action"));
		miaLabel1.setScore(miaPlayers.size());
		Score miaLabel2 = tObj.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_PURPLE + "Missing in Action"));
		miaLabel2.setScore(miaPlayers.size());
		
		for (String s : alivePlayers){
			Role role = TTTPlayer.getTTTPlayer(s).getRole();
			String ts = s;
			if (role != null)
				if (role == Role.TRAITOR)
					ts = ChatColor.RED + s;
				else if (role == Role.DETECTIVE)
					s = ChatColor.DARK_BLUE + s;
			Score score1 = iObj.getScore(Bukkit.getOfflinePlayer(s));
			score1.setScore(0);
			Score score2 = tObj.getScore(Bukkit.getOfflinePlayer(ts));
			score2.setScore(0);
			//TODO: Set score to karma
		}
		
		Score aliveLabel1 = iObj.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_PURPLE + "Terrorists"));
		aliveLabel1.setScore(alivePlayers.size());
		Score aliveLabel2 = tObj.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_PURPLE + "Terrorists"));
		aliveLabel2.setScore(alivePlayers.size());

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
	
}
