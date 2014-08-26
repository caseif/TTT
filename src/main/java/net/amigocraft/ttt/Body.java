package net.amigocraft.ttt;

import net.amigocraft.mglib.api.Location3D;
// the class

public class Body {
	private String player;
	private String arena;
	private String team;
	private Location3D l;
	private long time;

	public Body(String player, String arena, String team, Location3D l, long time){
		this.player = player;
		this.arena = arena;
		this.team = team;
		this.l = l;
		this.time = time;
	}

	public String getPlayer(){
		return player;
	}

	public String getArena(){
		return arena;
	}

	public String getTeam(){
		return team;
	}

	public Location3D getLocation(){
		return l;
	}

	public long getTime(){
		return time;
	}

	public boolean equals(Object b){
		return player.equals(((Body) b).getPlayer()) && arena.equals(((Body) b).getArena()) &&
				team.equals(((Body) b).getTeam()) && l.equals(((Body) b).getLocation());
	}

	public int hashCode(){
		return 41 * (41 + player.hashCode() + l.hashCode());
	}
}