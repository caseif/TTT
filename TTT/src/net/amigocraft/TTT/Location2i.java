package net.amigocraft.TTT;

import org.bukkit.block.Block;

public class Location2i {
	public int x;
	public int y;
	public int z;
	public String worldName;

	public Location2i(String worldname, int x, int y, int z){
		this.worldName = worldname;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Location2i getLocation(Block block){
		if (block.getWorld() == null)
			return null;
		return new Location2i(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
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
		return worldName.equals(((Location2i)l).getWorld()) && x == ((Location2i)l).getX() &&
				y == ((Location2i)l).getY() && z == ((Location2i)l).getZ();
	}
	
	public int hashCode(){
		return 41 * (41 + worldName.hashCode() + x + y + z);
	}
}

