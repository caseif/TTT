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

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.constant.Color;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.util.physical.Location3D;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class ListSpawnsCommand extends CommandHandler {

    public ListSpawnsCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        Optional<Arena> arena = TTTCore.mg.getArena(args[1]);
        if (!arena.isPresent()) {
            TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ALERT).sendTo(sender);
            return;
        }
        Map<Integer, Location3D> spawns = arena.get().getSpawnPoints();
        TTTCore.locale.getLocalizable("info.personal.arena.listspawns").withPrefix(Color.INFO)
                .withReplacements(Color.EM + arena.get().getName() + Color.INFO).sendTo(sender);
        for (Map.Entry<Integer, Location3D> spawn : spawns.entrySet()) {
            Location3D l = spawn.getValue();
            sender.sendMessage(Color.SECONDARY + "    " + spawn.getKey() + ": " + ChatColor.WHITE
                    + "(" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ")");
        }
    }

}
