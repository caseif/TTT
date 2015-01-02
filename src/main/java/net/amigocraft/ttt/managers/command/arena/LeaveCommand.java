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
package net.amigocraft.ttt.managers.command.arena;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.exception.NoSuchPlayerException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand extends SubcommandHandler {

	public LeaveCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (sender instanceof Player){
			if (sender.hasPermission("ttt.arena.leave")){
				if (Main.mg.isPlayer(sender.getName())){
					MGPlayer mp = Main.mg.getMGPlayer(sender.getName());
					//String arena = mp.getArena();
					try {
						mp.removeFromRound();
					}
					catch (NoSuchPlayerException ex){
						sender.sendMessage(Main.locale.getMessage("not-in-game"));
					}
					catch (PlayerOfflineException ex){
						ex.printStackTrace();
					}
					//sender.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + sender.getName() + " " +
					//		Main.locale.getMessage("left-game").replace("%", arena));
				}
				else {
					sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("not-in-game"));
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "[TTT] " +
						Main.locale.getMessage("no-permission-quit"));
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("must-be-ingame"));
		}
	}
}
