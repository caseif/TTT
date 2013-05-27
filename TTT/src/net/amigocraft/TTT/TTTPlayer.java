package net.amigocraft.TTT;

import java.util.ArrayList;

public class TTTPlayer {

	private String name;
	private String world;
	private Role role;
	private boolean dead;
	private String tracking;
	private String killer;
	public static ArrayList<TTTPlayer> players = new ArrayList<TTTPlayer>();

	public TTTPlayer(String name, String world){
		this.name = name;
		this.world = world;
		players.add(this);
	}
	
	public String getName(){
		return name;
	}

	public String getWorld(){
		return world;
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
		this.name = name;
	}

	public void setRole(Role role){
		this.role = role;
	}

	public void setDead(boolean dead){
		this.dead = dead;
	}

	public void setTracking(String tracking){
		this.tracking = tracking;
	}

	public void setKiller(String killer){
		this.killer = killer;
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
