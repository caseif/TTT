package net.amigocraft.ttt.managers.command.admin;

import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.api.Stage;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class EndCommand extends SubcommandHandler {

	public EndCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (args.length > 1){
			String arena = args[1];
			Round r = Main.mg.getRound(arena);
			if (r.getStage() == Stage.PREPARING || r.getStage() == Stage.PLAYING){
				if (args.length > 2){
					if (args[2].equalsIgnoreCase("t"))
						r.setMetadata("t-victory", true);
					else if (args[2].equalsIgnoreCase("i"))
						r.setMetadata("t-victory", false);
					else {
						sender.sendMessage(ChatColor.RED + "[TTT]" + Main.locale.getMessage("invalid-args-2"));
						return;
					}
				}
				r.end();
			}
			else
				sender.sendMessage(ChatColor.RED + "[TTT] Cannot find a round in arena " + ChatColor.ITALIC + arena + ChatColor.RED + "!");
		}
		else
			sender.sendMessage(ChatColor.RED + "[TTT]" + Main.locale.getMessage("invalid-args-1"));
	}
}
