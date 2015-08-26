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
package net.caseif.ttt.manager.command.arena;

import static net.caseif.ttt.util.MiscUtil.isInt;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.manager.command.SubcommandHandler;
import net.caseif.ttt.util.Constants.Color;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.util.physical.Location3D;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveSpawnCommand extends SubcommandHandler {

    public RemoveSpawnCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.arena.removespawn");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            if (!TTTCore.mg.getArena(args[1]).isPresent()) {
                TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ERROR.toString()).sendTo(sender);
                sendUsage();
                return;
            }
            Arena arena = TTTCore.mg.getArena(args[1]).get();
            int x = 0;
            int y = 0;
            int z = 0;
            int index = Integer.MAX_VALUE;
            if (args.length == 2) { // use sender's location
                if (sender instanceof Player) {
                    x = ((Player) sender).getLocation().getBlockX();
                    y = ((Player) sender).getLocation().getBlockY();
                    z = ((Player) sender).getLocation().getBlockZ();
                } else {
                    TTTCore.locale.getLocalizable("error.command.ingame").withPrefix(Color.ERROR.toString())
                            .sendTo(sender);
                    return;
                }
            } else if (args.length == 3) {
                if (isInt(args[2])) {
                    index = Integer.parseInt(args[2]);
                } else {
                    TTTCore.locale.getLocalizable("error.command.invalid-args").withPrefix(Color.ERROR.toString())
                            .sendTo(sender);
                    sendUsage();
                    return;
                }
            } else if (args.length == 5) { // use 3 provided coords
                if (isInt(args[2]) && isInt(args[3]) && isInt(args[4])) {
                    x = Integer.parseInt(args[2]);
                    y = Integer.parseInt(args[3]);
                    z = Integer.parseInt(args[4]);
                } else {
                    TTTCore.locale.getLocalizable("error.command.invalid-args").withPrefix(Color.ERROR.toString())
                            .sendTo(sender);
                    sendUsage();
                    return;
                }
            } else {
                TTTCore.locale.getLocalizable("error.command.invalid-args").withPrefix(Color.ERROR.toString())
                        .sendTo(sender);
                sendUsage();
                return;
            }
            try {
                if (index != Integer.MAX_VALUE) {
                    arena.removeSpawnPoint(index);
                } else {
                    arena.removeSpawnPoint(new Location3D(x, y, z));
                }
            } catch (IllegalArgumentException ex) {
                TTTCore.locale.getLocalizable("error.command.invalid-args").withPrefix(Color.ERROR.toString())
                        .sendTo(sender);
            }
        }
    }
}
