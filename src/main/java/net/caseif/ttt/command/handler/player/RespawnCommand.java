package net.caseif.ttt.command.handler.player;

import net.caseif.ttt.Body;
import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.helper.platform.LocationHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RespawnCommand extends CommandHandler {

    public RespawnCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        String name = args[1];

        @SuppressWarnings("deprecation")
        Player pl = Bukkit.getPlayer(name);
        if (pl == null) {
            TTTCore.locale.getLocalizable("error.round.player-offline").withPrefix(Color.ERROR).sendTo(sender);
            return;
        }

        Optional<Challenger> ch = TTTCore.mg.getChallenger(pl.getUniqueId());
        if (!ch.isPresent()) {
            TTTCore.locale.getLocalizable("error.round.no-such-player").withPrefix(Color.ERROR).sendTo(sender);
            return;
        }
        if (!ch.get().isSpectating()
                || ch.get().getMetadata().has(MetadataTag.PURE_SPECTATOR)
                || !ch.get().getMetadata().has(MetadataTag.BODY)) {
            TTTCore.locale.getLocalizable("error.round.not-dead").withPrefix(Color.ERROR).sendTo(sender);
            return;
        }

        ch.get().setSpectating(false);
        Location loc = LocationHelper.convert(ch.get().getMetadata().<Body>get(MetadataTag.BODY).get().getLocation());
        loc.getBlock().setType(Material.AIR);
        pl.teleport(loc);
        pl.setHealth(pl.getMaxHealth());
        pl.setFoodLevel(20);

        TTTCore.locale.getLocalizable("info.personal.respawn").withPrefix(Color.ERROR).sendTo(pl);
        TTTCore.locale.getLocalizable("info.personal.respawn.other").withPrefix(Color.INFO)
                .withReplacements(ch.get().getName()).sendTo(sender);
    }

}
