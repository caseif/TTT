/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016, Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.caseif.ttt.listeners.player;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.helper.event.DeathHelper;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener for player events initiated by an automatic process (that is, one
 * not necessarily directly invoked by a player).
 */
public class PlayerUpdateListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        new DeathHelper(event).handleEvent();
    }

    @EventHandler
    public void onHealthRegenerate(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (TTTCore.mg.getChallenger(p.getUniqueId()).isPresent()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (TTTCore.mg.getChallenger(event.getEntity().getUniqueId()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Optional<Challenger> victim = TTTCore.mg.getChallenger(event.getEntity().getUniqueId());
            if (victim.isPresent()
                    && (victim.get().getRound().getLifecycleStage() == Constants.Stage.WAITING
                    || victim.get().getRound().getLifecycleStage() == Constants.Stage.PREPARING)) {
                event.setCancelled(true);
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    Bukkit.getPlayer(victim.get().getUniqueId()).teleport(
                            LocationHelper.convert(victim.get().getRound().getArena().getSpawnPoints().get(0))
                    );
                }
                return;
            }

            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent) event;
                if (ed.getDamager().getType() == EntityType.PLAYER
                        || (ed.getDamager() instanceof Projectile
                        && ((Projectile) ed.getDamager()).getShooter() instanceof Player)) {
                    Player damager = ed.getDamager().getType() == EntityType.PLAYER
                            ? (Player) ed.getDamager()
                            : (Player) ((Projectile) ed.getDamager()).getShooter();
                    Optional<Challenger> damagerCh = TTTCore.mg.getChallenger(damager.getUniqueId());
                    if (damagerCh.isPresent()) {
                        if ((damagerCh.get().getRound().getLifecycleStage() == Constants.Stage.WAITING
                                || damagerCh.get().getRound().getLifecycleStage() == Constants.Stage.PREPARING)
                                || !victim.isPresent()
                                || damagerCh.get().isSpectating()) {
                            event.setCancelled(true);
                            return;
                        }

                        if (damager.getItemInHand() != null
                                && damager.getItemInHand().getItemMeta() != null
                                && damager.getItemInHand().getItemMeta().getDisplayName() != null
                                && damager.getItemInHand().getItemMeta().getDisplayName()
                                .endsWith(TTTCore.locale.getLocalizable("item.crowbar.name")
                                        .localize())) {
                            event.setDamage(TTTCore.config.get(ConfigKey.CROWBAR_DAMAGE));
                        }

                        Optional<Double> reduc = damagerCh.get().getMetadata()
                                .get(Constants.MetadataTag.DAMAGE_REDUCTION);
                        if (reduc.isPresent()) {
                            event.setDamage((int) (event.getDamage() * reduc.get()));
                        }

                        KarmaHelper.applyDamageKarma(damagerCh.get(), victim.get(), event.getDamage());
                    }
                }
            }
        }
    }

}
