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

import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.MiscUtil;

import java.util.UUID;

import net.amigocraft.mglib.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class PardonCommand extends SubcommandHandler {

	public PardonCommand(CommandSender sender, String[] args) {
		super(sender, args, "ttt.admin.ban");
	}

	@Override
	public void handle() {
		if (assertPermission()) {
			if (args.length > 1) {
				String name = args[1];
				try {
					UUID uuid = UUIDFetcher.getUUIDOf(name);
					if (uuid == null) {
						sender.sendMessage(getMessage("error.plugin.uuid", ERROR_COLOR));
						return;
					}
					if (MiscUtil.pardon(uuid)) {
						Bukkit.getPlayer(name).sendMessage(getMessage("info.personal.pardon", ERROR_COLOR));
						sender.sendMessage(getMessage("info.personal.pardon.other", INFO_COLOR, name));
					}
					else {
						sender.sendMessage(getMessage("error.plugin.pardon", ERROR_COLOR));
					}
				}
				catch (Exception ex) {
					sender.sendMessage(getMessage("error.plugin.generic", ERROR_COLOR));
					ex.printStackTrace();
				}
			}
			else {
				sender.sendMessage(getMessage("error.command.too-few-args", ERROR_COLOR));
				sendUsage();
			}
		}
	}


}
