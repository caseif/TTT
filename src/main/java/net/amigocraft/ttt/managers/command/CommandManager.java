/*
 * TTT
 * Copyright (c) 2014, Maxim Roncacé <http://bitbucket.org/mproncace>
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

import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.arena.*;
import net.amigocraft.ttt.managers.command.misc.DefaultCommand;
import net.amigocraft.ttt.managers.command.misc.HelpCommand;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (label.equalsIgnoreCase("ttt")){
			if (args.length > 0){
				if (args[0].equalsIgnoreCase("import") || args[0].equalsIgnoreCase("i")){
					new ImportCommand(sender, args).handle();
				}
				else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")){
					new JoinCommand(sender, args).handle();
				}
				else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("l") ||
						args[0].equalsIgnoreCase("quit") || args[0].equalsIgnoreCase("q")){
					new LeaveCommand(sender, args).handle();
				}
				else if (args[0].equalsIgnoreCase("carena") || args[0].equalsIgnoreCase("ca")){
					new CreateArenaCommand(sender, args).handle();
				}
				else if (args[0].equalsIgnoreCase("addspawn") || args[0].equalsIgnoreCase("as")){
					new AddSpawnCommand(sender, args).handle();
				}
				else if (args[0].equalsIgnoreCase("removespawn") || args[0].equalsIgnoreCase("rs")){
					new RemoveSpawnCommand(sender, args).handle();
				}
				else if (args[0].equalsIgnoreCase("setexit") || args[0].equalsIgnoreCase("se") ||
						args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("ss")){
					new SetExitCommand(sender, args).handle();
				}
				else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
					new HelpCommand(sender, args).handle();
				}
				else {
					sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-2"));
					sender.sendMessage(ChatColor.RED + Main.locale.getMessage("usage-1"));
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
