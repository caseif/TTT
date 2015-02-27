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

import net.caseif.ttt.Config;
import net.caseif.ttt.Main;
import net.caseif.ttt.managers.command.SubcommandHandler;

import java.io.File;

import net.amigocraft.mglib.api.LogLevel;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SetExitCommand extends SubcommandHandler {

	public SetExitCommand(CommandSender sender, String[] args) {
		super(sender, args, "ttt.setexit");
	}

	@Override
	public void handle() {
		if (assertPermission()) {
			if (sender instanceof Player) {
				try {
					File spawnFile = new File(Main.plugin.getDataFolder() + File.separator + "spawn.yml");
					if (!spawnFile.exists()) {
						if (Config.VERBOSE_LOGGING) {
							Main.mg.log(getMessage("info.plugin.compatibility.creating-file", null, false, "spawn.yml"), LogLevel.INFO);
						}
						spawnFile.createNewFile();
					}
					boolean keepOrientation = false;
					if (args.length > 1) {
						if (args[1].equalsIgnoreCase("true") ||
								args[1].equalsIgnoreCase("yes") ||
								args[1].equalsIgnoreCase("1")) {
							keepOrientation = true;
						}
						else if (args[1].equalsIgnoreCase("false") ||
								args[1].equalsIgnoreCase("no") ||
								args[1].equalsIgnoreCase("0")) {
							keepOrientation = false;
						}
						else {
							sender.sendMessage(getMessage("error.command.invalid-args", ERROR_COLOR));
							return;
						}
					}
					YamlConfiguration spawnYaml = new YamlConfiguration();
					spawnYaml.load(spawnFile);
					Location l = ((Player)sender).getLocation();
					spawnYaml.set("world", l.getWorld().getName());
					spawnYaml.set("x", l.getBlockX() + 0.5);
					spawnYaml.set("y", l.getBlockY());
					spawnYaml.set("z", l.getBlockZ() + 0.5);
					spawnYaml.set("pitch", keepOrientation ? l.getPitch() : null);
					spawnYaml.set("yaw", keepOrientation ? l.getYaw() : null);
					spawnYaml.save(spawnFile);
					if (keepOrientation) {
						Main.mg.getConfigManager().setDefaultExitLocation(
								new Location(l.getWorld(), l.getBlockX() + 0.5, l.getBlockY(), l.getBlockZ() + 0.5,
										l.getYaw(), l.getPitch())
						);
					}
					else {
						Main.mg.getConfigManager().setDefaultExitLocation(
								new Location(l.getWorld(), l.getBlockX() + 0.5, l.getBlockY(), l.getBlockZ() + 0.5)
						);
					}
					sender.sendMessage(getMessage("info.personal.set-exit.success", INFO_COLOR));
				}
				catch (Exception ex) {
					ex.printStackTrace();
					sender.sendMessage(getMessage("error.plugin.set-exit", ERROR_COLOR));
				}
			}
			else {
				sender.sendMessage(getMessage("error.command.ingame", ERROR_COLOR));
			}
		}
	}
}
