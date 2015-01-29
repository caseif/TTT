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
package net.amigocraft.ttt.managers.command;

import net.amigocraft.ttt.managers.command.admin.EndCommand;
import net.amigocraft.ttt.managers.command.admin.KickCommand;
import net.amigocraft.ttt.managers.command.admin.PrepareCommand;
import net.amigocraft.ttt.managers.command.admin.StartCommand;
import net.amigocraft.ttt.managers.command.arena.*;
import net.amigocraft.ttt.managers.command.misc.DefaultCommand;
import net.amigocraft.ttt.managers.command.misc.HelpCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static net.amigocraft.ttt.util.Constants.ERROR_COLOR;
import static net.amigocraft.ttt.util.MiscUtil.getMessage;

public class CommandManager implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("ttt")) {
			if (args.length > 0) {
				final String subCmd = args[0];
				// arena commands
				if (subCmd.equalsIgnoreCase("import") || subCmd.equalsIgnoreCase("i")) {
					new ImportCommand(sender, args).handle();
				}
				else if (subCmd.equalsIgnoreCase("join") || subCmd.equalsIgnoreCase("j")) {
					new JoinCommand(sender, args).handle();
				}
				else if (subCmd.equalsIgnoreCase("leave") || subCmd.equalsIgnoreCase("l") ||
						subCmd.equalsIgnoreCase("quit") || subCmd.equalsIgnoreCase("q")) {
					new LeaveCommand(sender, args).handle();
				}
				else if (subCmd.equalsIgnoreCase("carena") || subCmd.equalsIgnoreCase("ca")) {
					new CreateArenaCommand(sender, args).handle();
				}
				else if (subCmd.equalsIgnoreCase("addspawn") || subCmd.equalsIgnoreCase("as")) {
					new AddSpawnCommand(sender, args).handle();
				}
				else if (subCmd.equalsIgnoreCase("removespawn") || subCmd.equalsIgnoreCase("rs")) {
					new RemoveSpawnCommand(sender, args).handle();
				}
				// administrative commands
				else if (subCmd.equalsIgnoreCase("prepare")) {
					new PrepareCommand(sender, args);
				}
				else if (subCmd.equalsIgnoreCase("start")) {
					new StartCommand(sender, args);
				}
				else if (subCmd.equalsIgnoreCase("end")) {
					new EndCommand(sender, args);
				}
				else if (subCmd.equalsIgnoreCase("info.personal.kick")) {
					new KickCommand(sender, args);
				}
				// misc. commands
				else if (subCmd.equalsIgnoreCase("setexit") || subCmd.equalsIgnoreCase("se") ||
						subCmd.equalsIgnoreCase("setspawn") || subCmd.equalsIgnoreCase("ss")) {
					new SetExitCommand(sender, args).handle();
				}
				else if (subCmd.equalsIgnoreCase("help") || subCmd.equalsIgnoreCase("?")) {
					new HelpCommand(sender, args).handle();
				}
				else {
					sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
					sender.sendMessage(getMessage("info.command.usage.generic", ERROR_COLOR));
				}
			}
			else {
				new DefaultCommand(sender, args).handle();
			}
			return true;
		}
		return false;
	}

}
