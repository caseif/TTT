package net.amigocraft.ttt.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class BlockUtils {

	public static Block getAdjacentBlock(Block block){
		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
				BlockFace.WEST, BlockFace.UP};
		for (BlockFace face : faces){
			Block adjBlock = block.getRelative(face);
			if (adjBlock.getState() instanceof Sign){
				if (face != BlockFace.UP){
					@SuppressWarnings("deprecation")
					byte data = adjBlock.getData();
					byte north = 0x2;
					byte south = 0x3;
					byte west = 0x4;
					byte east = 0x5;
					BlockFace attached = null;
					if (data == east){
						attached = BlockFace.WEST;
					}
					else if (data == west){
						attached = BlockFace.EAST;
					}
					else if (data == north){
						attached = BlockFace.SOUTH;
					}
					else if (data == south){
						attached = BlockFace.NORTH;
					}
					if (adjBlock.getType() == Material.SIGN_POST){
						attached = BlockFace.DOWN;
					}
					if (block.getX() == adjBlock.getRelative(attached).getX() && block.getY() == 
							adjBlock.getRelative(attached).getY() && block.getZ() ==
							adjBlock.getRelative(attached).getZ()){
						return adjBlock;
					}
				}
			}
		}
		return null;
	}
	
}
