package net.caseif.ttt.listeners;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.helper.LocationHelper;

import net.caseif.flint.arena.Arena;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Listener for world-related events.
 */
public class WorldListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER
                && TTTCore.mg.getChallenger(event.getEntity().getUniqueId()).isPresent()) {
            event.setCancelled(true);
        } else if (event.getEntity() instanceof Projectile
                && ((Projectile) event.getEntity()).getShooter() instanceof Player
                && TTTCore.mg.getChallenger(((Player) ((Projectile) event.getEntity()).getShooter()).getUniqueId())
                .isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (event.getTarget().getType() == EntityType.PLAYER
                && TTTCore.mg.getChallenger(event.getTarget().getUniqueId()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        for (Arena arena : TTTCore.mg.getArenas()) {
            if (arena.getWorld().equals(event.getLocation().getWorld().getName())
                    && arena.getBoundary().contains(LocationHelper.convert(event.getLocation()))) {
                event.setCancelled(true);
            }
        }
    }

}
