package net.caseif.ttt.util.helper.gamemode;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.helper.misc.MiscHelper;
import net.caseif.ttt.util.helper.platform.InventoryHelper;
import net.caseif.ttt.util.helper.platform.TitleHelper;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import org.bukkit.entity.Player;

/**
 * Static-utility class for round-related methods.
 */
public class RoundHelper {

    @SuppressWarnings("deprecation")
    public static void startRound(Round round) {
        RoleHelper.assignRoles(round);
        for (Challenger ch : round.getChallengers()) {
            ScoreboardManager.getOrCreate(round).update(ch);
        }
        InventoryHelper.distributeItems(round);
        ScoreboardManager.getOrCreate(round).assignScoreboards();

        for (Challenger ch : round.getChallengers()) {
            assert ch.getTeam().isPresent();
            Player pl = TTTCore.getPlugin().getServer().getPlayer(ch.getUniqueId());
            assert pl != null;

            pl.setHealth(pl.getMaxHealth());
            pl.setFoodLevel(20);

            if (ch.getTeam().get().getId().equals(Constants.Role.INNOCENT)) {
                if (ch.getMetadata().has(Constants.Role.DETECTIVE)) {
                    TTTCore.locale.getLocalizable("info.personal.status.role.detective")
                            .withPrefix(Constants.Color.DETECTIVE).sendTo(pl);
                    TitleHelper.sendStatusTitle(pl, Constants.Role.DETECTIVE);
                } else {
                    TTTCore.locale.getLocalizable("info.personal.status.role.innocent")
                            .withPrefix(Constants.Color.INNOCENT).sendTo(pl);
                    TitleHelper.sendStatusTitle(pl, Constants.Role.INNOCENT);
                }
            } else if (ch.getTeam().get().getId().equals(Constants.Role.TRAITOR)) {
                if (ch.getTeam().get().getChallengers().size() > 1) {
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor")
                            .withPrefix(Constants.Color.TRAITOR).sendTo(pl);
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor.allies")
                            .withPrefix(Constants.Color.TRAITOR).sendTo(pl);
                    for (Challenger traitor : ch.getTeam().get().getChallengers()) {
                        if (traitor != ch) { // don't list them as an ally to themselves
                            pl.sendMessage(Constants.Color.TRAITOR + "- " + traitor.getName());
                        }
                    }
                } else {
                    TTTCore.locale.getLocalizable("info.personal.status.role.traitor.alone")
                            .withPrefix(Constants.Color.TRAITOR).sendTo(pl);
                }
                TitleHelper.sendStatusTitle(pl, Constants.Role.TRAITOR);
            }

            if (TTTCore.config.KARMA_DAMAGE_REDUCTION) {
                KarmaHelper.applyDamageReduction(ch);
                double reduc = KarmaHelper.getDamageReduction(ch);
                String percentage = reduc < 1
                        ? (int) (reduc * 100) + "%"
                        : TTTCore.locale.getLocalizable("fragment.full")
                        .localizeFor(pl);
                TTTCore.locale.getLocalizable("info.personal.status.karma-damage")
                        .withPrefix(Constants.Color.INFO).withReplacements(KarmaHelper.getKarma(ch) + "", percentage)
                        .sendTo(pl);
            }
        }
        MiscHelper.broadcast(round, TTTCore.locale.getLocalizable("info.global.round.event.started")
                .withPrefix(Constants.Color.INFO));
    }

}
