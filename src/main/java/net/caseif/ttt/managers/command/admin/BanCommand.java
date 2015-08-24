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
package net.caseif.ttt.managers.command.admin;

import static net.caseif.ttt.util.Constants.Color;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.MiscUtil;
import net.caseif.ttt.util.NumUtil;
import net.caseif.ttt.util.UUIDFetcher;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanCommand extends SubcommandHandler {

    public BanCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.admin.ban");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            if (args.length > 1) {
                String name = args[1];
                int time = -1;
                if (args.length > 2) {
                    if (NumUtil.isInt(args[2])) {
                        time = Integer.parseInt(args[2]);
                    } else {
                        TTTCore.locale.getLocalizable("error.admin.ban.invalid-time").withPrefix(Color.ERROR.toString())
                                .sendTo(sender);
                        return;
                    }
                }
                try {
                    UUID uuid = UUIDFetcher.getUUIDOf(name);
                    if (uuid == null) {
                        TTTCore.locale.getLocalizable("error.plugin.uuid").withPrefix(Color.ERROR.toString())
                                .sendTo(sender);
                        return;
                    }
                    if (MiscUtil.ban(uuid, time)) {
                        Player pl = Bukkit.getPlayer(uuid);
                        if (time == -1) {
                            TTTCore.locale.getLocalizable("info.personal.ban.perm")
                                    .withPrefix(Color.ERROR.toString()).sendTo(pl);
                            TTTCore.locale.getLocalizable("info.personal.ban.other.perm")
                                    .withPrefix(Color.ERROR.toString()).sendTo(pl);
                        } else {
                            TTTCore.locale.getLocalizable("info.personal.ban.temp").withPrefix(Color.ERROR.toString())
                                    .withReplacements(time + TTTCore.locale.getLocalizable("fragment.minutes")
                                            .localizeFor(pl))
                                    .sendTo(pl);
                            TTTCore.locale.getLocalizable("info.personal.ban.other.temp")
                                    .withPrefix(Color.ERROR.toString())
                                    .withReplacements(time + TTTCore.locale.getLocalizable("fragment.minutes")
                                            .localizeFor(sender))
                                    .sendTo(sender);
                        }
                    } else {
                        TTTCore.locale.getLocalizable("error.plugin.ban").withPrefix(Color.ERROR.toString())
                                .sendTo(sender);
                    }
                } catch (Exception ex) {
                    TTTCore.locale.getLocalizable("error.plugin.generic").withPrefix(Color.ERROR.toString())
                            .sendTo(sender);
                    ex.printStackTrace();
                }
            } else {
                TTTCore.locale.getLocalizable("error.command.too-few-args").withPrefix(Color.ERROR.toString())
                        .sendTo(sender);
                sendUsage();
            }
        }
    }


}
