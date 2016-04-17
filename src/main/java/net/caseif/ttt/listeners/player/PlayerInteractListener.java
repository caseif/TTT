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
import net.caseif.ttt.util.helper.event.InteractHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Listener for player events initiated by a manual interaction.
 */
public class PlayerInteractListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // check if player is in TTT round
        if (TTTCore.mg.getChallenger(event.getPlayer().getUniqueId()).isPresent()) {
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
    public void onInventoryClick(InventoryClickEvent event) {
        for (HumanEntity he : event.getViewers()) {
            Player p = (Player) he;
            Optional<Challenger> ch = TTTCore.mg.getChallenger(p.getUniqueId());
            if (ch.isPresent() && ch.get().getMetadata().has(Constants.MetadataTag.SEARCHING_BODY)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        Optional<Challenger> ch = TTTCore.mg.getChallenger(event.getPlayer().getUniqueId());
        if (ch.isPresent() && ch.get().getMetadata().has(Constants.MetadataTag.SEARCHING_BODY)) {
            ch.get().getMetadata().remove(Constants.MetadataTag.SEARCHING_BODY);
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
            TTTCore.locale.getLocalizable("info.personal.status.no-drop").withPrefix(Constants.Color.ERROR)
                    .sendTo(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Optional<Challenger> ch = TTTCore.mg.getChallenger(event.getPlayer().getUniqueId());
        if (ch.isPresent()) {
            if (ch.get().isSpectating()) {
                boolean spec = ch.get().getMetadata().has(Constants.MetadataTag.PURE_SPECTATOR);

                Localizable prefix = TTTCore.locale.getLocalizable(spec ? "fragment.spectator" : "fragment.dead");
                ChatColor color = spec ? ChatColor.GRAY : ChatColor.RED;

                for (Player pl : event.getRecipients()) {
                    pl.sendMessage(color + "[" + prefix.localizeFor(pl).toUpperCase() + "]" + ChatColor.WHITE
                            + "<" + event.getPlayer().getDisplayName() + "> "
                            + event.getMessage());
                }

                event.getRecipients().clear();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Optional<Challenger> ch = TTTCore.mg.getChallenger(event.getPlayer().getUniqueId());
        if (ch.isPresent() && ch.get().getMetadata().has(Constants.MetadataTag.WATCH_GAME_MODE)) {
            event.setCancelled(true);
            ch.get().getMetadata().remove(Constants.MetadataTag.WATCH_GAME_MODE);
        }
    }

}
