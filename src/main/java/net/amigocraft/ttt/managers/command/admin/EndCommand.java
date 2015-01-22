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
package net.amigocraft.ttt.managers.command.admin;

import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.api.Stage;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import org.bukkit.command.CommandSender;

import static net.amigocraft.ttt.util.Constants.ARENA_COLOR;
import static net.amigocraft.ttt.util.Constants.ERROR_COLOR;
import static net.amigocraft.ttt.util.MiscUtil.getMessage;

public class EndCommand extends SubcommandHandler {

	public EndCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public void handle() {
		if (args.length > 1) {
			String arena = args[1];
			Round r = Main.mg.getRound(arena);
			if (r.getStage() == Stage.PREPARING || r.getStage() == Stage.PLAYING) {
				if (args.length > 2) {
					if (args[2].equalsIgnoreCase("t"))
						r.setMetadata("t-victory", true);
					else if (args[2].equalsIgnoreCase("i"))
						r.setMetadata("t-victory", false);
					else {
						sender.sendMessage(getMessage("invalid-args-2", ERROR_COLOR));
						return;
					}
				}
				r.end();
			}
			else
				sender.sendMessage(getMessage("no-active-round", ERROR_COLOR, ARENA_COLOR + r.getArena()));
		}
		else
			sender.sendMessage(getMessage("invalid-args-1", ERROR_COLOR));
	}
}
