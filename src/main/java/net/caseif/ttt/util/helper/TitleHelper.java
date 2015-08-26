package net.caseif.ttt.util.helper;

import static com.google.common.base.Preconditions.checkNotNull;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants;

import net.caseif.crosstitles.TitleUtil;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import net.caseif.rosetta.Localizable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Static utility class for title-related functionality.
 */
public final class TitleHelper {

    public static void sendStatusTitle(Player player, String role) {
        if (ConfigHelper.SEND_TITLES && TitleUtil.areTitlesSupported()) {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null!");
            }
            role = role.toLowerCase();
            String title = TTTCore.locale.getLocalizable("info.personal.status.role." + role + ".title")
                    .localizeFor(player);
            ChatColor color;
            switch (role) {
                case Constants.Role.INNOCENT: {
                    color = Constants.Color.INNOCENT;
                    break;
                }
                case Constants.Role.DETECTIVE: {
                    color = Constants.Color.DETECTIVE;
                    break;
                }
                default: {
                    color = Constants.Color.TRAITOR;
                    break;
                }
            }
            if (ConfigHelper.SMALL_STATUS_TITLES) {
                TitleUtil.sendTitle(player, "", ChatColor.RESET, title, color);
            } else {
                TitleUtil.sendTitle(player, title, color);
            }
        }
    }

    public static void sendVictoryTitle(Round round, boolean traitorVictory) {
        if (ConfigHelper.SEND_TITLES && TitleUtil.areTitlesSupported()) {
            checkNotNull(round, "Round cannot be null!");
            Localizable loc = TTTCore.locale.getLocalizable("info.global.round.event.end."
                    + (traitorVictory ? Constants.Role.TRAITOR : Constants.Role.INNOCENT) + ".min");
            ChatColor color = traitorVictory ? Constants.Color.TRAITOR : Constants.Color.INNOCENT;
            for (Challenger ch : round.getChallengers()) {
                Player pl = Bukkit.getPlayer(ch.getUniqueId());
                if (ConfigHelper.SMALL_VICTORY_TITLES) {
                    TitleUtil.sendTitle(Bukkit.getPlayer(ch.getUniqueId()), "", ChatColor.RESET, loc.localizeFor(pl),
                            color);
                } else {
                    TitleUtil.sendTitle(Bukkit.getPlayer(ch.getUniqueId()), loc.localizeFor(pl), color);
                }
            }
        }
    }

}
