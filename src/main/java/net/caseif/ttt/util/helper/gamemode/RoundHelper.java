/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2017, Max Roncace <me@caseif.net>
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

package net.caseif.ttt.util.helper.gamemode;

import static net.caseif.ttt.lobby.StatusLobbySignPopulator.SIGN_HASTE_SWITCH_PERIOD;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.constant.Color;
import net.caseif.ttt.util.constant.MetadataKey;
import net.caseif.ttt.util.constant.Role;
import net.caseif.ttt.util.constant.Stage;
import net.caseif.ttt.util.helper.platform.TitleHelper;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;

/**
 * Static-utility class for round-related methods.
 */
public final class RoundHelper {

    private static final ItemStack ITEM_CROWBAR;
    private static final ItemStack ITEM_GUN;
    private static final ItemStack ITEM_AMMO;
    private static final ItemStack ITEM_DNA_SCANNER;

    static {
        ITEM_CROWBAR = new ItemStack(TTTCore.config.get(ConfigKey.CROWBAR_ITEM), 1);
        ItemMeta cbMeta = ITEM_CROWBAR.getItemMeta();
        cbMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.crowbar.name").localize());
        ITEM_CROWBAR.setItemMeta(cbMeta);

        ITEM_GUN = new ItemStack(TTTCore.config.get(ConfigKey.GUN_ITEM), 1);
        ItemMeta gunMeta = ITEM_GUN.getItemMeta();
        gunMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.gun.name").localize());
        ITEM_GUN.setItemMeta(gunMeta);

        ITEM_AMMO = new ItemStack(Material.ARROW, TTTCore.config.get(ConfigKey.INITIAL_AMMO));

        ITEM_DNA_SCANNER = new ItemStack(Material.COMPASS, 1);
        ItemMeta dnaMeta = ITEM_DNA_SCANNER.getItemMeta();
        dnaMeta.setDisplayName(Color.INFO + TTTCore.locale.getLocalizable("item.dna-scanner.name").localize());
        ITEM_DNA_SCANNER.setItemMeta(dnaMeta);
    }

    private RoundHelper() {
    }

    @SuppressWarnings("deprecation")
    public static void startRound(Round round) {
        RoleHelper.assignRoles(round);
        ScoreboardManager sm
                = round.getMetadata().<ScoreboardManager>get(MetadataKey.Round.SCOREBOARD_MANAGER).get();
        sm.updateAllEntries();
        for (Challenger ch : round.getTeam(Role.TRAITOR).get().getChallengers()) {
            sm.applyScoreboard(ch);
        }
        distributeItems(round);

        for (Challenger ch : round.getChallengers()) {
            assert ch.getTeam().isPresent();
            Player pl = TTTCore.getPlugin().getServer().getPlayer(ch.getUniqueId());
            assert pl != null;

            pl.setHealth(pl.getMaxHealth());
            pl.setFoodLevel(20);

            if (ch.getTeam().get().getId().equals(Role.INNOCENT)) {
                if (ch.getMetadata().has(Role.DETECTIVE)) {
                    TTTCore.locale.getLocalizable("info.personal.status.role.detective")
                            .withPrefix(Color.DETECTIVE).sendTo(pl);
                    TitleHelper.sendStatusTitle(pl, Role.DETECTIVE);
                } else {
                    TTTCore.locale.getLocalizable("info.personal.status.role.innocent")
                            .withPrefix(Color.INNOCENT).sendTo(pl);
                    TitleHelper.sendStatusTitle(pl, Role.INNOCENT);
                }
            } else if (ch.getTeam().get().getId().equals(Role.TRAITOR)) {
                if (ch.getTeam().get().getChallengers().size() > 1) {
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor")
                            .withPrefix(Color.TRAITOR).sendTo(pl);
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor.allies")
                            .withPrefix(Color.TRAITOR).sendTo(pl);
                    for (Challenger traitor : ch.getTeam().get().getChallengers()) {
                        if (traitor != ch) { // don't list them as an ally to themselves
                            pl.sendMessage(Color.TRAITOR + "- " + traitor.getName());
                        }
                    }
                } else {
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor.alone")
                            .withPrefix(Color.TRAITOR).sendTo(pl);
                }
                TitleHelper.sendStatusTitle(pl, Role.TRAITOR);
            }

            if (TTTCore.config.get(ConfigKey.KARMA_DAMAGE_REDUCTION)) {
                KarmaHelper.applyDamageReduction(ch);
                double reduc = KarmaHelper.getDamageReduction(ch);
                String percentage = reduc < 1
                        ? (int) (reduc * 100) + "%"
                        : TTTCore.locale.getLocalizable("fragment.full").localizeFor(pl);
                TTTCore.locale.getLocalizable("info.personal.status.karma-damage")
                        .withPrefix(Color.INFO).withReplacements(KarmaHelper.getKarma(ch) + "", percentage)
                        .sendTo(pl);
            }
        }

        round.getMetadata().<ScoreboardManager>get(MetadataKey.Round.SCOREBOARD_MANAGER).get().updateAllEntries();

        broadcast(round, TTTCore.locale.getLocalizable("info.global.round.event.started")
                .withPrefix(Color.INFO));
    }

