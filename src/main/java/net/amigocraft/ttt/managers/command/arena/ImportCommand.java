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
package net.amigocraft.ttt.managers.command.arena;

import net.amigocraft.mglib.exception.ArenaExistsException;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import net.amigocraft.ttt.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;

import java.io.File;

public class ImportCommand extends SubcommandHandler {

	public ImportCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (sender.hasPermission("ttt.arena.import")){
			if (args.length > 1){
				String worldName = "";
				for (File f : Bukkit.getWorldContainer().listFiles()){
					if (f.getName().equalsIgnoreCase(args[1])){
						worldName = f.getName();
					}
				}
				if (!worldName.equals("")){
					if (FileUtil.isWorld(args[1])){
						World w = Bukkit.createWorld(new WorldCreator(worldName));
						if (w != null){
							try {
								Main.mg.createArena(worldName, w.getSpawnLocation());
								sender.sendMessage(ChatColor.DARK_PURPLE + "[TTT] " + Main.locale.getMessage("import-success"));
							}
							catch (ArenaExistsException e){
								//TODO: replace this message with something more accurate
								sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("already-imported"));
							}
						}
						else {
							sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("cannot-load-world"));
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("cannot-load-world"));
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("folder-error"));
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-1"));
				sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("usage-import"));
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + Main.locale.getMessage("no-permission-import"));
		}
	}
}
