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
import static net.caseif.ttt.util.MiscUtil.getMessage;

import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.MiscUtil;
import net.caseif.ttt.util.NumUtil;

import net.amigocraft.mglib.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

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
                        sender.sendMessage(getMessage("error.admin.ban.invalid-time", ERROR_COLOR));
                        return;
                    }
                }
                try {
                    UUID uuid = UUIDFetcher.getUUIDOf(name);
                    if (uuid == null) {
                        sender.sendMessage(getMessage("error.plugin.uuid", ERROR_COLOR));
                        return;
                    }
                    if (MiscUtil.ban(uuid, time)) {
                        Bukkit.getPlayer(name).sendMessage(
                                time == -1
                                        ? getMessage("info.personal.ban.perm", ERROR_COLOR)
                                        : getMessage("info.personal.ban.temp", ERROR_COLOR,
                                                time + getMessage("fragment.minutes", ERROR_COLOR, false))
                        );
                        sender.sendMessage(
                                time == -1
                                        ? getMessage("info.personal.ban.other.perm", INFO_COLOR, name)
                                        : getMessage("info.personal.ban.other.temp", INFO_COLOR, name,
                                                time + getMessage("fragment.minutes", INFO_COLOR, false))
                        );
                    } else {
                        sender.sendMessage(getMessage("error.plugin.ban", ERROR_COLOR));
                    }
                } catch (Exception ex) {
                    sender.sendMessage(getMessage("error.plugin.generic", ERROR_COLOR));
                    ex.printStackTrace();
                }
            } else {
                sender.sendMessage(getMessage("error.command.too-few-args", ERROR_COLOR));
                sendUsage();
            }
        }
    }


}
