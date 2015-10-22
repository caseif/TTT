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
package net.caseif.ttt.util.helper.event;

import net.caseif.ttt.Body;
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.misc.MiscHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;
import net.caseif.ttt.util.helper.platform.NmsHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.PlayerDeathEvent;
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

/**
 * Utility class for player death-related functionality.
 */
public class DeathHelper {

    private static Field fieldRbHelper;
    private static Method logBlockChange;

    private final PlayerDeathEvent event;

    public DeathHelper(PlayerDeathEvent event) {
        this.event = event;
    }

    public void handleEvent() {
        Player pl = event.getEntity();
        // admittedly not the best way of doing this, but easiest for the purpose of porting
        Optional<Challenger> chOpt = TTTCore.mg.getChallenger(pl.getUniqueId());
        if (chOpt.isPresent()) {
            Challenger ch = chOpt.get();
            Location loc = event.getEntity().getLocation();

            cancelEvent(ch);

            Optional<Challenger> killer = getKiller();
            if (killer.isPresent()) {
                // set killer's karma
                KarmaHelper.applyKillKarma(killer.get(), ch);
            }

            if (ScoreboardManager.get(ch.getRound()).isPresent()) {
                ScoreboardManager.get(ch.getRound()).get().update(ch);
            }

            Block block = loc.getBlock();
            while (block.getType() != Material.AIR && block.getType() != Material.WATER
                    && block.getType() != Material.LAVA && block.getType() != Material.STATIONARY_WATER
                    && block.getType() != Material.STATIONARY_LAVA) {
                block = loc.add(0, 1, 0).getBlock();
            }

            flagForRollback(block.getLocation(), ch.getRound());

            createBody(block.getLocation(), ch, killer.orNull());
        }
    }

    private void cancelEvent(Challenger ch) {
        event.setDeathMessage("");
        event.getDrops().clear();

        Location loc = event.getEntity().getLocation(); // sending the packet resets the location
        NmsHelper.sendRespawnPacket(event.getEntity());
        event.getEntity().teleport(loc);
        ch.setSpectating(true);
        //ch.setPrefix(Config.SB_MIA_PREFIX); //TODO
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
    }

    private Optional<Challenger> getKiller() {
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
                return TTTCore.mg.getChallenger(uuid);
            }
        }
        return Optional.absent();
    }

    private void createBody(Location loc, Challenger ch, Challenger killer) {
        loc.getBlock().setType(((loc.getBlockX() + loc.getBlockY()) % 2 == 0)
                ? Material.TRAPPED_CHEST
                : Material.CHEST);
        Chest chest = (Chest) loc.getBlock().getState();

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
            roleId = Role.DETECTIVE;
        } else if (!MiscHelper.isTraitor(ch)) {
            durability = 5;
            roleId = Role.INNOCENT;
        } else {
            durability = 14;
            roleId = Role.TRAITOR;
        }
        ti.setDurability(durability);
        tiMeta.setDisplayName(TTTCore.locale.getLocalizable("fragment." + roleId)
                .withPrefix(Color.DETECTIVE).localize());
        tiMeta.setLore(Collections.singletonList(TTTCore.locale.getLocalizable("item.id." + roleId).localize()));
        ti.setItemMeta(tiMeta);
        chest.getInventory().addItem(id, ti);

        storeBody(loc, ch, killer);
    }

    private void storeBody(Location loc, Challenger ch, Challenger killer) {
        List<Body> bodies = ch.getRound().getMetadata().<List<Body>>get(MetadataTag.BODY_LIST).orNull();
        if (bodies == null) {
            bodies = new ArrayList<>();
        }
        bodies.add(
                new Body(
                        ch.getRound(),
                        LocationHelper.convert(loc),
                        ch.getUniqueId(),
                        ch.getName(),
                        killer.getUniqueId(),
                        ch.getMetadata().has(Role.DETECTIVE)
                                ? Role.DETECTIVE
                                : (ch.getTeam().isPresent() ? ch.getTeam().get().getId() : null),
                        System.currentTimeMillis()
                )
        );
        ch.getRound().getMetadata().set(MetadataTag.BODY_LIST, bodies);
    }

    private void flagForRollback(Location loc, Round round) {
        //TTTCore.mg.getRollbackManager().logBlockChange(block, ch.getArena()); //TODO (probably Flint 1.1)
        //TODO: Add check for doors and such (sort of implemented as of 0.8)
        try {
            //TODO: temporary hack (I'm still a terrible person for even writing this)
            if (fieldRbHelper == null) {
                fieldRbHelper = round.getArena().getClass().getDeclaredField("rbHelper");
                fieldRbHelper.setAccessible(true);
            }
            Object rbHelper = fieldRbHelper.get(round.getArena());
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
    }

}
