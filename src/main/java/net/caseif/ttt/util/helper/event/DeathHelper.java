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
package net.caseif.ttt.util.helper.event;

import net.caseif.flint.challenger.Challenger;
import net.caseif.ttt.Body;
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;
import net.caseif.ttt.util.helper.platform.NmsHelper;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for player death-related functionality.
 */
public final class DeathHelper {

    private final PlayerDeathEvent event;
    private final Player player;

    public DeathHelper(PlayerDeathEvent event) {
        this.event = event;
        this.player = event.getEntity();
    }

    public DeathHelper(Player player) {
        this.event = null;
        this.player = player;
    }

    public void handleEvent() {
        // admittedly not the best way of doing this, but easiest for the purpose of porting
        Optional<Challenger> chOpt = TTTCore.mg.getChallenger(player.getUniqueId());
        if (!chOpt.isPresent()) {
            return;
        }
        Challenger ch = chOpt.get();
        Location loc = player.getLocation();

        Optional<Challenger> killer = getKiller();

        cancelEvent(ch);

        if (killer.isPresent()) {
            // set killer's karma
            KarmaHelper.applyKillKarma(killer.get(), ch);
        }

        Block block = loc.getBlock();
        while (block.getType() != Material.AIR && block.getType() != Material.WATER
                && block.getType() != Material.LAVA && block.getType() != Material.STATIONARY_WATER
                && block.getType() != Material.STATIONARY_LAVA) {
            block = loc.add(0, 1, 0).getBlock();
        }

        ch.getRound().getArena().markForRollback(LocationHelper.convert(block.getLocation()));

        createBody(block.getLocation(), ch, killer.orNull());

        ch.getRound().getMetadata().<ScoreboardManager>get(MetadataTag.SCOREBOARD_MANAGER).get().updateEntry(ch);
    }

    private void cancelEvent(Challenger ch) {
        Location loc = player.getLocation(); // sending the packet resets the location

        if (event != null) {
            event.setDeathMessage("");
            event.getDrops().clear();

            NmsHelper.sendRespawnPacket(player);
            player.teleport(loc);
        }
        ch.setSpectating(true);
        player.setHealth(player.getMaxHealth());
    }

    private Optional<Challenger> getKiller() {
        if (event == null || player.getKiller() == null) {
            return Optional.absent();
        }

        UUID uuid = null;
        if (player.getKiller().getType() == EntityType.PLAYER) {
            uuid = player.getKiller().getUniqueId();
        } else if (player.getKiller() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) player).getShooter();
            if (shooter instanceof Player) {
                uuid = ((Player) shooter).getUniqueId();
            }
        }

        if (uuid != null) {
            return TTTCore.mg.getChallenger(uuid);
        }
        return Optional.absent();
    }

    private void createBody(Location loc, Challenger ch, Challenger killer) {
        loc.getBlock().setType(((loc.getBlockX() + loc.getBlockY()) % 2 == 0)
                ? Material.TRAPPED_CHEST
                : Material.CHEST);

        storeBody(loc, ch, killer);
    }

    private void storeBody(Location loc, Challenger ch, Challenger killer) {
        List<Body> bodies = ch.getRound().getMetadata().<List<Body>>get(MetadataTag.BODY_LIST).orNull();
        if (bodies == null) {
            bodies = new ArrayList<>();
        }

        long expiry = -1;
        if (killer != null) {
            double dist = player.getLocation().toVector()
                    .distance(Bukkit.getPlayer(killer.getUniqueId()).getLocation().toVector());
            if (dist <= TTTCore.config.KILLER_DNA_RANGE) {
                final double a = 0.2268; // copied from the official gamemode and scaled to account for different units
                int decayTime = TTTCore.config.KILLER_DNA_BASETIME - (int) Math.floor(a * Math.pow(dist, 2));
                if (decayTime > 0) {
                    expiry = System.currentTimeMillis() + (decayTime * 1000);
                }
            }
        }

        Body body;
        bodies.add(body = new Body(
                ch.getRound(),
                LocationHelper.convert(loc),
                ch.getUniqueId(),
                ch.getName(),
                killer != null ? killer.getUniqueId() : null,
                ch.getMetadata().has(Role.DETECTIVE)
                        ? Role.DETECTIVE
                        : (ch.getTeam().isPresent() ? ch.getTeam().get().getId() : null),
                System.currentTimeMillis(),
                expiry
        ));
        ch.getRound().getMetadata().set(MetadataTag.BODY_LIST, bodies);
        ch.getMetadata().set(MetadataTag.BODY, body);
    }

}
