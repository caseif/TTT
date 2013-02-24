package net.amigocraft.TTT;

import org.bukkit.block.Block;

public class FixedLocation {
	public int x;
	public int y;
	public int z;
	public String worldName;

	public FixedLocation(String worldname, int x, int y, int z){
		this.worldName = worldname;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static FixedLocation getFixedLocation(Block block){
		if (block.getWorld() == null)
			return null;
		return new FixedLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}
	
	public String getWorld(){
		return worldName;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getZ(){
		return z;
	}
	
	public String toString(){
		return worldName + ", " + x + ", " + y + ", " + z;
	}
	
	public boolean equals(Object l){
		return worldName.equals(((FixedLocation)l).getWorld()) && x == ((FixedLocation)l).getX() && y == ((FixedLocation)l).getY() && z == ((FixedLocation)l).getZ();
	}
	
	public int hashCode(){
		return 41 * (41 + worldName.hashCode() + x + y + z);
	}
}

