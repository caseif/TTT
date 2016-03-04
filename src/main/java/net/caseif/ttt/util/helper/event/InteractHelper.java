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

import net.caseif.ttt.Body;
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.Constants.Role;
import net.caseif.ttt.util.helper.data.CollectionsHelper;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static-utility class for player interact-related functionality.
 *
 * @author Max Roncace
 */
public final class InteractHelper {

    private InteractHelper() {
    }

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
        for (Body body : bodies) {
            if (!body.getLocation().equals(clicked)) {
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
                    doDnaCheck(body, opener, event.getPlayer());
                    return;
                }
            }

            event.setCancelled(true);
            searchBody(body, event.getPlayer(), ((Chest) event.getClickedBlock().getState()).getInventory().getSize());

            if (opener.isSpectating() || event.getPlayer().isSneaking()) {
                TTTCore.locale.getLocalizable("info.personal.status.discreet-search").withPrefix(Color.INFO)
                        .sendTo(event.getPlayer());
                return;
            } else if (!body.isFound()) {
                Optional<Challenger> bodyPlayer = TTTCore.mg.getChallenger(body.getPlayer());
                String color;
                switch (body.getRole()) {
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
                        throw new AssertionError("Failed to determine role of found body. Report this immediately.");
                    }
                }

                body.setFound();
                if (bodyPlayer.isPresent() && bodyPlayer.get().getRound() == body.getRound()) {
                    bodyPlayer.get().getMetadata().set(MetadataTag.BODY_FOUND, true);

                    ScoreboardManager sm = body.getRound().getMetadata()
                            .<ScoreboardManager>get(Constants.MetadataTag.SCOREBOARD_MANAGER).get();
                    sm.updateEntry(bodyPlayer.get());
                }

