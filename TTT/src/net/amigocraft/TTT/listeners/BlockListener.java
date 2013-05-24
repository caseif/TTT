package net.amigocraft.TTT.listeners;

import static net.amigocraft.TTT.TTTPlayer.getTTTPlayer;
import static net.amigocraft.TTT.TTTPlayer.isPlayer;

import net.amigocraft.TTT.Body;
import net.amigocraft.TTT.FixedLocation;
import net.amigocraft.TTT.TTT;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class BlockListener implements Listener {

	private TTT plugin = TTT.plugin;
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if (isPlayer(e.getPlayer().getName()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if (isPlayer(e.getPlayer().getName()))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent e){
		for (HumanEntity he : e.getViewers()){
			Player p = (Player)he;
			if (isPlayer(p.getName())){
				if (getTTTPlayer(p.getName()).isDead()){
					e.setCancelled(true);
				}
				else if (e.getInventory().getType() == InventoryType.CHEST){
					Block block = ((Chest)e.getInventory().getHolder()).getBlock();
					for (Body b : plugin.bodies){
						if (b.getLocation().equals(FixedLocation.getFixedLocation(block))){
							e.setCancelled(true);
							break;
						}
					}
				}
			}
		}
	}
}
