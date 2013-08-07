package net.amigocraft.TTT;

public class LobbySign {

	private int x;
	private int y;
	private int z;
	private String world;
	private String round;
	private int number;
	
	public LobbySign(int x, int y, int z, String world, String round, int number){
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
		this.number = number;
	}

	public int getX(){
		return x;
	}

	public void setX(int x){
		this.x = x;
	}

	public int getY(){
		return y;
	}

	public void setY(int y){
		this.y = y;
	}

	public int getZ(){
		return z;
	}

	public void setZ(int z){
		this.z = z;
	}

	public String getWorld(){
		return world;
	}

	public void setWorld(String world){
		this.world = world;
	}

	public String getRound(){
		return round;
	}

	public void setRound(String round){
		this.round = round;
	}

	public int getNumber(){
		return number;
	}

	public void setNumber(int number){
		this.number = number;
	}
	
}
