package net.amigocraft.ttt.managers.command.admin;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.exception.NoSuchPlayerException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class KickCommand extends SubcommandHandler {

	public KickCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (args.length > 1){
			String name = args[1];
			MGPlayer mp = Main.mg.getMGPlayer(name);
			if (mp != null){
				try {
					mp.removeFromRound();
					mp.getBukkitPlayer().sendMessage(ChatColor.DARK_PURPLE + "[TTT] You have been kicked from your current round!");
				}
				catch (NoSuchPlayerException ex){
					sender.sendMessage(ChatColor.RED + "[TTT] The specified player is not in a round!"); // shouldn't ever happen
				}
				catch (PlayerOfflineException ex){
					sender.sendMessage(ChatColor.RED + "[TTT] The specified player is not online!");
				}
			}
			else
				sender.sendMessage(ChatColor.RED + "[TTT] The specified player is not in a round!");
		}
		else
			sender.sendMessage(ChatColor.RED + "[TTT] " + Main.locale.getMessage("invalid-args-1"));
	}
}
