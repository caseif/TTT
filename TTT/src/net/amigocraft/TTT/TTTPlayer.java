package net.amigocraft.TTT;

import java.util.ArrayList;

public class TTTPlayer {

	private String name;
	private String world;
	private Role role;
	private boolean dead;
	private boolean discreet = false;
	private String tracking;
	private String killer;
	private int karma;
	private int dispKarma;
	public static ArrayList<TTTPlayer> players = new ArrayList<TTTPlayer>();

	public TTTPlayer(String name, String world){
		this.name = name;
		this.world = world;
		karma = 1000;
		dispKarma = 1000;
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
	
	public boolean isDiscreet(){
		return discreet;
	}

	public String getTracking(){
		return tracking;
	}

	public String getKiller(){
		return killer;
	}
	
	public int getKarma(){
		return karma;
	}
	
	public int getDisplayedKarma(){
		return dispKarma;
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
	
	public void setDiscreet(boolean discreet){
		this.discreet = discreet;
	}

	public void setTracking(String tracking){
		this.tracking = tracking;
	}

	public void setKiller(String killer){
		this.killer = killer;
	}
	
	public void setKarma(int karma){
		this.karma = karma;
	}
	
	public void setDisplayedKarma(int karma){
		this.dispKarma = karma;
	}
	
	public void addKarma(int karma){
		this.karma += karma;
	}
	
	public void subtractKarma(int karma){
		this.karma -= karma;
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
	
	public boolean equals(Object p){
		TTTPlayer t = (TTTPlayer)p;
		boolean trackingEquals = false;
		if (tracking == null && t.getTracking() == null)
			trackingEquals = true;
		else if (tracking != null && t.getTracking() != null)
			if (tracking.equals(t.getTracking()))
				trackingEquals = true;
		boolean killerEquals = false;
		if (killer == null && t.getKiller() == null)
			killerEquals = true;
		else if (killer != null && t.getKiller() != null)
			if (killer.equals(t.getKiller()))
				killerEquals = true;
		boolean roleEquals = false;
		if (role == null && t.getRole() == null)
			roleEquals = true;
		else if (role != null && t.getRole() != null)
			if (role.equals(t.getRole()))
				roleEquals = true;
		return name.equals(t.getName()) && world.equals(t.getWorld()) &&
				roleEquals && dead == t.isDead() &&
				discreet == t.isDiscreet() && trackingEquals && killerEquals;
	}
	
	public int hashCode(){
		int trackingHash = 0;
		if (tracking != null)
			trackingHash = tracking.hashCode();
		int killerHash = 0;
		if (killer != null)
			killerHash = killer.hashCode();
		return 41 * (name.hashCode() + world.hashCode() + role.hashCode() +
				((Boolean)dead).hashCode() + ((Boolean)discreet).hashCode() +
				trackingHash + killerHash);
	}

}
