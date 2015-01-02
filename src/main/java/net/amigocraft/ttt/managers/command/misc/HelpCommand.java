/*
 * TTT
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

import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand extends SubcommandHandler {

	public HelpCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (args.length > 1 && args[1].equalsIgnoreCase("lobby")){
			if (sender.hasPermission("ttt.lobby.create")){
				sender.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE +
						Main.locale.getMessage("lobby-help"));
				sender.sendMessage("");
				sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("first") + " " +
						Main.locale.getMessage("line") + " " +
						ChatColor.GREEN + "[TTT]");
				sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("second") + " " +
						Main.locale.getMessage("line") + " " +
						ChatColor.GREEN + Main.locale.getMessage("type"));
				sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("third") + " " +
						Main.locale.getMessage("line") + " " +
						ChatColor.GREEN + Main.locale.getMessage("round"));
				sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("fourth") + " " +
						Main.locale.getMessage("line") + " " +
						ChatColor.GREEN + Main.locale.getMessage("number"));
			}
			else {
				sender.sendMessage(Main.locale.getMessage("no-permission"));
			}
		}
		else if (sender.hasPermission("ttt.help")){
			sender.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE +
					Main.locale.getMessage("commands"));
			sender.sendMessage("");
			if (sender.hasPermission("ttt.arena.join")){
				sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt join, j " + ChatColor.GREEN +
						Main.locale.getMessage("join-help"));
			}
			if (sender.hasPermission("ttt.arena.quit")){
				sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt quit, q " + ChatColor.GREEN +
						Main.locale.getMessage("quit-help"));
			}
			if (sender.hasPermission("ttt.arena.import")){
				sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt import, i " + ChatColor.GREEN +
						Main.locale.getMessage("import-help"));
			}
			if (sender.hasPermission("ttt.arena.create")){
				sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt carena, ca " + ChatColor.GREEN +
						Main.locale.getMessage("createarena-help"));
			}
			if (sender.hasPermission("ttt.arena.addspawn")){
				sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt addspawn, ad " + ChatColor.GREEN +
						Main.locale.getMessage("addspawn-help"));
			}
			if (sender.hasPermission("ttt.arena.removespawn")){
				sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt removespawn, rs " + ChatColor.GREEN +
						Main.locale.getMessage("removespawn-help"));
			}
			if (sender.hasPermission("ttt.setspawn")){
				sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt setexit, se " + ChatColor.GREEN +
						Main.locale.getMessage("spawn-help"));
			}
			if (sender.hasPermission("ttt.help")){
				sender.sendMessage(ChatColor.DARK_PURPLE + "/ttt help, ? " + ChatColor.GREEN +
						Main.locale.getMessage("help-help"));
			}
		}
		else {
			sender.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("no-permission"));
		}
	}
}
