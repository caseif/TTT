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
package net.amigocraft.ttt.managers.command.admin;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.exception.NoSuchPlayerException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class KickCommand extends SubcommandHandler {

	public KickCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (args.length > 1){
			String name = args[1];
			MGPlayer mp = Main.mg.getMGPlayer(name);
			if (mp != null){
				try {
					mp.removeFromRound();
					mp.getBukkitPlayer().sendMessage(ChatColor.DARK_PURPLE + "[TTT] You have been kicked from your current round!");
				}
				catch (NoSuchPlayerException ex){
					sender.sendMessage(ChatColor.RED + "[TTT] The specified player is not in a round!"); // shouldn't ever happen
				}
				catch (PlayerOfflineException ex){
					sender.sendMessage(ChatColor.RED + "[TTT] The specified player is not online!");
				}
			}
			else
				sender.sendMessage(ChatColor.RED + "[TTT] The specified player is not in a round!");
		}
		else
			sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-1"));
	}
}
