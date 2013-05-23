package net.amigocraft.TTT;

public class TTTPlayer {
	
	private String game;
	private Role role;
	private boolean dead;
	private String tracking;
	private String killer;
	
	public TTTPlayer(String game, Role role){
		this.game = game;
		this.role = role;
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
	
	public void setGame(String game){
		this.game = game;
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
	
}
