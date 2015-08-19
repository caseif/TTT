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

import static net.caseif.ttt.util.Constants.ARENA_COLOR;
import static net.caseif.ttt.util.Constants.ERROR_COLOR;
import static net.caseif.ttt.util.Constants.INFO_COLOR;
import static net.caseif.ttt.util.MiscUtil.getMessage;

import net.caseif.ttt.Main;
import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.NumUtil;

import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.api.Stage;
import org.bukkit.command.CommandSender;

public class PrepareCommand extends SubcommandHandler {

    public PrepareCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.adnin.prepare");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            if (args.length > 1) {
                String arena = args[1];
                Round r = Main.mg.getRound(arena);
                if (r != null) {
                    if (r.getPlayerCount() > 1) {
                        if (args.length > 2 && NumUtil.isInt(args[2])) {
                            r.setPreparationTime(Integer.parseInt(args[2]));
                        }
                        r.setStage(Stage.WAITING);
                        r.start();
                        sender.sendMessage(getMessage("info.personal.arena.set-stage.preparing.success",
                                INFO_COLOR, ARENA_COLOR + r.getArena()));
                    } else {
                        sender.sendMessage(getMessage("error.arena.too-few-players", ERROR_COLOR));
                    }
                } else {
                    sender.sendMessage(getMessage("error.round.dne", ERROR_COLOR, ARENA_COLOR + arena));
                }
            } else {
                sender.sendMessage(getMessage("error.command.too-few-args", ERROR_COLOR));
                sendUsage();
            }
        }
    }
}
