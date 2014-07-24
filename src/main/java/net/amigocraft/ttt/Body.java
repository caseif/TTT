package net.amigocraft.ttt;

import net.amigocraft.mglib.api.Location3D;
// the class

public class Body {
	private TTTPlayer player;
	private Location3D l;
	private long time;

	public Body(TTTPlayer player, Location3D l, long time){
		this.player = player;
		this.l = l;
		this.time = time;
	}
	
	public TTTPlayer getPlayer(){
		return player;
	}

	public Location3D getLocation(){
		return l;
	}
	
	public long getTime(){
		return time;
	}
	
	public boolean equals(Object b){
		return player.equals(((Body)b).getPlayer()) && l.equals(((Body)b).getLocation());
	}
	
	public int hashCode(){
		return 41 * (41 + player.hashCode() + l.hashCode());
	}
}