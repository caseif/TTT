/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncacé <mproncace@lapis.blue>
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
package net.caseif.ttt.manager.command.admin;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.manager.command.SubcommandHandler;
import net.caseif.ttt.util.Constants.Color;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand extends SubcommandHandler {

    public KickCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.admin.kick");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            if (args.length > 1) {
                String name = args[1];
                @SuppressWarnings("deprecation")
                Player pl = Bukkit.getPlayer(name);
                if (pl == null) {
                    TTTCore.locale.getLocalizable("error.round.player-offline").withPrefix(Color.ERROR.toString())
                            .sendTo(sender);
                    return;
                }
                Optional<Challenger> ch = TTTCore.mg.getChallenger(pl.getUniqueId());
                if (ch.isPresent()) {
                    ch.get().removeFromRound();
                    TTTCore.locale.getLocalizable("info.personal.kick").withPrefix(Color.ERROR.toString()).sendTo(pl);
                    TTTCore.locale.getLocalizable("info.global.round.event.kick").withPrefix(Color.INFO.toString())
                            .withReplacements(ch.get().getName()).sendTo(sender);
                } else {
                    TTTCore.locale.getLocalizable("error.round.no-such-player").withPrefix(Color.ERROR.toString())
                            .sendTo(sender);
                }
            } else {
                TTTCore.locale.getLocalizable("error.command.too-few-args").withPrefix(Color.ERROR.toString())
                        .sendTo(sender);
                sendUsage();
            }
        }
    }
}
