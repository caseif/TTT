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
package net.caseif.ttt.command.arena;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.SubcommandHandler;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.helper.misc.FileHelper;
import net.caseif.ttt.util.helper.platform.LocationHelper;

import net.caseif.flint.util.physical.Boundary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;

import java.io.File;

public class ImportCommand extends SubcommandHandler {

    public ImportCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.arena.import");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            if (args.length > 1) {
                String worldName = null;
                for (File f : Bukkit.getWorldContainer().listFiles()) {
                    if (f.getName().equalsIgnoreCase(args[1])) {
                        worldName = f.getName();
                    }
                }
                if (worldName != null) {
                    if (FileHelper.isWorld(args[1])) {
                        World w = Bukkit.createWorld(new WorldCreator(worldName));
                        if (w != null) {
                            if (TTTCore.mg.getArena(worldName).isPresent()) {
                                //TODO: replace this message with something more accurate
                                TTTCore.locale.getLocalizable("error.arena.already-exists")
                                        .withPrefix(Color.ERROR).sendTo(sender);
                            }
                            Location l = w.getSpawnLocation();
                            TTTCore.mg.createArena(
                                    worldName,
                                    LocationHelper.convert(l),
                                    Boundary.INFINITE
                            );
                            TTTCore.locale.getLocalizable("info.personal.arena.import.success")
                                    .withPrefix(Color.INFO).sendTo(sender);
                            return;
                        }
                    }
                }
                // this executes only if something goes wrong loading the world
                TTTCore.locale.getLocalizable("error.plugin.world-load").withPrefix(Color.ERROR)
                        .sendTo(sender);
            } else {
                TTTCore.locale.getLocalizable("error.command.too-few-args").withPrefix(Color.ERROR)
                        .sendTo(sender);
                sendUsage();
            }
        }
    }
}