    public static void closeRound(Round round, boolean sendWinMessages) {
        KarmaHelper.allocateKarma(round);
        KarmaHelper.saveKarma(round);

        if (sendWinMessages) {
            boolean tVic = round.getMetadata().<Boolean>get(MetadataKey.Round.TRAITOR_VICTORY).or(false);
            String color = (tVic ? Color.TRAITOR : Color.INNOCENT);

            Localizable msg = TTTCore.locale.getLocalizable("info.global.round.event.end."
                    + (tVic ? Role.TRAITOR : Role.INNOCENT))
                    .withPrefix(color)
                    .withReplacements(Color.EM + round.getArena().getDisplayName() + color);
            if (TTTCore.config.get(ConfigKey.BROADCAST_WIN_MESSAGES_TO_SERVER)) {
                msg.broadcast();
            } else {
                for (Challenger ch : round.getChallengers()) {
                    msg.sendTo(Bukkit.getPlayer(ch.getUniqueId()));
                }
            }
        }
        TitleHelper.sendVictoryTitle(round,
                round.getMetadata().<Boolean>get(MetadataKey.Round.TRAITOR_VICTORY).or(false));
    }

    public static void distributeItems(Round round) {
        for (Challenger ch : round.getChallengers()) {
            distributeItems(ch);
        }
    }

    public static void distributeItems(Challenger chal) {
        Player pl = Bukkit.getPlayer(chal.getUniqueId());
        assert pl != null;
        pl.getInventory().clear();
        pl.getInventory().addItem(ITEM_CROWBAR, ITEM_GUN, ITEM_AMMO);
        if (chal.getMetadata().containsKey(Role.DETECTIVE)) {
            pl.getInventory().addItem(ITEM_DNA_SCANNER);
        }
    }

    /**
     * Broadcasts a {@link Localizable} to a {@link Round}.
     *
     * @param round The {@link Round} to broadcast to
     * @param localizable The {@link Localizable} to broadcast
     */
    public static void broadcast(Round round, Localizable localizable) {
        for (Challenger ch : round.getChallengers()) {
            Player pl = Bukkit.getPlayer(ch.getUniqueId());
            assert pl != null;
            localizable.sendTo(pl);
        }
    }

    public static String getTimeDisplay(Round round, boolean t, ChatColor defaultColor) {
        StringBuilder timeStr = new StringBuilder();

        boolean indefinite = round.getLifecycleStage().getDuration() == -1;
        boolean hasteDisp = round.getLifecycleStage() == Stage.PLAYING
                && !indefinite
                && TTTCore.config.get(ConfigKey.HASTE)
                && System.currentTimeMillis() % (SIGN_HASTE_SWITCH_PERIOD * 1000 * 2)
                >= SIGN_HASTE_SWITCH_PERIOD * 1000;
        long dispTime;
        if (indefinite) {
            dispTime = round.getTime();
        } else {
            if (hasteDisp || round.getLifecycleStage() != Stage.PLAYING) {
                dispTime = round.getRemainingTime();
            } else {
                dispTime = round.getRemainingTime()
                        - round.getMetadata().<Integer>get(MetadataKey.Round.HASTE_TIME).or(0);
            }
        }
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(2);
        final int secondsPerMinute = 60;
        String minutes = nf.format(dispTime / secondsPerMinute);
        String seconds = nf.format(dispTime % secondsPerMinute);
        if (hasteDisp) {
            timeStr.append(ChatColor.RED.toString());
            if (t) {
                timeStr.append(minutes).append(":").append(seconds);
            } else {
                timeStr.append(TTTCore.locale.getLocalizable("fragment.haste-mode").localize());
            }
        } else {
            if (defaultColor != null) {
                timeStr.append(defaultColor.toString());
            }
            if (round.getLifecycleStage() == Stage.PLAYING && dispTime <= 0) {
                timeStr.append(TTTCore.locale.getLocalizable("fragment.overtime").localize());
            } else {
                timeStr.append(minutes).append(":").append(seconds);
            }
        }
        return timeStr.toString();
    }

}
