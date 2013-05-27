package net.amigocraft.TTT;

import java.util.ArrayList;
import java.util.List;

public class Round {

	public static List<Round> rounds = new ArrayList<Round>();
	
	private int time = 0;
	private Stage stage = Stage.WAITING;
	private String world;
	
	public Round(String world){
		this.world = world;
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
		rounds.remove(this);
		this.world = world;
		rounds.add(this);
	}
	
	public void setStage(Stage s){
		rounds.remove(this);
		stage = s;
		rounds.add(this);
	}
	
	public void setTime(int t){
		rounds.remove(this);
		time = t;
		rounds.add(this);
	}
	
	public void tickDown(){
		rounds.remove(this);
		time -= 1;
		rounds.add(this);
	}
	
	public void subtractTime(int t){
		rounds.remove(this);
		time -= t;
		rounds.add(this);
	}
	
	public void addTime(int t){
		rounds.remove(this);
		time += t;
		rounds.add(this);
	}
	
	public void destroy(){
		rounds.remove(this);
	}
	
	public static Round getRound(String n){
		for (Round r : rounds){
			if (r.getWorld().equals(n))
				return r;
		}
		return new Round(n);
	}
	
}
