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
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.helper.platform.ConfigHelper;
import net.caseif.ttt.util.helper.platform.InventoryHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.util.physical.Location3D;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * Static-utility class for body-related functionality.
 *
 * @author Max Roncace
 */
public class InteractHelper {

    public static void handleEvent(PlayerInteractEvent event, Challenger opener) {
        // handle body checking
        Location3D clicked = new Location3D(
                event.getClickedBlock().getWorld().getName(),
                event.getClickedBlock().getX(),
                event.getClickedBlock().getY(),
                event.getClickedBlock().getZ());
        if (!(event.getClickedBlock().getType() == Material.CHEST
                || event.getClickedBlock().getType() == Material.TRAPPED_CHEST)) {
            return;
        }

        List<Body> bodies = opener.getRound().getMetadata().<List<Body>>get(MetadataTag.BODY_LIST).orNull();
        if (bodies == null) {
            return;
        }
        for (Body b : bodies) {
            if (!b.getLocation().equals(clicked)) {
                continue;
            }

            if (opener.getMetadata().has(Role.DETECTIVE) && !opener.isSpectating()) { // handle DNA scanning
                if (event.getPlayer().getItemInHand() != null
                        && event.getPlayer().getItemInHand().getType() == Material.COMPASS
                        && event.getPlayer().getItemInHand().getItemMeta() != null
                        && event.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null
                        && event.getPlayer().getItemInHand().getItemMeta().getDisplayName().endsWith(
                        TTTCore.locale.getLocalizable("item.dna-scanner.name").localize())) {
                    event.setCancelled(true);
                    doDnaCheck(b, opener, event.getPlayer());
                    return;
                }
            }

            if (opener.isSpectating() || event.getPlayer().isSneaking()) {
                event.setCancelled(true);
                Inventory chestInv = ((Chest) event.getClickedBlock().getState()).getInventory();
                Inventory inv = TTTCore.getPlugin().getServer().createInventory(chestInv.getHolder(),
                        chestInv.getSize());
                inv.setContents(chestInv.getContents());
                event.getPlayer().openInventory(inv);
                TTTCore.locale.getLocalizable("info.personal.status.discreet-search")
                        .withPrefix(Color.INFO).sendTo(event.getPlayer());
                return;
            } else {
                Optional<Challenger> bodyPlayer = TTTCore.mg.getChallenger(b.getPlayer());
                String color = "";
                switch (b.getRole()) {
                    case Role.INNOCENT: {
                        color = Color.INNOCENT;
                        break;
                    }
                    case Role.TRAITOR: {
                        color = Color.TRAITOR;
                        break;
                    }
                    case Role.DETECTIVE: {
                        color = Color.DETECTIVE;
                        break;
                    }
                    default: {
                        event.getPlayer().sendMessage("Something's gone terribly wrong inside the TTT "
                                + "plugin. Please notify an admin."); // eh, may as well tell the player
                        throw new AssertionError("Failed to determine role of found body. "
                                + "Report this immediately.");
                    }
                }
                Localizable loc = TTTCore.locale.getLocalizable("info.global.round.event.body-find")
                        .withPrefix(color);
                Localizable roleMsg = TTTCore.locale
                        .getLocalizable("info.global.round.event.body-find." + b.getRole());
                for (Challenger c : b.getRound().getChallengers()) {
                    Player pl = Bukkit.getPlayer(c.getUniqueId());
                    pl.sendMessage(loc.withReplacements(event.getPlayer().getName(),
                            b.getName()).localizeFor(pl) + " "
                            + roleMsg.localizeFor(pl));
                }

                b.setFound();
                if (bodyPlayer.isPresent() && bodyPlayer.get().getRound().equals(b.getRound())) {
                    bodyPlayer.get().getMetadata().set(MetadataTag.BODY_FOUND, true);
                    if (ScoreboardManager.get(bodyPlayer.get().getRound()).isPresent()) {
                        ScoreboardManager.get(bodyPlayer.get().getRound()).get()
                                .update(bodyPlayer.get());
                    }
                }
            }
        }
    }

    public static void doDnaCheck(Body body, Challenger ch, Player pl) {
        if (body.getKiller().isPresent()) {
            Player killer = Bukkit.getPlayer(body.getKiller().get());
            if (killer != null
                    && TTTCore.mg.getChallenger(killer.getUniqueId()).isPresent()
                    && !TTTCore.mg.getChallenger(killer.getUniqueId()).get()
                    .isSpectating()) {
                ch.getMetadata().set("tracking", body.getKiller().get());
                TTTCore.locale.getLocalizable("info.personal.status.collect-dna")
                        .withPrefix(Color.INFO)
                        .withReplacements(body.getName())
                        .sendTo(pl);
            } else {
                TTTCore.locale.getLocalizable("error.round.killer-left")
                        .withPrefix(Color.ERROR).sendTo(pl);
            }
        } else {
            TTTCore.locale.getLocalizable("info.personal.status.no-dna")
                    .withPrefix(Color.ERROR).sendTo(pl);
        }
    }

    @SuppressWarnings("deprecation")
    public static void handleGun(PlayerInteractEvent event) {
        // guns
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
                || event.getPlayer().getItemInHand() == null
                || event.getPlayer().getItemInHand().getItemMeta() == null
                || event.getPlayer().getItemInHand().getItemMeta().getDisplayName() == null) {
            return;
        }

        if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName()
                .endsWith(TTTCore.locale.getLocalizable("item.gun.name").localize())) {

            Optional<Challenger> ch = TTTCore.mg.getChallenger(event.getPlayer().getUniqueId());
            if (!ch.isPresent() || ch.get().isSpectating()
                    || (ch.get().getRound().getLifecycleStage() != Constants.Stage.PLAYING)) {
                return;
            }

            event.setCancelled(true);
            if (event.getPlayer().getInventory().contains(Material.ARROW)
                    || !ConfigHelper.REQUIRE_AMMO_FOR_GUNS) {
                if (ConfigHelper.REQUIRE_AMMO_FOR_GUNS) {
                    InventoryHelper.removeArrow(event.getPlayer().getInventory());
                    event.getPlayer().updateInventory();
                }
                event.getPlayer().launchProjectile(Arrow.class);
            } else {
                TTTCore.locale.getLocalizable("info.personal.status.no-ammo")
                        .withPrefix(Color.ERROR).sendTo(event.getPlayer());
            }
        }
    }

}
