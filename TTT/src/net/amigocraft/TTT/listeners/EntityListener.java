package net.amigocraft.TTT.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.TTTPlayer;

public class EntityListener {

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

}
