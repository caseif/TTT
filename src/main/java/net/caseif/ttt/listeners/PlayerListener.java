/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncacé <mproncace@lapis.blue>
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
package net.caseif.ttt.listeners;

import net.caseif.ttt.Body;
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.Constants.Stage;
import net.caseif.ttt.util.helper.misc.MiscHelper;
import net.caseif.ttt.util.helper.platform.ConfigHelper;
import net.caseif.ttt.util.helper.event.InteractHelper;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;
import net.caseif.ttt.util.helper.platform.NmsHelper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.util.physical.Location3D;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final ImmutableList<String> disabledCommands = ImmutableList.of("kit", "msg", "pm", "r", "me");

    private static Field fieldRbHelper;
    private static Method logBlockChange;

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).isPresent()) { // check if player is in TTT round
            Challenger ch = TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).get();
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                // disallow cheating/bed setting
                if (event.getClickedBlock().getType() == Material.ENDER_CHEST
                        || event.getClickedBlock().getType() == Material.BED_BLOCK) {
                    event.setCancelled(true);
                    return;
                }

                InteractHelper.handleEvent(event, ch);
            }
        }

        InteractHelper.handleGun(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Optional<Challenger> victim = TTTCore.mg.getChallenger(event.getEntity().getUniqueId());
            if (victim.isPresent() && victim.get().getRound().getLifecycleStage() != Stage.PLAYING
                    && event.getCause() == DamageCause.VOID) {
                event.setCancelled(true);
                Bukkit.getPlayer(victim.get().getUniqueId()).teleport(
                        LocationHelper.convert(victim.get().getRound().getArena().getSpawnPoints().get(0))
                );
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
                        if (damagerCh.get().getRound().getLifecycleStage() != Stage.PLAYING
                                || !victim.isPresent()
                                || damagerCh.get().isSpectating()) {
                            event.setCancelled(true);
                            return;
                        }
                        if (damager.getItemInHand() != null) {
                            if (damager.getItemInHand().getItemMeta() != null) {
                                if (damager.getItemInHand().getItemMeta().getDisplayName() != null) {
                                    if (damager.getItemInHand().getItemMeta().getDisplayName()
                                            .endsWith(TTTCore.locale.getLocalizable("item.crowbar.name")
                                                    .localize())) {
                                        event.setDamage(ConfigHelper.CROWBAR_DAMAGE);
                                    }
                                }
                            }
                        }
                        Optional<Double> reduc = damagerCh.get().getMetadata().get(MetadataTag.DAMAGE_REDUCTION);
                        if (reduc.isPresent()) {
                            event.setDamage((int) (event.getDamage() * reduc.get()));
                        }
                        KarmaHelper.applyDamageKarma(damagerCh.get(), victim.get(), event.getDamage());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).isPresent()) {
            event.setCancelled(true);
            TTTCore.locale.getLocalizable("info.personal.status.no-drop").withPrefix(Color.ERROR)
                    .sendTo(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!ConfigHelper.KARMA_PERSIST) {
            KarmaHelper.resetKarma(event.getPlayer().getUniqueId());
        }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        for (HumanEntity he : event.getViewers()) {
            Player p = (Player) he;
            Optional<Challenger> ch = TTTCore.mg.getChallenger(p.getUniqueId());
            if (ch.isPresent()) {
                if (event.getInventory().getType() == InventoryType.CHEST) {
                    Block block;
                    Block block2 = null;
                    if (event.getInventory().getHolder() instanceof Chest) {
                        block = ((Chest) event.getInventory().getHolder()).getBlock();
                    } else if (event.getInventory().getHolder() instanceof DoubleChest) {
                        block = ((Chest) ((DoubleChest) event.getInventory().getHolder()).getLeftSide()).getBlock();
                        block2 = ((Chest) ((DoubleChest) event.getInventory().getHolder()).getRightSide()).getBlock();
                    } else {
                        return;
                    }
                    Location3D l1 = LocationHelper.convert(block.getLocation());
                    Location3D l2 = block2 != null ? LocationHelper.convert(block2.getLocation()) : null;
                    for (Body b : ch.get().getRound().getMetadata().<List<Body>>get(MetadataTag.BODY_LIST).get()) {
                        if (b.getLocation().equals(l1) || b.getLocation().equals(l2)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    //TODO: probably split this up a little
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player pl = event.getEntity();
        // admittedly not the best way of doing this, but easiest for the purpose of porting
        Optional<Challenger> chOpt = TTTCore.mg.getChallenger(pl.getUniqueId());
        if (chOpt.isPresent()) {
            Challenger ch = chOpt.get();

            event.setDeathMessage("");
            event.getDrops().clear();

            Optional<Challenger> killer = Optional.absent();
            if (event.getEntity().getKiller() != null) {
                UUID uuid = null;
                if (event.getEntity().getType() == EntityType.PLAYER) {
                    uuid = event.getEntity().getKiller().getUniqueId();
                } else if (event.getEntity().getKiller() instanceof Projectile) {
                    ProjectileSource shooter = ((Projectile) event.getEntity()).getShooter();
                    if (shooter instanceof Player) {
                        uuid = ((Player) shooter).getUniqueId();
                    }
                }
                if (uuid != null) {
                    killer = TTTCore.mg.getChallenger(uuid);
                }
            }

            Location loc = pl.getLocation(); // sending the packet resets the location
            NmsHelper.sendRespawnPacket(pl);
            pl.teleport(loc);
            ch.setSpectating(true);
            //ch.setPrefix(Config.SB_MIA_PREFIX); //TODO
            pl.setHealth(pl.getMaxHealth());

            if (ScoreboardManager.get(ch.getRound()).isPresent()) {
                ScoreboardManager.get(ch.getRound()).get().update(ch);
            }

            if (killer.isPresent()) {
                // set killer's karma
                KarmaHelper.applyKillKarma(killer.get(), ch);
                ch.getMetadata().set("killer", killer.get().getUniqueId());
            }

            Block block = loc.getBlock();
            while (block.getType() != Material.AIR && block.getType() != Material.WATER
                    && block.getType() != Material.LAVA && block.getType() != Material.STATIONARY_WATER
                    && block.getType() != Material.STATIONARY_LAVA) {
                block = loc.add(0, 1, 0).getBlock();
            }
            //TTTCore.mg.getRollbackManager().logBlockChange(block, ch.getArena()); //TODO (probably Flint 1.1)
            //TODO: Add check for doors and such (sort of implemented as of 0.8)
            //TODO: move this code to another method
            try {
                //TODO: temporary hack (I'm still a terrible person for even writing this)
                if (fieldRbHelper == null) {
                    fieldRbHelper = ch.getRound().getArena().getClass().getDeclaredField("rbHelper");
                    fieldRbHelper.setAccessible(true);
                }
                Object rbHelper = fieldRbHelper.get(ch.getRound().getArena());
                if (logBlockChange == null) {
                    logBlockChange
                            = rbHelper.getClass().getDeclaredMethod("logBlockChange", Location.class, BlockState.class);
                    logBlockChange.setAccessible(true);
                }
                logBlockChange.invoke(rbHelper, loc, loc.getBlock().getState());

            } catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException
                    | NoSuchMethodException ex) {
                TTTCore.log.severe("Failed to log body for rollback");
                ex.printStackTrace();
            }
            block.setType(((loc.getBlockX() + loc.getBlockY()) % 2 == 0) ? Material.TRAPPED_CHEST : Material.CHEST);
            Chest chest = (Chest) block.getState();

            // player identifier
            ItemStack id = new ItemStack(TTTCore.HALLOWEEN ? Material.JACK_O_LANTERN : Material.PAPER, 1);
            ItemMeta idMeta = id.getItemMeta();
            idMeta.setDisplayName(TTTCore.locale.getLocalizable("item.id.name").localize());
            List<String> idLore = new ArrayList<>();
            idLore.add(TTTCore.locale.getLocalizable("corpse.of").withReplacements(ch.getName()).localize());
            idLore.add(ch.getName());
            idMeta.setLore(idLore);
            id.setItemMeta(idMeta);

            // role identifier
            ItemStack ti = new ItemStack(Material.WOOL, 1);
            ItemMeta tiMeta = ti.getItemMeta();
            short durability;
            String roleId = "";
            if (ch.getMetadata().has(Role.DETECTIVE)) {
                durability = 11;
                roleId = "detective";
            } else if (!MiscHelper.isTraitor(ch)) {
                durability = 5;
                roleId = "innocent";
            } else {
                durability = 14;
                roleId = "traitor";
            }
            ti.setDurability(durability);
            tiMeta.setDisplayName(TTTCore.locale.getLocalizable("fragment." + roleId)
                    .withPrefix(Color.DETECTIVE).localize());
            tiMeta.setLore(Collections.singletonList(TTTCore.locale.getLocalizable("item.id." + roleId).localize()));
            ti.setItemMeta(tiMeta);
            chest.getInventory().addItem(id, ti);
            List<Body> bodies = ch.getRound().getMetadata().<List<Body>>get(MetadataTag.BODY_LIST).orNull();
            if (bodies == null) {
                bodies = new ArrayList<>();
            }
            bodies.add(
                    new Body(
                            ch.getRound(),
                            LocationHelper.convert(block.getLocation()),
                            ch.getUniqueId(),
                            ch.getName(),
                            killer.isPresent() ? killer.get().getUniqueId() : null,
                            ch.getMetadata().has(Role.DETECTIVE)
                                    ? Role.DETECTIVE
                                    : (ch.getTeam().isPresent() ? ch.getTeam().get().getId() : null),
                            System.currentTimeMillis()
                    )
            );
            ch.getRound().getMetadata().set(MetadataTag.BODY_LIST, bodies);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String label = event.getMessage().split(" ")[0].substring(1);
        if (disabledCommands.contains(label)) {
            if (TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).isPresent()) {
                event.setCancelled(true);
                TTTCore.locale.getLocalizable("error.round.disabled-command").withPrefix(Color.ERROR)
                        .sendTo(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (TTTCore.mg.getChallenger(event.getEntity().getUniqueId()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Optional<Challenger> ch = TTTCore.mg.getChallenger(event.getPlayer().getUniqueId());
        if (ch.isPresent()) {
            if (ch.get().isSpectating()) {
                Localizable dead = TTTCore.locale.getLocalizable("fragment.dead");
                for (Player pl : event.getRecipients()) {
                    pl.sendMessage("[" + dead.localizeFor(pl).toUpperCase() + "]"
                            + "<" + event.getPlayer().getDisplayName() + "> "
                            + event.getMessage());
                }
                event.getRecipients().clear();
            }
        }
    }

}
