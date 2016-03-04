/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016, Max Roncace <me@caseif.net>
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

package net.caseif.ttt.command.handler.arena;

import static net.caseif.ttt.util.helper.data.DataVerificationHelper.isInt;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.Constants.Color;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.util.physical.Location3D;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveSpawnCommand extends CommandHandler {

    public RemoveSpawnCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (!TTTCore.mg.getArena(args[1]).isPresent()) {
            TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ERROR).sendTo(sender);
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
                TTTCore.locale.getLocalizable("error.command.ingame").withPrefix(Color.ERROR).sendTo(sender);
                return;
            }
        } else if (args.length == 3) {
            if (isInt(args[2])) {
                index = Integer.parseInt(args[2]);
            } else {
                printInvalidArgsError();
                return;
            }
        } else if (args.length == 5) { // use 3 provided coords
            if (isInt(args[2]) && isInt(args[3]) && isInt(args[4])) {
                x = Integer.parseInt(args[2]);
                y = Integer.parseInt(args[3]);
                z = Integer.parseInt(args[4]);
            } else {
                printInvalidArgsError();
                return;
            }
        }
        try {
            if (index != Integer.MAX_VALUE) {
                arena.removeSpawnPoint(index);
                TTTCore.locale.getLocalizable("info.personal.arena.removespawn.index").withPrefix(Color.INFO)
                        .withReplacements(Color.LABEL + index + Color.INFO,
                                Color.ARENA + arena.getName() + Color.INFO).sendTo(sender);
            } else {
                arena.removeSpawnPoint(new Location3D(arena.getWorld(), x, y, z));
                TTTCore.locale.getLocalizable("info.personal.arena.removespawn.coords").withPrefix(Color.INFO)
                        .withReplacements(Color.LABEL + "(" + x + ", " + y + ", " + z + ")" + Color.INFO,
                                Color.ARENA + arena.getName() + Color.INFO).sendTo(sender);
            }
        } catch (IllegalArgumentException ex) {
            TTTCore.locale.getLocalizable("error.arena.removespawn.missing").withPrefix(Color.ERROR).sendTo(sender);
        }
    }

}
