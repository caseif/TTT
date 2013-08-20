package net.amigocraft.TTT;

import java.util.ArrayList;

import net.amigocraft.TTT.managers.KarmaManager;
import net.amigocraft.TTT.managers.LobbyManager;

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
	private boolean teamKill = false;
	private double damageRed = 1;
	private boolean found = false;
	public static ArrayList<TTTPlayer> players = new ArrayList<TTTPlayer>();

	public TTTPlayer(String name, String world){
		this.name = name;
		this.world = world;
		KarmaManager.loadKarma(name);
		karma = KarmaManager.playerKarma.get(name);
		dispKarma = KarmaManager.playerKarma.get(name);
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

	public int getDisplayKarma(){
		return dispKarma;
	}
	
	public double getDamageReduction(){
		return damageRed;
	}

	public boolean hasTeamKilled(){
		return teamKill;
	}
	
	public boolean isBodyFound(){
		return found;
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
		TTT.kLog.info(Integer.toString(karma));
	}

	public void setDisplayKarma(int karma){
		this.dispKarma = karma;
	}
	
	public void setBodyFound(boolean found){
		this.found = found;
	}
	
	public void calculateDamageReduction(){
		double a = -8.7369523603199 * Math.pow(10, -8);
		double b = 0.001285215082891;
		double c = -0.12425741322646;
		int x = dispKarma;
		this.damageRed = a * Math.pow(x, 2) + b * x + c;
		if (damageRed > 1)
			damageRed = 1;
	}

	public void addKarma(int karma){
		if (karma == 0 && TTT.plugin.getConfig().getBoolean("karma-round-to-one"))
			karma = 1;
		if (this.karma + karma < TTT.maxKarma)
			this.karma += karma;
		else if (this.karma < TTT.maxKarma)
			this.karma = TTT.maxKarma;
		if (TTT.plugin.getConfig().getBoolean("karma-debug"))
			TTT.kLog.info(getName() + ": +" + karma + ". " + "New value: " + getKarma());
	}

	public void subtractKarma(int karma){
		if (karma == 0 && TTT.plugin.getConfig().getBoolean("karma-round-to-one"))
			karma = 1;
		if (this.karma - karma < TTT.plugin.getConfig().getInt("karma-kick"))
			KarmaManager.handleKick(this);
		else {
			this.karma -= karma;
			teamKill = true;
		}
		if (TTT.plugin.getConfig().getBoolean("karma-debug"))
			TTT.kLog.info(getName() + ": -" + karma + ". " + "New value: " + getKarma());
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
		LobbyManager.updateSigns(world);
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

	public boolean isTraitor(){
		return role == Role.TRAITOR;
	}

}