                Localizable loc = TTTCore.locale.getLocalizable("info.global.round.event.body-find").withPrefix(color);
                Localizable roleMsg
                        = TTTCore.locale.getLocalizable("info.global.round.event.body-find." + body.getRole());
                for (Challenger c : body.getRound().getChallengers()) {
                    Player pl = Bukkit.getPlayer(c.getUniqueId());
                    pl.sendMessage(loc.withReplacements(event.getPlayer().getName(),
                            body.getName()).localizeFor(pl) + " " + roleMsg.localizeFor(pl));
                }
            }
        }
    }

    public static void doDnaCheck(Body body, Challenger ch, Player pl) {
        if (!body.isFound()) {
            TTTCore.locale.getLocalizable("info.personal.status.dna-id")
                    .withPrefix(Color.ERROR).sendTo(pl);
            return;
        }

        if (!body.getKiller().isPresent() || body.getExpiry() == -1) {
            TTTCore.locale.getLocalizable("info.personal.status.no-dna")
                    .withPrefix(Color.ERROR).sendTo(pl);
            return;
        }

        if (System.currentTimeMillis() > body.getExpiry()) {
            TTTCore.locale.getLocalizable("info.personal.status.dna-decayed")
                    .withPrefix(Color.ERROR).sendTo(pl);
            return;
        }

        Player killer = Bukkit.getPlayer(body.getKiller().get());
        if (killer != null
                && TTTCore.mg.getChallenger(killer.getUniqueId()).isPresent()
                && !TTTCore.mg.getChallenger(killer.getUniqueId()).get().isSpectating()) {
            ch.getMetadata().set("tracking", body.getKiller().get());
            TTTCore.locale.getLocalizable("info.personal.status.collect-dna")
                    .withPrefix(Color.INFO)
                    .withReplacements(body.getName())
                    .sendTo(pl);
        } else {
            TTTCore.locale.getLocalizable("error.round.killer-left")
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
                    || (ch.get().getRound().getLifecycleStage() == Constants.Stage.WAITING
                    || ch.get().getRound().getLifecycleStage() == Constants.Stage.PREPARING)) {
                return;
            }

            event.setCancelled(true);
            if (event.getPlayer().getInventory().contains(Material.ARROW)
                    || !TTTCore.config.REQUIRE_AMMO_FOR_GUNS) {
                if (TTTCore.config.REQUIRE_AMMO_FOR_GUNS) {
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

    private static void searchBody(Body body, Player player, int size) {
        Inventory inv = Bukkit.createInventory(player, size);

        // player identifier
        {
            ItemStack id = new ItemStack(TTTCore.HALLOWEEN ? Material.JACK_O_LANTERN : Material.PAPER, 1);
            ItemMeta idMeta = id.getItemMeta();
            idMeta.setDisplayName(TTTCore.locale.getLocalizable("item.id.name").localizeFor(player));
            List<String> idLore = new ArrayList<>();
            idLore.add(TTTCore.locale.getLocalizable("item.id.desc").withReplacements(body.getName())
                    .localizeFor(player));
            idMeta.setLore(idLore);
            id.setItemMeta(idMeta);
            inv.addItem(id);
        }

        // role identifier
        {
            ItemStack roleId = new ItemStack(Material.WOOL, 1);
            ItemMeta roleIdMeta = roleId.getItemMeta();
            short durability;
            String roleStr = body.getRole();
            String prefix;
            switch (body.getRole()) {
                case Role.DETECTIVE: {
                    durability = 11;
                    prefix = Color.DETECTIVE;
                    break;
                }
                case Role.INNOCENT: {
                    durability = 5;
                    prefix = Color.INNOCENT;
                    break;
                }
                case Role.TRAITOR: {
                    durability = 14;
                    prefix = Color.TRAITOR;
                    break;
                }
                default: {
                    throw new AssertionError();
                }
            }
            roleId.setDurability(durability);
            roleIdMeta.setDisplayName(TTTCore.locale.getLocalizable("fragment." + roleStr).withPrefix(prefix)
                    .localizeFor(player));
            roleIdMeta.setLore(Collections.singletonList(
                    TTTCore.locale.getLocalizable("item.role." + roleStr).localizeFor(player)
            ));
            roleId.setItemMeta(roleIdMeta);
            inv.addItem(roleId);
        }

        // death clock
        {
            ItemStack clock = new ItemStack(Material.WATCH, 1);
            ItemMeta clockMeta = clock.getItemMeta();
            long deathSeconds = (System.currentTimeMillis() - body.getDeathTime()) / 1000L;
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);
            String deathTime = nf.format(deathSeconds / 60) + ":" + nf.format(deathSeconds % 60);
            clockMeta.setDisplayName(deathTime);
            clockMeta.setLore(CollectionsHelper.formatLore(
                    TTTCore.locale.getLocalizable("item.deathclock.desc").withReplacements(deathTime)
                            .withReplacements(deathTime).localizeFor(player)
            ));
            clock.setItemMeta(clockMeta);
            inv.addItem(clock);
        }

        // DNA sample
        if (body.getExpiry() > System.currentTimeMillis()) { // still has DNA
            long decaySeconds = (body.getExpiry() - System.currentTimeMillis()) / 1000L;
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);
            String decayTime = nf.format(decaySeconds / 60) + ":" + nf.format(decaySeconds % 60);
            ItemStack dna = new ItemStack(Material.LEASH, 1);
            ItemMeta dnaMeta = dna.getItemMeta();
            dnaMeta.setDisplayName(TTTCore.locale.getLocalizable("item.dna.name").localizeFor(player));
            dnaMeta.setLore(CollectionsHelper.formatLore(
                    TTTCore.locale.getLocalizable("item.dna.desc").withReplacements(decayTime).localizeFor(player)
            ));
            dna.setItemMeta(dnaMeta);
            inv.addItem(dna);
        }

        player.openInventory(inv);
        TTTCore.mg.getChallenger(player.getUniqueId()).get().getMetadata().set(MetadataTag.SEARCHING_BODY, true);
    }

}
