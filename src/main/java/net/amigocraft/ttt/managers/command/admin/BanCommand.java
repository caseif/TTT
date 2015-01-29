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

import net.amigocraft.mglib.UUIDFetcher;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import net.amigocraft.ttt.util.MiscUtil;
import net.amigocraft.ttt.util.NumUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

import static net.amigocraft.ttt.util.Constants.ERROR_COLOR;
import static net.amigocraft.ttt.util.MiscUtil.getMessage;

public class BanCommand extends SubcommandHandler {

	public BanCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public void handle() {
		if (args.length > 1) {
			if (sender.hasPermission("ttt.admin.ban")) {
				String name = args[1];
				int time = -1;
				if (args.length > 2) {
					if (NumUtil.isInt(args[2])) {
						time = Integer.parseInt(args[2]);
					}
					else {
						sender.sendMessage(getMessage("error.admin.ban.invalid-time", ERROR_COLOR));
						return;
					}
				}
				try {
					UUID uuid = UUIDFetcher.getUUIDOf(name);
					if (uuid == null)
						throw new Exception();
					MiscUtil.ban(uuid, time);
					Bukkit.getPlayer(uuid).sendMessage(
							time == -1 ?
									getMessage("info.personal.ban.perm", ERROR_COLOR) :
									getMessage("info.personal.ban.temp", ERROR_COLOR, time + "")
					);
				}
				catch (Exception ex) {
					sender.sendMessage(getMessage("error.plugin.uuid", ERROR_COLOR));
				}
			}
		}
		else
			sender.sendMessage(getMessage("error.command.too-few-args", ERROR_COLOR));
	}
}
