/*
 * TTT
 * Copyright (c) 2014, Maxim Roncacé <http://bitbucket.org/mproncace>
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
package net.amigocraft.ttt.managers.command;

import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.Config;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class SetExitCommand extends SubcommandHandler {

	public SetExitCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	public void handle(){
		if (sender.hasPermission("ttt.setexit")){
			if (sender instanceof Player){
				try {
					File spawnFile = new File(Main.plugin.getDataFolder() + File.separator + "spawn.yml");
					if (!spawnFile.exists()){
						if (Config.VERBOSE_LOGGING){
							Main.mg.log("No spawn.yml found, creating...", LogLevel.INFO);
						}
						spawnFile.createNewFile();
					}
					YamlConfiguration spawnYaml = new YamlConfiguration();
					spawnYaml.load(spawnFile);
					spawnYaml.set("world", ((Player) sender).getLocation().getWorld().getName());
					spawnYaml.set("x", ((Player) sender).getLocation().getBlockX() + 0.5);
					spawnYaml.set("y", ((Player) sender).getLocation().getBlockY());
					spawnYaml.set("z", ((Player) sender).getLocation().getBlockZ() + 0.5);
					spawnYaml.save(spawnFile);
					Location l = ((Player) sender).getLocation();
					Main.mg.getConfigManager().setDefaultExitLocation(new Location(l.getWorld(), l.getBlockX() + 0.5, l.getBlockY(), l.getBlockZ() + 0.5));
					sender.sendMessage(ChatColor.DARK_PURPLE + "Successfully set TTT return point!");
				}
				catch (Exception ex){
					ex.printStackTrace();
					sender.sendMessage(ChatColor.RED + "An error occurred while setting the TTT return point.");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + Main.locale.getMessage("must-be-ingame"));
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + Main.locale.getMessage("no-permission"));
		}
	}
}
