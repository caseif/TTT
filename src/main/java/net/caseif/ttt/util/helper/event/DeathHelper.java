/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2019, Max Roncace <me@caseif.net>
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

import static net.caseif.ttt.util.helper.gamemode.RoleHelper.isTraitor;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Body;
import net.caseif.ttt.util.helper.platform.MaterialHelper;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.constant.MetadataKey;
import net.caseif.ttt.util.constant.Role;
import net.caseif.ttt.util.helper.gamemode.KarmaHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;
import net.caseif.ttt.util.helper.platform.NmsHelper;
import net.caseif.ttt.util.helper.platform.PlayerHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.util.physical.Boundary;
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
            if (killer.get().getRound().getLifecycleStage().getDuration() != -1
                    && isTraitor(killer.get()) && !isTraitor(chOpt.get())) {
                killer.get().getRound().getMetadata().set(MetadataKey.Round.HASTE_TIME,
                        killer.get().getRound().getMetadata().<Integer>get(MetadataKey.Round.HASTE_TIME).or(0)
                                + TTTCore.config.get(ConfigKey.HASTE_SECONDS_PER_DEATH));
                killer.get().getRound().setTime(killer.get().getRound().getTime()
                        - TTTCore.config.get(ConfigKey.HASTE_SECONDS_PER_DEATH));
            }
        }

        Block block = loc.getBlock();
        while (block.getType() != Material.AIR && !MaterialHelper.instance().isLiquid(block.getType())) {
            block = loc.add(0, 1, 0).getBlock();
        }

        ch.getRound().getArena().markForRollback(LocationHelper.convert(block.getLocation()));

        createBody(block.getLocation(), ch, killer.orNull());

        ch.getRound().getMetadata().<ScoreboardManager>get(MetadataKey.Round.SCOREBOARD_MANAGER).get().updateEntry(ch);
    }

    private void cancelEvent(final Challenger ch) {
        Location loc = player.getLocation(); // sending the packet resets the location

        if (event != null) {
            event.setDeathMessage("");
            event.getDrops().clear();

            NmsHelper.sendRespawnPacket(player);
            player.teleport(loc);
        }

        ch.setSpectating(true);
        PlayerHelper.watchPlayerGameMode(ch);

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
        Boundary bound = ch.getRound().getArena().getBoundary();
        if (!bound.contains(LocationHelper.convert(loc))) {
            double x = loc.getX() > bound.getUpperBound().getX() ? bound.getUpperBound().getX()
                    : loc.getX() < bound.getLowerBound().getX() ? bound.getLowerBound().getX()
                    : loc.getX();
            double y = loc.getY() > bound.getUpperBound().getY() ? bound.getUpperBound().getY()
                    : loc.getY() < bound.getLowerBound().getY() ? bound.getLowerBound().getY()
                    : loc.getY();
            double z = loc.getZ() > bound.getUpperBound().getZ() ? bound.getUpperBound().getZ()
                    : loc.getZ() < bound.getLowerBound().getZ() ? bound.getLowerBound().getZ()
                    : loc.getZ();
            loc = new Location(loc.getWorld(), x, y, z);
        }
        loc.getBlock().setType(((loc.getBlockX() + loc.getBlockY()) % 2 == 0)
                ? Material.TRAPPED_CHEST
                : Material.CHEST);

        storeBody(loc, ch, killer);
    }

    private void storeBody(Location loc, Challenger ch, Challenger killer) {
        List<Body> bodies = ch.getRound().getMetadata().<List<Body>>get(MetadataKey.Round.BODY_LIST).orNull();
        if (bodies == null) {
            bodies = new ArrayList<>();
        }

        long expiry = -1;
        if (killer != null) {
            double dist = player.getLocation().toVector()
                    .distance(Bukkit.getPlayer(killer.getUniqueId()).getLocation().toVector());
            if (dist <= TTTCore.config.get(ConfigKey.KILLER_DNA_RANGE)) {
                final double a = 0.2268; // copied from the official gamemode and scaled to account for different units
                int decayTime
                        = TTTCore.config.get(ConfigKey.KILLER_DNA_BASETIME) - (int) Math.floor(a * Math.pow(dist, 2));
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
                ch.getMetadata().containsKey(Role.DETECTIVE)
                        ? Role.DETECTIVE
                        : (ch.getTeam().isPresent() ? ch.getTeam().get().getId() : null),
                System.currentTimeMillis(),
                expiry
        ));
        ch.getRound().getMetadata().set(MetadataKey.Round.BODY_LIST, bodies);
        ch.getMetadata().set(MetadataKey.Player.BODY, body);
    }

}
