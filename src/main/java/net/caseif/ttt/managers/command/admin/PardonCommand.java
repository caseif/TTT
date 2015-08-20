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

import static net.caseif.ttt.util.Constants.ERROR_COLOR;
import static net.caseif.ttt.util.Constants.INFO_COLOR;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.MiscUtil;
import net.caseif.ttt.util.UUIDFetcher;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class PardonCommand extends SubcommandHandler {

    public PardonCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.admin.ban");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            if (args.length > 1) {
                String name = args[1];
                try {
                    UUID uuid = UUIDFetcher.getUUIDOf(name);
                    if (uuid == null) {
                        TTTCore.locale.getLocalizable("error.plugin.uuid").withPrefix(ERROR_COLOR.toString())
                                .sendTo(sender);
                        return;
                    }
                    if (MiscUtil.pardon(uuid)) {
                        TTTCore.locale.getLocalizable("info.personal.pardon").withPrefix(INFO_COLOR.toString())
                        .sendTo(Bukkit.getPlayer(uuid));
                        TTTCore.locale.getLocalizable("info.personal.pardon.other")
                                .withPrefix(INFO_COLOR.toString()).withReplacements(name).sendTo(sender);
                    } else {
                        TTTCore.locale.getLocalizable("error.plugin.pardon").withPrefix(ERROR_COLOR.toString())
                                .sendTo(sender);
                    }
                } catch (Exception ex) {
                    TTTCore.locale.getLocalizable("error.plugin.generic").withPrefix(ERROR_COLOR.toString())
                            .sendTo(sender);
                    ex.printStackTrace();
                }
            } else {
                TTTCore.locale.getLocalizable("error.command.too-few-args").withPrefix(ERROR_COLOR.toString())
                        .sendTo(sender);
                sendUsage();
            }
        }
    }


}
