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

package net.caseif.ttt.command.handler.player;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.scoreboard.ScoreboardManager;
import net.caseif.ttt.util.Body;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;
import net.caseif.ttt.util.helper.gamemode.RoundHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.metadata.Metadata;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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

        Location loc = LocationHelper.convert(ch.get().getMetadata().<Body>get(MetadataTag.BODY).get().getLocation());
        loc.getBlock().setType(Material.AIR);
        pl.teleport(loc);

        Metadata meta = ch.get().getMetadata();
        Body body = meta.<Body>get(MetadataTag.BODY).orNull();
        meta.remove(MetadataTag.BODY);
        meta.remove(MetadataTag.BODY_FOUND);
        if (body != null) {
            List<Body> bodies = ch.get().getRound().getMetadata().<List<Body>>get(MetadataTag.BODY_LIST).get();
            bodies.remove(body);
            ch.get().getRound().getMetadata().set(MetadataTag.BODY_LIST, bodies);
        }

        ch.get().setSpectating(false);

        RoundHelper.distributeItems(ch.get());

        pl.setHealth(pl.getMaxHealth());
        pl.setFoodLevel(20);

        ch.get().getRound().getMetadata().<ScoreboardManager>get(MetadataTag.SCOREBOARD_MANAGER).get()
                .updateEntry(ch.get());

        TTTCore.locale.getLocalizable("info.personal.respawn").withPrefix(Color.INFO).sendTo(pl);
        TTTCore.locale.getLocalizable("info.personal.respawn.other").withPrefix(Color.INFO)
                .withReplacements(ch.get().getName()).sendTo(sender);
    }

}
