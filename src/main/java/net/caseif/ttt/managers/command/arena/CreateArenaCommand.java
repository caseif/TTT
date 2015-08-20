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

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.FileUtil;
import net.caseif.ttt.util.NumUtil;

import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateArenaCommand extends SubcommandHandler {

    public CreateArenaCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.arena.create");
    }

    @Override
    public void handle() {
        //TODO: get corners (probably need a wizard for that :P)
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
                    TTTCore.locale.getLocalizable("error.command.ingame").withPrefix(ERROR_COLOR.toString())
                            .sendTo(sender);
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
                    TTTCore.locale.getLocalizable("error.command.invalid-args").withPrefix(ERROR_COLOR.toString())
                            .sendTo(sender);
                    sendUsage();
                    return;
                }
            } else {
                TTTCore.locale.getLocalizable("error.command.invalid-args").withPrefix(ERROR_COLOR.toString())
                        .sendTo(sender);
                sendUsage();
                return;
            }
            if (TTTCore.mg.getArena(args[1]).isPresent()) {
                TTTCore.locale.getLocalizable("error.arena.already-exists").withPrefix(ERROR_COLOR.toString())
                        .sendTo(sender);
            }
            TTTCore.mg.createArena(args[1], new Location3D(Bukkit.createWorld(new WorldCreator(w)).getName(), x, y, z),
                    Boundary.INFINITE);
            TTTCore.locale.getLocalizable("info.personal.arena.create.success").withPrefix(INFO_COLOR.toString())
                    .withReplacements(args[1]).sendTo(sender);
        }
    }
}
