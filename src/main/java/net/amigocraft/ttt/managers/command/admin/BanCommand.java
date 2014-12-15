package net.amigocraft.ttt.managers.command.admin;

import net.amigocraft.mglib.UUIDFetcher;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import net.amigocraft.ttt.util.MiscUtil;
import net.amigocraft.ttt.util.NumUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BanCommand extends SubcommandHandler {

	public BanCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (args.length > 1){
			if (sender.hasPermission("ttt.admin.ban")){
				String name = args[1];
				int time = -1;
				if (args.length > 2){
					if (NumUtil.isInt(args[2])){
						time = Integer.parseInt(args[2]);
					}
					else {
						sender.sendMessage(ChatColor.RED + "[TTT] Ban time must be a number!");
						return;
					}
				}
				try {
					MiscUtil.ban(UUIDFetcher.getUUIDOf(name), time);
				}
				catch (Exception ex){
					ex.printStackTrace();
					sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("exception"));
				}
			}
		}
		else
			sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-1"));
	}
}
