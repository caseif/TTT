/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2017, Max Roncace <me@caseif.net>
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
import net.caseif.ttt.util.helper.gamemode.ArenaHelper;
import net.caseif.ttt.util.helper.io.FileHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.util.physical.Boundary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;

import java.io.File;

public class ImportCommand extends CommandHandler {

    public ImportCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (TTTCore.mg.getArena(args[1].toLowerCase()).isPresent()) {
            TTTCore.locale.getLocalizable("error.arena.already-exists").withPrefix(Color.ALERT)
                    .sendTo(sender);
            return;
        }

        String worldName = null;
        assert Bukkit.getWorldContainer().listFiles() != null;
        for (File f : Bukkit.getWorldContainer().listFiles()) {
            if (f.getName().equalsIgnoreCase(args[1])) {
                worldName = f.getName();
            }
        }
        if (worldName != null) {
            if (FileHelper.isWorld(args[1])) {
                World w = Bukkit.createWorld(new WorldCreator(worldName));
                if (w != null) {
                    Location l = w.getSpawnLocation();
                    TTTCore.mg.createBuilder(Arena.class).id(worldName).spawnPoints(LocationHelper.convert(l))
                            .boundary(Boundary.INFINITE).build();
                    ArenaHelper.updateShuffledArenas();
                    TTTCore.locale.getLocalizable("info.personal.arena.import.success").withPrefix(Color.INFO)
                            .sendTo(sender);
                    return;
                }
            }
        }
        // this executes only if something goes wrong loading the world
        TTTCore.locale.getLocalizable("error.plugin.world-load").withPrefix(Color.ALERT).sendTo(sender);
    }

}
