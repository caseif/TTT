package net.caseif.ttt.command.handler.player;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.helper.event.DeathHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SlayCommand extends CommandHandler {

    public SlayCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        String name = args[1];

        @SuppressWarnings("deprecation")
        Player pl = Bukkit.getPlayer(name);
        if (pl == null) {
            TTTCore.locale.getLocalizable("error.round.player-offline").withPrefix(Constants.Color.ERROR).sendTo(sender);
            return;
        }

        Optional<Challenger> ch = TTTCore.mg.getChallenger(pl.getUniqueId());
        if (ch.isPresent()) {
            new DeathHelper(pl).handleEvent();
            TTTCore.locale.getLocalizable("info.personal.slay").withPrefix(Constants.Color.ERROR).sendTo(pl);
            TTTCore.locale.getLocalizable("info.global.slay.other").withPrefix(Constants.Color.INFO)
                    .withReplacements(ch.get().getName()).sendTo(sender);
        } else {
            TTTCore.locale.getLocalizable("error.round.no-such-player").withPrefix(Constants.Color.ERROR).sendTo(sender);
        }
    }

}
