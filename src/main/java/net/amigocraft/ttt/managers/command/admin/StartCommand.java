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

import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.api.Stage;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import net.amigocraft.ttt.util.NumUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class StartCommand extends SubcommandHandler {

	public StartCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (args.length > 1){
			String arena = args[1];
			Round r = Main.mg.getRound(arena);
			if (r != null){
				if (args.length > 2 && NumUtil.isInt(args[2]))
					r.setPlayingTime(Integer.parseInt(args[2]));
				r.setStage(Stage.PREPARING); // force player reset
				r.setTime(r.getPreparationTime() + 1); // this is a weird way of doing things but it should only stay preparing for less than 1 tick
				sender.sendMessage(ChatColor.GREEN + "[TTT] Set stage in arena " + ChatColor.ITALIC + r.getArena() + ChatColor.GREEN + " to playing");
			}
			else
				sender.sendMessage(ChatColor.RED + "[TTT] Cannot find a round in arena " + ChatColor.ITALIC + arena + ChatColor.RED + "!");
		}
		else
			sender.sendMessage(ChatColor.RED + "[TTT]" + Main.locale.getMessage("invalid-args-1"));
	}
}
