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

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.NumUtil;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.util.physical.Location3D;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddSpawnCommand extends SubcommandHandler {

    public AddSpawnCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.arena.addspawn");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
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
                    TTTCore.locale.getLocalizable("error.command.ingame").withPrefix(Color.ERROR.toString())
                            .sendTo(sender);
                    return;
                }
            } else if (args.length == 5) { // use 3 provided coords
                if (NumUtil.isInt(args[2]) && NumUtil.isInt(args[3]) && NumUtil.isInt(args[4])) {
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
            Optional<Arena> arena = TTTCore.mg.getArena(args[1]);
            if (arena.isPresent()) {
                if (w == null) {
                    arena.get().addSpawnPoint(new Location3D(x, y, z));
                } else {
                    if (!arena.get().getWorld().equals(w.getName())) {
                        TTTCore.locale.getLocalizable("error.arena.invalid-location").withPrefix(Color.ERROR.toString())
                                .sendTo(sender);
                        return;
                    }
                    arena.get().addSpawnPoint(new Location3D(w.getName(), x, y, z));
                }
            } else {
                TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ERROR.toString()).sendTo(sender);
            }
        }
    }
}
