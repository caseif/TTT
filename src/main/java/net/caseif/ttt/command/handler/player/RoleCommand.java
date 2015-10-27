package net.caseif.ttt.command.handler.player;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.helper.gamemode.RoleHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RoleCommand extends CommandHandler {

    public RoleCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        String name = args[1];

        @SuppressWarnings("deprecation")
        Player pl = Bukkit.getPlayer(name);
        if (pl == null) {
            TTTCore.locale.getLocalizable("error.round.player-offline").withPrefix(Color.ERROR)
                    .sendTo(sender);
            return;
        }

        Optional<Challenger> ch = TTTCore.mg.getChallenger(pl.getUniqueId());
        if (!ch.isPresent()) {
            TTTCore.locale.getLocalizable("error.round.no-such-player").withPrefix(Color.ERROR)
                    .sendTo(sender);
            return;
        }

        String title = TTTCore.locale.getLocalizable("fragment-in-round").withPrefix(Color.INFO)
                .withReplacements(Color.DESCRIPTION + pl.getName() + Color.INFO,
                        Color.ARENA + ch.get().getRound().getArena().getName() + Color.INFO).localizeFor(sender);

        sender.sendMessage(title + " " + RoleHelper.genRoleMessage(sender, ch.get()));
    }

}
