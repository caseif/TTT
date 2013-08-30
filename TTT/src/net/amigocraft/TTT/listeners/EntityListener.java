package net.amigocraft.TTT.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.TTTPlayer;

public class EntityListener implements Listener {

	@EventHandler
	public void onEntityTarget(EntityTargetEvent e){
		if (e.getTarget() instanceof Player){
			if (TTTPlayer.isPlayer(((Player)e.getTarget()).getName()))
				e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e){
		if (Round.getRound(e.getEntity().getWorld().getName().replace("TTT_", "")) != null)
			e.setCancelled(true);
	}

	@EventHandler
	public void onHangingBreak(HangingBreakByEntityEvent e){
		if (e.getRemover() instanceof Player){
			if (TTTPlayer.isPlayer(((Player)e.getRemover()).getName()))
				e.setCancelled(true);
		}
	}
	
}
