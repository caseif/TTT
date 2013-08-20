package net.amigocraft.TTT.listeners;

import static net.amigocraft.TTT.TTTPlayer.isPlayer;

import net.amigocraft.TTT.LobbySign;
import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.managers.LobbyManager;
import net.amigocraft.TTT.utils.NumUtils;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
		for (LobbySign l : LobbyManager.signs){
			if (l.getX() == e.getBlock().getX() && l.getY() == e.getBlock().getY() &&
					l.getZ() == e.getBlock().getZ() && l.getWorld() == e.getBlock().getWorld().getName())
				if (e.getPlayer().hasPermission("ttt.lobby.destroy"))
					LobbyManager.removeSign(l);
				else {
					e.getPlayer().sendMessage(ChatColor.RED +
							"[TTT] You do not have permission to destroy this lobby sign!");
					e.setCancelled(true);
				}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e){
		if (e.getLine(0).equalsIgnoreCase("[TTT]")){
			if (e.getPlayer().hasPermission("ttt.lobby.create")){
				if (!e.getLine(3).equals(""))
					if (NumUtils.isInt(e.getLine(3)))
						LobbyManager.addSign(e.getBlock(), e.getLine(2), e.getLine(1).toLowerCase(),
								Integer.parseInt(e.getLine(3)), e.getPlayer());
					else
						e.getPlayer().sendMessage(ChatColor.RED + TTT.local.getMessage("invalid-sign"));
				else
					LobbyManager.addSign(e.getBlock(), e.getLine(2), e.getLine(1).toLowerCase(), 0, e.getPlayer());
			}
			else
				e.getPlayer().sendMessage(ChatColor.RED + TTT.local.getMessage("no-permission"));
		}
	}
}
