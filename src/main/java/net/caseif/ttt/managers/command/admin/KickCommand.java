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
package net.caseif.ttt.managers.command.admin;

import static net.caseif.ttt.util.Constants.ERROR_COLOR;
import static net.caseif.ttt.util.Constants.INFO_COLOR;
import static net.caseif.ttt.util.MiscUtil.getMessage;

import net.caseif.ttt.Main;
import net.caseif.ttt.managers.command.SubcommandHandler;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.exception.NoSuchPlayerException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import org.bukkit.command.CommandSender;

public class KickCommand extends SubcommandHandler {

	public KickCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public void handle() {
		if (args.length > 1) {
			String name = args[1];
			MGPlayer mp = Main.mg.getMGPlayer(name);
			if (mp != null) {
				try {
					mp.removeFromRound();
					mp.getBukkitPlayer().sendMessage(getMessage("info.personal.kick", INFO_COLOR));
					sender.sendMessage(getMessage("info.global.round.event.kick", INFO_COLOR, mp.getBukkitPlayer().getName()));
				}
				catch (NoSuchPlayerException ex) {
					sender.sendMessage(getMessage("error.round.no-such-player", ERROR_COLOR)); // shouldn't ever happen
				}
				catch (PlayerOfflineException ex) {
					sender.sendMessage(getMessage("error.round.player-offline", ERROR_COLOR));
				}
			}
			else
				sender.sendMessage(getMessage("error.round.no-such-player", ERROR_COLOR));
		}
		else
			sender.sendMessage(getMessage("error.command.too-few-args", ERROR_COLOR));
	}
}
