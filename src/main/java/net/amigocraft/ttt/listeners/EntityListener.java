package net.amigocraft.ttt.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

import net.amigocraft.ttt.Main;

public class EntityListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityTarget(EntityTargetEvent e){
		if (e.getTarget() instanceof Player){
			if (Main.mg.isPlayer(((Player)e.getTarget()).getName()))
				e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent e){
		//TODO: cancel if in minigame world
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHangingBreak(HangingBreakByEntityEvent e){
		if (e.getRemover() instanceof Player){
			if (Main.mg.isPlayer(((Player)e.getRemover()).getName()))
				e.setCancelled(true);
		}
	}
	
}
