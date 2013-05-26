package net.amigocraft.TTT;

import java.util.ArrayList;

public class TTTPlayer {

	private String name;
	private String game;
	private Role role;
	private boolean dead;
	private String tracking;
	private String killer;
	public static ArrayList<TTTPlayer> players = new ArrayList<TTTPlayer>();

	public TTTPlayer(String name, String game){
		this.name = name;
		this.game = game;
		players.add(this);
	}
	
	public String getName(){
		return name;
	}

	public String getGame(){
		return game;
	}

	public Role getRole(){
		return role;
	}

	public boolean isDead(){
		return dead;
	}

	public String getTracking(){
		return tracking;
	}

	public String getKiller(){
		return killer;
	}

	public void setName(String name){
		players.remove(this);
		this.name = name;
		players.add(this);
	}
	
	public void setGame(String game){
		players.remove(this);
		this.game = game;
		players.add(this);
	}

	public void setRole(Role role){
		players.remove(this);
		this.role = role;
		players.add(this);
	}

	public void setDead(boolean dead){
		players.remove(this);
		this.dead = dead;
		players.add(this);
	}

	public void setTracking(String tracking){
		players.remove(this);
		this.tracking = tracking;
		players.add(this);
	}

	public void setKiller(String killer){
		players.remove(this);
		this.killer = killer;
		players.add(this);
	}

	public static TTTPlayer getTTTPlayer(String player){
		for (TTTPlayer p : players){
			if (p.getName().equals(player))
				return p;
		}
		return null;
	}
	
	public void destroy(){
		players.remove(this);
	}
	
	public static void destroy(String p){
		TTTPlayer remove = null;
		for (TTTPlayer t : players)
			if (t.getName().equals(p))
				remove = t;
		if (remove != null)
			players.remove(remove);
	}
	
	public static boolean isPlayer(String p){
		if (getTTTPlayer(p) != null)
			return true;
		return false;
	}

}
