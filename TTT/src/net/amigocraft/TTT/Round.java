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
	
	public static Round getRound(String n){
		for (Round r : rounds){
			if (r.getWorld().equals(n))
				return r;
		}
		return new Round(n);
	}
	
}
