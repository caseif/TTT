package net.amigocraft.ttt.listeners;

import static net.amigocraft.ttt.TTTPlayer.isPlayer;

import net.amigocraft.ttt.LobbySign;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.LobbyManager;
import net.amigocraft.ttt.utils.BlockUtils;
import net.amigocraft.ttt.utils.NumUtils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class BlockListener implements Listener {

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if (isPlayer(e.getPlayer().getName()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if (isPlayer(e.getPlayer().getName())){
			e.setCancelled(true);
			return;
		}
		Block adjBlock = null;
		adjBlock = BlockUtils.getAdjacentBlock(e.getBlock());
		if (adjBlock != null){
			for (LobbySign l : LobbyManager.signs){
				if (l.getX() == adjBlock.getX() && l.getY() == adjBlock.getY()
						&& l.getZ() == adjBlock.getZ() &&
						l.getWorld().equals(adjBlock.getWorld().getName())){
					e.setCancelled(true);
					e.getPlayer().sendMessage(ChatColor.RED + "[TTT] This block holds a lobby " +
							"sign! To unregister the sign, hold shift and left-click it.");
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e){
		if (e.getLine(0).equalsIgnoreCase("[TTT]")){
			if (e.getPlayer().hasPermission("ttt.lobby.create")){
				if (e.getBlock().getType() == Material.WALL_SIGN){
					if (!e.getLine(3).equals(""))
						if (NumUtils.isInt(e.getLine(3)))
							LobbyManager.addSign(e.getBlock(), e.getLine(2), e.getLine(1).toLowerCase(),
									Integer.parseInt(e.getLine(3)), e.getPlayer());
						else
							e.getPlayer().sendMessage(ChatColor.RED + Main.local.getMessage("invalid-sign"));
					else
						LobbyManager.addSign(e.getBlock(), e.getLine(2), e.getLine(1).toLowerCase(), 0, e.getPlayer());
				}
			}
			else
				e.getPlayer().sendMessage(ChatColor.RED + Main.local.getMessage("no-permission"));
		}
	}

	@EventHandler
	public void onBlockBurn(BlockBurnEvent e){
		Block adjBlock = null;
		adjBlock = BlockUtils.getAdjacentBlock(e.getBlock());
		if (adjBlock != null){
			for (LobbySign l : LobbyManager.signs){
				if (l.getX() == adjBlock.getX() && l.getY() == adjBlock.getY()
						&& l.getZ() == adjBlock.getZ() &&
						l.getWorld().equals(adjBlock.getWorld().getName())){
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBlockFade(BlockFadeEvent e){
		Block adjBlock = null;
		adjBlock = BlockUtils.getAdjacentBlock(e.getBlock());
		if (adjBlock != null){
			for (LobbySign l : LobbyManager.signs){
				if (l.getX() == adjBlock.getX() && l.getY() == adjBlock.getY()
						&& l.getZ() == adjBlock.getZ() &&
						l.getWorld().equals(adjBlock.getWorld().getName())){
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent e){
		Block adjBlock = null;
		adjBlock = BlockUtils.getAdjacentBlock(e.getBlock());
		if (adjBlock != null){
			for (LobbySign l : LobbyManager.signs){
				if (l.getX() == adjBlock.getX() && l.getY() == adjBlock.getY()
						&& l.getZ() == adjBlock.getZ() &&
						l.getWorld().equals(adjBlock.getWorld().getName())){
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent e){
		for (Block b : e.getBlocks()){
			Block adjBlock = null;
			adjBlock = BlockUtils.getAdjacentBlock(b);
			if (adjBlock != null){
				for (LobbySign l : LobbyManager.signs){
					if (l.getX() == adjBlock.getX() && l.getY() == adjBlock.getY()
							&& l.getZ() == adjBlock.getZ() &&
							l.getWorld().equals(adjBlock.getWorld().getName())){
						e.setCancelled(true);
						break;
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent e){
		Block b = e.getRetractLocation().getBlock();
		if (b.getState() instanceof Sign){
			for (LobbySign l : LobbyManager.signs){
				if (l.getX() == b.getX() && l.getY() == b.getY()
						&& l.getZ() == b.getZ() &&
						l.getWorld().equals(b.getWorld().getName())){
					e.setCancelled(true);
					break;
				}
			}
		}
		Block adjBlock = BlockUtils.getAdjacentBlock(e.getRetractLocation().getBlock());
		if (adjBlock != null){
			for (LobbySign l : LobbyManager.signs){
				if (l.getX() == adjBlock.getX() && l.getY() == adjBlock.getY()
						&& l.getZ() == adjBlock.getZ() &&
						l.getWorld().equals(adjBlock.getWorld().getName())){
					e.setCancelled(true);
					break;
				}
			}
		}
	}
}
