package net.amigocraft.ttt;

import java.util.ArrayList;
import java.util.List;

public class Round {

	public static List<Round> rounds = new ArrayList<Round>();

	private int time = 0;
	private Stage stage;
	private String world;

	public Round(String world){
		this.world = world;
		stage = Stage.WAITING;
		rounds.add(this);
	}

	public String getWorld(){
		return world;
	}

	public Stage getStage(){
		return stage;
	}

	public int getTime(){
		return time;
	}

	public void setWorld(String world){
		this.world = world;
	}

	public void setStage(Stage s){
		stage = s;
	}

	public void setTime(int t){
		time = t;
	}

	public void tickDown(){
		time -= 1;
	}

	public void subtractTime(int t){
		time -= t;
	}

	public void addTime(int t){
		time += t;
	}

	public void destroy(){
		rounds.remove(this);
	}

	public List<TTTPlayer> getPlayers(){
		List<TTTPlayer> temp = new ArrayList<TTTPlayer>();
		List<TTTPlayer> p = new ArrayList<TTTPlayer>();
		for (TTTPlayer t : TTTPlayer.players)
			if (t.getWorld().equals(world))
				temp.add(t);
		for (TTTPlayer t : temp)
			if (!t.isDead())
				p.add(t);
		for (TTTPlayer t : temp)
			if (t.isDead() && !t.isBodyFound())
				p.add(t);
		for (TTTPlayer t : temp)
			if (t.isDead() && t.isBodyFound())
				p.add(t);
		return p;
	}

	public static Round getRound(String n){
		for (Round r : rounds){
			if (r.getWorld().equals(n))
				return r;
		}
		return null;
	}

	public boolean equals(Object p){
		Round r = (Round)p;
		return world.equals(r.getWorld());
	}

	public int hashCode(){
		return 41 * (world.hashCode() + 41);
	}
}
