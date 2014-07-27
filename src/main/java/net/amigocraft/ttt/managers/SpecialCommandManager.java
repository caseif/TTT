package net.amigocraft.ttt.managers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SpecialCommandManager implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (label.equalsIgnoreCase("ttt")){
			if (sender.hasPermission("ttt.build.warn"))
				sender.sendMessage(ChatColor.RED + "This version of TTT requires a library called MGLib. You can download and install it from " +
						"http://dev.bukkit.org/bukkit-plugins/mglib/. Note that TTT " + ChatColor.ITALIC + "will not function " +
						ChatColor.RED + "without it!");
			else
				sender.sendMessage(ChatColor.RED + "TTT is currently disabled!");
			return true;
		}
		return false;
	}

}
