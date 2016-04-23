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

package net.caseif.ttt.listeners.wizard;

import static net.caseif.ttt.util.constant.Text.DIVIDER;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.constant.Color;
import net.caseif.ttt.util.helper.gamemode.ArenaHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Listener for wizard-related events.
 *
 * @author Max Roncace
 */
public class WizardListener implements Listener {

    public static BiMap<UUID, Integer> WIZARDS = HashBiMap.create();
    public static BiMap<UUID, Object[]> WIZARD_INFO = HashBiMap.create();

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("fallthrough")
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
            int stage = WIZARDS.get(event.getPlayer().getUniqueId());
            if (event.getMessage().equalsIgnoreCase(TTTCore.locale
                    .getLocalizable("info.personal.arena.create.cancel-keyword")
                    .localizeFor(event.getPlayer()))) {
                event.setCancelled(true);
                WIZARDS.remove(event.getPlayer().getUniqueId());
                WIZARD_INFO.remove(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(DIVIDER);
                TTTCore.locale.getLocalizable("info.personal.arena.create.cancelled").withPrefix(Color.ERROR)
                        .sendTo(event.getPlayer());
                return;
            }
            event.setCancelled(true);
            switch (stage) {
                case Stage.WIZARD_ID: {
                    if (!TTTCore.mg.getArena(event.getMessage()).isPresent()) {
                        if (event.getMessage().contains(".") || event.getMessage().contains(" ")) {
                            TTTCore.locale.getLocalizable("error.arena.create.invalid-id")
                                    .withPrefix(Color.ERROR).sendTo(event.getPlayer());
                            break;
                        }
                        increment(event.getPlayer());
                        WIZARD_INFO.get(event.getPlayer().getUniqueId())[Stage.WIZARD_ID] = event.getMessage();
                        event.getPlayer().sendMessage(DIVIDER);
                        TTTCore.locale.getLocalizable("info.personal.arena.create.id")
                                .withPrefix(Color.INFO)
                                .withReplacements(Color.FLAIR + event.getMessage().toLowerCase() + Color.INFO)
                                .sendTo(event.getPlayer());
                    } else {
                        TTTCore.locale.getLocalizable("error.arena.create.id-already-exists")
                                .withPrefix(Color.ERROR).sendTo(event.getPlayer());
                    }
                    break;
                }
                case Stage.WIZARD_SPAWN_POINT: {
                    if (event.getMessage().equalsIgnoreCase(
                            TTTCore.locale.getLocalizable("info.personal.arena.create.ok-keyword")
                                    .localizeFor(event.getPlayer()))) {
                        Object[] info = WIZARD_INFO.get(event.getPlayer().getUniqueId());
                        Boundary boundary = new Boundary((Location3D) info[Stage.WIZARD_FIRST_BOUND],
                                (Location3D) info[Stage.WIZARD_SECOND_BOUND]);
                        Location3D spawn = LocationHelper.convert(event.getPlayer().getLocation());

                        if (!event.getPlayer().getWorld().getName().equals(((Location3D) WIZARD_INFO
                                .get(event.getPlayer().getUniqueId())[Stage.WIZARD_FIRST_BOUND])
                                .getWorld().get())
                                || !boundary.contains(spawn)) {
                            TTTCore.locale.getLocalizable("error.arena.create.bad-spawn").withPrefix(Color.ERROR)
                                    .sendTo(event.getPlayer());
                            break;
                        }
                        TTTCore.mg.createArena((String) info[Stage.WIZARD_ID], spawn,
                                boundary);
                        ArenaHelper.updateShuffledArenas();
                        event.getPlayer().sendMessage(DIVIDER);
                        TTTCore.locale.getLocalizable("info.personal.arena.create.success").withPrefix(Color.INFO)
                                .withReplacements(Color.FLAIR + "/ttt join "
                                        + ((String) info[Stage.WIZARD_ID]).toLowerCase() + Color.INFO)
                                .sendTo(event.getPlayer());
                        WIZARDS.remove(event.getPlayer().getUniqueId());
                        WIZARD_INFO.remove(event.getPlayer().getUniqueId());
                        break;
                    }
                    // fall-through is intentional
                }
                default: {
                    event.setCancelled(false);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("fallthrough")
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
                int stage = WIZARDS.get(event.getPlayer().getUniqueId());
                event.setCancelled(true);
                Block c = event.getClickedBlock();
                switch (stage) {
                    case Stage.WIZARD_FIRST_BOUND:
                        increment(event.getPlayer());
                        WIZARD_INFO.get(event.getPlayer().getUniqueId())[Stage.WIZARD_FIRST_BOUND]
                                = new Location3D(c.getWorld().getName(), c.getX(), 0, c.getZ());
                        event.getPlayer().sendMessage(DIVIDER);
                        TTTCore.locale.getLocalizable("info.personal.arena.create.bound-1")
                                .withPrefix(Color.INFO)
                                .withReplacements(Color.FLAIR + "(x=" + c.getX() + ", z=" + c.getZ() + ")" + Color.INFO)
                                .sendTo(event.getPlayer());
                        break;
                    case Stage.WIZARD_SECOND_BOUND:
                        if (c.getWorld().getName().equals(((Location3D) WIZARD_INFO
                                        .get(event.getPlayer().getUniqueId())[Stage.WIZARD_FIRST_BOUND])
                                        .getWorld().get()
                        )) {
                            increment(event.getPlayer());
                            WIZARD_INFO.get(event.getPlayer().getUniqueId())[Stage.WIZARD_SECOND_BOUND]
                                    = new Location3D(c.getWorld().getName(), c.getX(), c.getWorld().getMaxHeight(),
                                    c.getZ());
                            event.getPlayer().sendMessage(DIVIDER);
                            TTTCore.locale.getLocalizable("info.personal.arena.create.bound-2")
                                    .withPrefix(Color.INFO)
                                    .withReplacements(Color.FLAIR + "(x=" + c.getX() + ", z=" + c.getZ() + ")"
                                            + Color.INFO,
                                            Color.FLAIR
                                            + TTTCore.locale.getLocalizable("info.personal.arena.create.ok-keyword")
                                            .localizeFor(event.getPlayer()) + Color.INFO)
                                    .sendTo(event.getPlayer());
                        } else {
                            TTTCore.locale.getLocalizable("error.arena.create.bad-bound")
                                    .withPrefix(Color.ERROR).sendTo(event.getPlayer());
                        }
                        break;
                    default:
                        event.setCancelled(false);
                        break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
            WIZARDS.remove(event.getPlayer().getUniqueId());
            WIZARD_INFO.remove(event.getPlayer().getUniqueId());
        }
    }

    private void increment(Player player) {
        WIZARDS.put(player.getUniqueId(), WIZARDS.get(player.getUniqueId()) + 1);
    }

    private class Stage {
        private static final int WIZARD_ID = 0;
        private static final int WIZARD_FIRST_BOUND = 1;
        private static final int WIZARD_SECOND_BOUND = 2;
        private static final int WIZARD_SPAWN_POINT = 3;
    }

}
