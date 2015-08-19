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
package net.caseif.ttt.managers.command.misc;

import static net.caseif.ttt.util.Constants.DESCRIPTION_COLOR;
import static net.caseif.ttt.util.Constants.ERROR_COLOR;
import static net.caseif.ttt.util.Constants.INFO_COLOR;
import static net.caseif.ttt.util.Constants.SPECIAL_COLOR;
import static net.caseif.ttt.util.Constants.USAGE_COLOR;
import static net.caseif.ttt.util.MiscUtil.getMessage;

import net.caseif.ttt.Main;
import net.caseif.ttt.managers.command.CommandManager;
import net.caseif.ttt.managers.command.SubcommandHandler;

import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;

public class HelpCommand extends SubcommandHandler {

    public static final LinkedHashMap<String, String[]> commands = new LinkedHashMap<String, String[]>();

    static {
        registerCommand("join", Main.locale.getMessage("info.help.arena.join"), "ttt.arena.join");
        registerCommand("leave", Main.locale.getMessage("info.help.arena.leave"), "ttt.arena.leave");
        registerCommand("carena", Main.locale.getMessage("info.help.arena.create"), "ttt.arena.create");
        registerCommand("rarena", Main.locale.getMessage("info.help.arena.remove"), "ttt.arena.remove");
        registerCommand("import", Main.locale.getMessage("info.help.arena.import"), "ttt.arena.import");
        registerCommand("addspawn", Main.locale.getMessage("info.help.arena.addspawn"), "ttt.arena.addspawn");
        registerCommand("removespawn", Main.locale.getMessage("info.help.arena.removespawn"), "ttt.arena.removespawn");

        registerCommand("prepare", Main.locale.getMessage("info.help.admin.prepare"), "ttt.admin.prepare");
        registerCommand("start", Main.locale.getMessage("info.help.admin.start"), "ttt.admin.start");
        registerCommand("end", Main.locale.getMessage("info.help.admin.end"), "ttt.admin.end");
        registerCommand("kick", Main.locale.getMessage("info.help.admin.kick"), "ttt.admin.kick");
        registerCommand("ban", Main.locale.getMessage("info.help.admin.ban"), "ttt.admin.ban");
        registerCommand("pardon", Main.locale.getMessage("info.help.admin.pardon"), "ttt.admin.pardon");
        //registerCommand("slay", Main.locale.getMessage("info.help.admin.slay"), "ttt.admin.slay");
        //registerCommand("respawn", Main.locale.getMessage("info.help.admin.respawn"), "ttt.admin.respawn");

        registerCommand("setexit", Main.locale.getMessage("info.help.setexit"), "ttt.setexit");
        registerCommand("help", Main.locale.getMessage("info.help.help"), "ttt.help");
    }

    public HelpCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.help");
    }

    private static void registerCommand(String cmd, String description, String permission) {
        commands.put(cmd, new String[]{description, permission});
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle() {
        if (assertPermission()) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("lobby")) {
                    if (sender.hasPermission("ttt.lobby.create")) {
                        sender.sendMessage(getMessage("info.help.lobby", SPECIAL_COLOR, false));
                        sender.sendMessage("");
                        sender.sendMessage(getMessage("info.help.lobby.line.1", INFO_COLOR, false) + " "
                                + getMessage("info.help.lobby.line.label", INFO_COLOR, false) + " "
                                + DESCRIPTION_COLOR + "[TTT]");
                        sender.sendMessage(getMessage("info.help.lobby.line.2", INFO_COLOR, false) + " "
                                + getMessage("info.help.lobby.line.label", INFO_COLOR, false) + " "
                                + getMessage("info.help.lobby.line.2.content", DESCRIPTION_COLOR, false));
                        sender.sendMessage(getMessage("info.help.lobby.line.3", INFO_COLOR, false) + " "
                                + getMessage("info.help.lobby.line.label", INFO_COLOR, false) + " "
                                + getMessage("info.help.lobby.line.3.content", DESCRIPTION_COLOR, false));
                        sender.sendMessage(getMessage("info.help.lobby.line.4", INFO_COLOR, false) + " "
                                + getMessage("info.help.lobby.line.label", INFO_COLOR, false) + " "
                                + getMessage("info.help.lobby.line.4.content", DESCRIPTION_COLOR, false));
                    } else {
                        sender.sendMessage(Main.locale.getMessage("error.perms.generic"));
                    }
                } else {
                    String cmd = args[1];
                    String[] info = commands.get(cmd);
                    if (info != null) {
                        if (sender.hasPermission(info[1])) {
                            sender.sendMessage(INFO_COLOR + "/ttt " + cmd + " "
                                    + DESCRIPTION_COLOR + info[0]);
                            sender.sendMessage(INFO_COLOR + "    " + Main.locale.getMessage("fragment.usage") + " "
                                    + USAGE_COLOR + CommandManager.getUsage(cmd));
                        } else {
                            sender.sendMessage(getMessage("error.perms.generic", ERROR_COLOR));
                        }
                    } else {
                        sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
                    }
                }
            } else {
                sender.sendMessage(getMessage("info.help.available-cmds", SPECIAL_COLOR));
                sender.sendMessage("");
                for (String cmd : commands.keySet()) {
                    String[] info = commands.get(cmd);
                    if (sender.hasPermission(info[1])) {
                        sender.sendMessage(INFO_COLOR + "/ttt " + cmd + " "
                                + DESCRIPTION_COLOR + info[0]);
                        sender.sendMessage(INFO_COLOR + "    " + Main.locale.getMessage("fragment.usage") + " "
                                + USAGE_COLOR + CommandManager.getUsage(cmd));
                    }
                }
            }
        }
    }
}
