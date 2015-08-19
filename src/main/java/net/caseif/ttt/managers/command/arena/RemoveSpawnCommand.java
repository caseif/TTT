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
import static net.caseif.ttt.util.MiscUtil.getMessage;

import net.caseif.ttt.managers.command.SubcommandHandler;
import net.caseif.ttt.util.NumUtil;

import net.amigocraft.mglib.MGUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class RemoveSpawnCommand extends SubcommandHandler {

    public RemoveSpawnCommand(CommandSender sender, String[] args) {
        super(sender, args, "ttt.arena.removespawn");
    }

    @Override
    public void handle() {
        if (assertPermission()) {
            int x = 0;
            int y = 0;
            int z = 0;
            int index = Integer.MAX_VALUE;
            if (args.length == 2) { // use sender's location
                if (sender instanceof Player) {
                    x = ((Player)sender).getLocation().getBlockX();
                    y = ((Player)sender).getLocation().getBlockY();
                    z = ((Player)sender).getLocation().getBlockZ();
                }
                else {
                    sender.sendMessage(getMessage("error.command.ingame", ERROR_COLOR));
                    return;
                }
            }
            else if (args.length == 3) {
                if (NumUtil.isInt(args[2])) {
                    index = Integer.parseInt(args[2]);
                }
                else {
                    sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
                    sendUsage();
                    return;
                }
            }
            else if (args.length == 5) { // use 3 provided coords
                if (NumUtil.isInt(args[2]) && NumUtil.isInt(args[3]) && NumUtil.isInt(args[4])) {
                    x = Integer.parseInt(args[2]);
                    y = Integer.parseInt(args[3]);
                    z = Integer.parseInt(args[4]);
                }
                else {
                    sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
                    sendUsage();
                    return;
                }
            }
            else {
                sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
                sendUsage();
                return;
            }
            if (index != Integer.MAX_VALUE) {
                YamlConfiguration yaml = MGUtil.loadArenaYaml("TTT");
                if (yaml.isSet(args[1] + ".spawns")) {
                    if (yaml.isSet(args[1] + ".spawns." + index)) {
                        yaml.set(args[1] + ".spawns." + index, null);
                        MGUtil.saveArenaYaml("TTT", yaml);
                    }
                    else {
                        sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
                        sendUsage();
                    }
                }
                else {
                    sender.sendMessage(getMessage("error.arena.dne", ERROR_COLOR));
                }
            }
            else {
                YamlConfiguration yaml = MGUtil.loadArenaYaml("TTT");
                if (yaml.isSet(args[1] + ".spawns")) {
                    ConfigurationSection cs = yaml.getConfigurationSection(args[1] + ".spawns");
                    for (String k : cs.getKeys(false)) {
                        if (cs.getInt(k + ".x") == x && cs.getInt(k + ".y") == y && cs.getInt(k + ".z") == z) {
                            cs.set(k, null);
                            MGUtil.saveArenaYaml("TTT", yaml);
                            return;
                        }
                    }
                    sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
                    sendUsage();
                }
                else {
                    sender.sendMessage(getMessage("error.arena.dne", ERROR_COLOR));
                }
            }
        }
    }
}
