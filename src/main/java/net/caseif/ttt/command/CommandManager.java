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
package net.caseif.ttt.command;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.admin.BanCommand;
import net.caseif.ttt.command.admin.EndCommand;
import net.caseif.ttt.command.admin.KickCommand;
import net.caseif.ttt.command.admin.PardonCommand;
import net.caseif.ttt.command.admin.PrepareCommand;
import net.caseif.ttt.command.admin.StartCommand;
import net.caseif.ttt.command.arena.AddSpawnCommand;
import net.caseif.ttt.command.arena.CreateArenaCommand;
import net.caseif.ttt.command.arena.ImportCommand;
import net.caseif.ttt.command.arena.JoinCommand;
import net.caseif.ttt.command.arena.LeaveCommand;
import net.caseif.ttt.command.arena.RemoveArenaCommand;
import net.caseif.ttt.command.arena.RemoveSpawnCommand;
import net.caseif.ttt.command.misc.DefaultCommand;
import net.caseif.ttt.command.misc.HelpCommand;
import net.caseif.ttt.util.Constants.Color;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class CommandManager implements CommandExecutor {

    /**
     * Retrieves the usage for the given subcommand from the plugin.yml file.
     *
     * @return the usage for the given subcommand, or null if not specified
     */
    public static String getUsage(String subcommand) {
        Object map = TTTCore.getInstance().getDescription().getCommands()
                .get("ttt").get(subcommand);
        if (map instanceof Map) {
            return ((Map) map).get("usage").toString();
        }
        return null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("ttt")) {
            if (args.length > 0) {
                final String subCmd = args[0];
                // arena commands
                if (subCmd.equalsIgnoreCase("import") || subCmd.equalsIgnoreCase("i")) {
                    new ImportCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("join") || subCmd.equalsIgnoreCase("j")) {
                    new JoinCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("leave") || subCmd.equalsIgnoreCase("l")
                        || subCmd.equalsIgnoreCase("quit") || subCmd.equalsIgnoreCase("q")) {
                    new LeaveCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("carena") || subCmd.equalsIgnoreCase("ca")) {
                    new CreateArenaCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("rarena") || subCmd.equalsIgnoreCase("ra")) {
                    new RemoveArenaCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("addspawn") || subCmd.equalsIgnoreCase("as")) {
                    new AddSpawnCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("removespawn") || subCmd.equalsIgnoreCase("rs")) {
                    new RemoveSpawnCommand(sender, args).handle();
                    // administrative commands
                } else if (subCmd.equalsIgnoreCase("prepare")) {
                    new PrepareCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("start")) {
                    new StartCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("end")) {
                    new EndCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("kick")) {
                    new KickCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("ban")) {
                    new BanCommand(sender, args).handle();
                } else if (subCmd.equalsIgnoreCase("pardon")) {
                    new PardonCommand(sender, args).handle();
                    // misc. commands
                } else if (subCmd.equalsIgnoreCase("help") || subCmd.equalsIgnoreCase("?")) {
                    new HelpCommand(sender, args).handle();
                } else {
                    TTTCore.locale.getLocalizable("error.command.invalid-args")
                            .withPrefix(Color.ERROR).sendTo(sender);
                }
            } else {
                new DefaultCommand(sender, args).handle();
            }
            return true;
        }
        return false;
    }

}
