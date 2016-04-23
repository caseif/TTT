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

package net.caseif.ttt.command.handler.misc;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.CommandManager;
import net.caseif.ttt.command.CommandRef;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.constant.Color;
import net.caseif.ttt.util.helper.data.DataVerificationHelper;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HelpCommand extends CommandHandler {

    private static final int COMMANDS_PER_PAGE = 4;
    private static final String DIVIDER = "------------";

    public HelpCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle() {
        boolean isInt = false;
        if (args.length == 1 || (isInt = DataVerificationHelper.isInt(args[1]))) {
            int pageIndex = isInt ? Integer.parseInt(args[1]) : 1;

            List<CommandRef> availableCommands = getAvailableCommands();

            int pageCount = (int) Math.ceil((float) availableCommands.size() / COMMANDS_PER_PAGE);

            if (pageIndex > pageCount || pageIndex <= 0) {
                TTTCore.locale.getLocalizable("error.command.help.bad-page").withPrefix(Color.ERROR).sendTo(sender);
                return;
            }

            printHeader(pageIndex, pageCount);

            for (int i = 0; i < COMMANDS_PER_PAGE; i++) {
                int index = COMMANDS_PER_PAGE * (pageIndex - 1) + i;
                if (index >= availableCommands.size()) {
                    break;
                }

                printDescription(availableCommands.get(index));
            }

            printFooter(pageIndex, pageCount);
        } else { // assume they're querying a specific command
            if (!CommandManager.commands.containsKey(args[1])) {
                printInvalidArgsError();
                return;
            }

            CommandRef cmdRef = CommandManager.commands.get(args[1]);

            if (cmdRef.getPermission() == null || sender.hasPermission(cmdRef.getPermission())) {
                printDescription(cmdRef);
            } else {
                TTTCore.locale.getLocalizable("error.perms.generic").withPrefix(Color.ERROR).sendTo(sender);
            }
        }
    }

    private void printDescription(CommandRef cmdRef) {
        assert cmdRef != null;

        cmdRef.getDescription().withPrefix(Color.LABEL + "/ttt " + cmdRef.getLabel() + " " + Color.INFO).sendTo(sender);

        TTTCore.locale.getLocalizable("fragment.usage")
                .withPrefix("    " + Color.FLAIR + ChatColor.ITALIC)
                .withReplacements("" + Color.SPECIAL + ChatColor.ITALIC + cmdRef.getUsage())
                .sendTo(sender);
        if (cmdRef.getAliases().length > 0) {
            StringBuilder aliasStr = new StringBuilder();
            for (String alias : cmdRef.getAliases()) {
                aliasStr.append(alias).append(", ");
            }
            aliasStr.delete(aliasStr.length() - 2, aliasStr.length());
            TTTCore.locale.getLocalizable("fragment.aliases").withPrefix("    " + Color.FLAIR + ChatColor.ITALIC)
                    .withReplacements(Color.SPECIAL + ChatColor.ITALIC + aliasStr.toString()).sendTo(sender);
        }
    }

    private List<CommandRef> getAvailableCommands() {
        Set<CommandRef> cmds = new LinkedHashSet<>();
        for (CommandRef ref : CommandManager.commands.values()) {
            if (ref.getPermission() == null || sender.hasPermission(ref.getPermission())) {
                cmds.add(ref);
            }
        }
        return new ArrayList<>(cmds);
    }

    private void printHeader(int pageIndex, int pageCount) {
        sender.sendMessage("");
        TTTCore.locale.getLocalizable("info.help.available-cmds").withPrefix(Color.INFO).sendTo(sender);
        TTTCore.locale.getLocalizable("info.help.page").withPrefix(DIVIDER + " " + Color.FLAIR)
                .withSuffix(ChatColor.WHITE + " " + DIVIDER).withReplacements(pageIndex + " / " + pageCount)
                .sendTo(sender);
    }

    private void printFooter(int pageIndex, int pageCount) {
        if (pageIndex < pageCount) {
            TTTCore.locale.getLocalizable("info.help.next-page").withPrefix(Color.INFO)
                    .withReplacements(Color.FLAIR + "/ttt help " + (pageIndex + 1) + Color.INFO).sendTo(sender);
        }
    }


}
