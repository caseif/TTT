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

import static net.amigocraft.ttt.util.Constants.*;
import static net.amigocraft.ttt.util.MiscUtil.*;

import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.exception.NoSuchArenaException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.mglib.exception.PlayerPresentException;
import net.amigocraft.mglib.exception.RoundFullException;
import net.amigocraft.mglib.misc.JoinResult;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand extends SubcommandHandler {

	public JoinCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (sender instanceof Player){
			if (sender.hasPermission("ttt.arena.join")){
				if (args.length > 1){
					Round r;
					try {
						r = Main.mg.getRound(args[1]);
						if (r == null){
							r = Main.mg.createRound(args[1]);
						}
						JoinResult result = r.addPlayer(sender.getName());
					}
					catch (NoSuchArenaException ex){
						sender.sendMessage(getMessage("arena-invalid", ERROR_COLOR));
					}
					catch (PlayerOfflineException ex){ // this should never be able to happen
						ex.printStackTrace();
					}
					catch (PlayerPresentException ex){
						sender.sendMessage(getMessage("already-entered", ERROR_COLOR));
					}
					catch (RoundFullException ex){
						sender.sendMessage(getMessage("round-full", ERROR_COLOR));
					}
				}
				else {
					sender.sendMessage(getMessage("invalid-args-1", ERROR_COLOR));
					sender.sendMessage(getMessage("usage-join", ERROR_COLOR));
				}
			}
			else {
				sender.sendMessage(getMessage("no-permission-join", ERROR_COLOR));
			}
		}
		else {
			sender.sendMessage(getMessage("must-be-ingame", ERROR_COLOR));
		}
	}
}
