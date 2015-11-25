/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncace <mproncace@lapis.blue>
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
package net.caseif.ttt.command.handler.misc;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.CommandManager;
import net.caseif.ttt.command.CommandRef;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.Constants.Color;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand extends CommandHandler {

    public HelpCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle() {
        if (args.length > 1) {
            if (!CommandManager.commands.containsKey(args[1])) {
                printInvalidArgsError();
                return;
            }

            CommandRef cmdRef = CommandManager.commands.get(args[1]);

            if (sender.hasPermission(cmdRef.getPermission())) {
                sendDescription(cmdRef);
            } else {
                TTTCore.locale.getLocalizable("error.perms.generic").withPrefix(Color.ERROR).sendTo(sender);
            }
        } else {
            sender.sendMessage("");
            TTTCore.locale.getLocalizable("info.help.available-cmds").withPrefix(Color.SPECIAL).sendTo(sender);

            for (String cmd : CommandManager.commands.keySet()) {
                CommandRef cmdRef = CommandManager.commands.get(cmd);

                if (cmdRef.getPermission() == null || sender.hasPermission(cmdRef.getPermission())) {
                    sendDescription(cmdRef);
                }
            }
        }
    }

    private void sendDescription(CommandRef cmdRef) {
        assert cmdRef != null;

        sender.sendMessage(
                Color.LABEL + "/ttt " + cmdRef.getLabel() + " "
                        + Color.INFO + cmdRef.getDescription().localizeFor(sender)
        );

        TTTCore.locale.getLocalizable("fragment.usage")
                .withPrefix("" + Color.FLAIR + ChatColor.ITALIC + "    ")
                .withReplacements("" + Color.SPECIAL + ChatColor.ITALIC + cmdRef.getUsage())
                .sendTo(sender);
    }

}
