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
package net.amigocraft.ttt.managers.command.misc;

import static net.amigocraft.ttt.util.Constants.*;
import static net.amigocraft.ttt.util.MiscUtil.getMessage;

import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;

import org.bukkit.command.CommandSender;

public class HelpCommand extends SubcommandHandler {

	public HelpCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public void handle() {
		if (args.length > 1 && args[1].equalsIgnoreCase("lobby")) {
			if (sender.hasPermission("ttt.lobby.create")) {
				sender.sendMessage(getMessage("info.help.lobby", SPECIAL_COLOR));
				sender.sendMessage("");
				sender.sendMessage(getMessage("info.help.lobby.line.1", INFO_COLOR) + " " +
						getMessage("info.help.lobby.line.label", INFO_COLOR) + " " +
						DESCRIPTION_COLOR + "[TTT]");
				sender.sendMessage(getMessage("info.help.lobby.line.2", INFO_COLOR) + " " +
						getMessage("info.help.lobby.line.label", INFO_COLOR) + " " +
						getMessage("info.help.lobby.line.2.content", DESCRIPTION_COLOR));
				sender.sendMessage(getMessage("info.help.lobby.line.3", INFO_COLOR) + " " +
						getMessage("info.help.lobby.line.label", INFO_COLOR) + " " +
						getMessage("info.help.lobby.line.3.content", DESCRIPTION_COLOR));
				sender.sendMessage(getMessage("info.help.lobby.line.4", INFO_COLOR) + " " +
						getMessage("info.help.lobby.line.label", INFO_COLOR) + " " +
						getMessage("info.help.lobby.line.4.content", DESCRIPTION_COLOR));
			}
			else {
				sender.sendMessage(Main.locale.getMessage("error.perms.generic"));
			}
		}
		else if (sender.hasPermission("ttt.help")) {
			sender.sendMessage(getMessage("info.help.available-cmds", SPECIAL_COLOR));
			sender.sendMessage("");
			if (sender.hasPermission("ttt.arena.join")) {
				sender.sendMessage(INFO_COLOR + "/ttt join, j " +
						getMessage("info.help.arena.join", DESCRIPTION_COLOR));
			}
			if (sender.hasPermission("ttt.arena.quit")) {
				sender.sendMessage(INFO_COLOR + "/ttt quit, q " +
						getMessage("info.help.arena.leave", DESCRIPTION_COLOR));
			}
			if (sender.hasPermission("ttt.arena.import")) {
				sender.sendMessage(INFO_COLOR + "/ttt import, i " +
						getMessage("info.help.arena.import", DESCRIPTION_COLOR));
			}
			if (sender.hasPermission("ttt.arena.create")) {
				sender.sendMessage(INFO_COLOR + "/ttt carena, ca " +
						getMessage("info.help.arena.create", DESCRIPTION_COLOR));
			}
			if (sender.hasPermission("ttt.arena.addspawn")) {
				sender.sendMessage(INFO_COLOR + "/ttt addspawn, ad " +
						getMessage("info.help.arena.spawn.add", DESCRIPTION_COLOR));
			}
			if (sender.hasPermission("ttt.arena.removespawn")) {
				sender.sendMessage(INFO_COLOR + "/ttt removespawn, rs " +
						getMessage("info.help.arena.spawn.remove", DESCRIPTION_COLOR));
			}
			if (sender.hasPermission("ttt.setspawn")) {
				sender.sendMessage(INFO_COLOR + "/ttt setexit, se " +
						getMessage("info.help.set-exit", DESCRIPTION_COLOR));
			}
			if (sender.hasPermission("ttt.help")) {
				sender.sendMessage(INFO_COLOR + "/ttt help, ? " +
						getMessage("info.help.help", DESCRIPTION_COLOR));
			}
		}
		else {
			sender.sendMessage(getMessage("error.perms.generic", ERROR_COLOR));
		}
	}
}
