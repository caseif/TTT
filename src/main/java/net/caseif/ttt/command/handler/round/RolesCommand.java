package net.caseif.ttt.command.handler.round;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.helper.gamemode.RoleHelper;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.challenger.Challenger;
import org.bukkit.command.CommandSender;

public class RolesCommand extends CommandHandler{

    public RolesCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        String arenaName = args[1];
        Optional<Arena> arena = TTTCore.mg.getArena(args[1]);

        if (!arena.isPresent()) {
            TTTCore.locale.getLocalizable("error.round.dne").withPrefix(Color.ERROR)
                    .withReplacements(Color.ARENA + arenaName + Color.ERROR).sendTo(sender);
            return;
        }

        if (!arena.get().getRound().isPresent()) {
            TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ERROR)
                    .withReplacements(Color.ARENA + arenaName + Color.ERROR).sendTo(sender);
            return;
        }

        TTTCore.locale.getLocalizable("fragment.in-round-all").withPrefix(Color.INFO)
                .withReplacements(arena.get().getName()).sendTo(sender);
        for (Challenger ch : arena.get().getRound().get().getChallengers()) {
            sender.sendMessage(Color.DESCRIPTION + ch.getName() + ": " + RoleHelper.genRoleMessage(sender, ch));
        }
    }

}
