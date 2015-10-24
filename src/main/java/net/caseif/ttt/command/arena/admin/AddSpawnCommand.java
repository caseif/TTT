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
package net.caseif.ttt.command.arena.admin;

import static net.caseif.ttt.util.helper.misc.MiscHelper.isInt;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.CommandHandler;
import net.caseif.ttt.util.Constants.Color;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.util.physical.Location3D;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddSpawnCommand extends CommandHandler {

    public AddSpawnCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.superadmin");
    }

    @Override
    public void handle() {
        World w = null;
        int x;
        int y;
        int z;
        if (args.length == 2) { // use sender's location
            if (sender instanceof Player) {
                w = ((Player) sender).getWorld();
                x = ((Player) sender).getLocation().getBlockX();
                y = ((Player) sender).getLocation().getBlockY();
                z = ((Player) sender).getLocation().getBlockZ();
            } else {
                TTTCore.locale.getLocalizable("error.command.ingame").withPrefix(Color.ERROR).sendTo(sender);
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
        } else {
            printInvalidArgsError();
            return;
        }
        Optional<Arena> arena = TTTCore.mg.getArena(args[1]);
        if (arena.isPresent()) {
            Location3D loc = new Location3D(w != null ? w.getName() : null, x, y, z);
            if (w != null && !arena.get().getWorld().equals(w.getName())) {
                TTTCore.locale.getLocalizable("error.arena.invalid-location").withPrefix(Color.ERROR).sendTo(sender);
                return;
            }
            if (!arena.get().getBoundary().contains(loc)) {
                TTTCore.locale.getLocalizable("error.arena.create.bad-spawn").withPrefix(Color.ERROR).sendTo(sender);
                return;
            }
            arena.get().addSpawnPoint(loc);
            TTTCore.locale.getLocalizable("info.personal.arena.addspawn").withPrefix(Color.INFO)
                    .withReplacements("(" + x + ", " + y + ", " + z + ")", arena.get().getName()).sendTo(sender);
        } else {
            TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ERROR).sendTo(sender);
        }
    }

}
