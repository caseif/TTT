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

import static net.caseif.ttt.util.Constants.Color;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.managers.command.CommandManager;
import net.caseif.ttt.managers.command.SubcommandHandler;

import net.caseif.rosetta.Localizable;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;

public class HelpCommand extends SubcommandHandler {

    public static final LinkedHashMap<String, Object[]> commands = new LinkedHashMap<>();

    static {
        registerCommand("join", TTTCore.locale.getLocalizable("info.help.arena.join"), "ttt.arena.join");
        registerCommand("leave", TTTCore.locale.getLocalizable("info.help.arena.leave"), "ttt.arena.leave");
        registerCommand("carena", TTTCore.locale.getLocalizable("info.help.arena.create"), "ttt.arena.create");
        registerCommand("rarena", TTTCore.locale.getLocalizable("info.help.arena.remove"), "ttt.arena.remove");
        registerCommand("import", TTTCore.locale.getLocalizable("info.help.arena.import"), "ttt.arena.import");
        registerCommand("addspawn", TTTCore.locale.getLocalizable("info.help.arena.addspawn"), "ttt.arena.addspawn");
        registerCommand("removespawn", TTTCore.locale.getLocalizable("info.help.arena.removespawn"),
                "ttt.arena.removespawn");

        registerCommand("prepare", TTTCore.locale.getLocalizable("info.help.admin.prepare"), "ttt.admin.prepare");
        registerCommand("start", TTTCore.locale.getLocalizable("info.help.admin.start"), "ttt.admin.start");
        registerCommand("end", TTTCore.locale.getLocalizable("info.help.admin.end"), "ttt.admin.end");
        registerCommand("kick", TTTCore.locale.getLocalizable("info.help.admin.kick"), "ttt.admin.kick");
        registerCommand("ban", TTTCore.locale.getLocalizable("info.help.admin.ban"), "ttt.admin.ban");
        registerCommand("pardon", TTTCore.locale.getLocalizable("info.help.admin.pardon"), "ttt.admin.pardon");
        //registerCommand("slay", Main.locale.getLocalizable("info.help.admin.slay"), "ttt.admin.slay");
        //registerCommand("respawn", Main.locale.getLocalizable("info.help.admin.respawn"), "ttt.admin.respawn");

        registerCommand("setexit", TTTCore.locale.getLocalizable("info.help.setexit"), "ttt.setexit");
        registerCommand("help", TTTCore.locale.getLocalizable("info.help.help"), "ttt.help");
    }

    public HelpCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.help");
    }

    private static void registerCommand(String cmd, Localizable description, String permission) {
        commands.put(cmd, new Object[]{description, permission});
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle() {
        if (assertPermission()) {
            if (args.length > 1) {
                String cmd = args[1];
                Object[] info = commands.get(cmd);
                if (info != null) {
                    if (sender.hasPermission((String) info[1])) {
                        sender.sendMessage(Color.INFO + "/ttt " + cmd + " "
                                + Color.DESCRIPTION + ((Localizable) info[0]).localizeFor(sender));
                        TTTCore.locale.getLocalizable("fragment.usage").withPrefix(Color.INFO.toString())
                                .withReplacements(CommandManager.getUsage(cmd)).sendTo(sender);
                    } else {
                        TTTCore.locale.getLocalizable("error.perms.generic").withPrefix(Color.ERROR.toString())
                                .sendTo(sender);
                    }
                } else {
                    TTTCore.locale.getLocalizable("error.command.invalid-args").withPrefix(Color.ERROR.toString())
                            .sendTo(sender);
                }
            } else {
                TTTCore.locale.getLocalizable("info.help.available-cmds").withPrefix(Color.SPECIAL.toString())
                        .sendTo(sender);
                sender.sendMessage("");
                for (String cmd : commands.keySet()) {
                    Object[] info = commands.get(cmd);
                    if (sender.hasPermission((String) info[1])) {
                        sender.sendMessage(Color.INFO + "/ttt " + cmd + " "
                                + Color.DESCRIPTION + ((Localizable) info[0]).localizeFor(sender));
                        sender.sendMessage(Color.INFO + "    " + TTTCore.locale.getLocalizable("fragment.usage")
                                + " " + Color.USAGE + CommandManager.getUsage(cmd));
                    }
                }
            }
        }
    }
}
