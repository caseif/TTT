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
package net.caseif.ttt.managers.command.arena;

import static net.caseif.ttt.util.Constants.ERROR_COLOR;
import static net.caseif.ttt.util.Constants.INFO_COLOR;
import static net.caseif.ttt.util.MiscUtil.getMessage;

import net.caseif.ttt.Main;
import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.FileUtil;
import net.caseif.ttt.util.NumUtil;

import net.amigocraft.mglib.exception.ArenaExistsException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateArenaCommand extends SubcommandHandler {

    public CreateArenaCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.arena.create");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            String w;
            int x;
            int y;
            int z;
            if (args.length == 2) { // use sender's location
                if (sender instanceof Player) {
                    w = ((Player) sender).getWorld().getName();
                    x = ((Player) sender).getLocation().getBlockX();
                    y = ((Player) sender).getLocation().getBlockY();
                    z = ((Player) sender).getLocation().getBlockZ();
                } else {
                    sender.sendMessage(getMessage("error.command.ingame", ERROR_COLOR));
                    return;
                }
            } else if (args.length == 6) { // use 3 provided coords and world
                if (NumUtil.isInt(args[2]) && NumUtil.isInt(args[3]) && NumUtil.isInt(args[4])
                        && FileUtil.isWorld(args[5])) {
                    x = Integer.parseInt(args[2]);
                    y = Integer.parseInt(args[3]);
                    z = Integer.parseInt(args[4]);
                    w = args[5];
                } else {
                    sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
                    sendUsage();
                    return;
                }
            } else {
                sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
                sendUsage();
                return;
            }
            try {
                Main.mg.createArena(args[1], new Location(Bukkit.createWorld(new WorldCreator(w)), x, y, z));
                sender.sendMessage(getMessage("info.personal.arena.create.success", INFO_COLOR, args[1]));
            } catch (ArenaExistsException ex) {
                sender.sendMessage(getMessage("error.arena.already-exists", ERROR_COLOR));
            }
        }
    }
}
