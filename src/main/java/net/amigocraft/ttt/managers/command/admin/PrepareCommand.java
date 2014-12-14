package net.amigocraft.ttt.managers.command.admin;

import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.api.Stage;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.TTTPlayer;
import net.amigocraft.ttt.managers.command.SubcommandHandler;
import net.amigocraft.ttt.util.NumUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PrepareCommand extends SubcommandHandler {

	public PrepareCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		String arena = args[1];
		Round r = Main.mg.getRound(arena);
		if (r != null){
			if (args.length > 2 && NumUtil.isInt(args[2]))
				r.setPreparationTime(Integer.parseInt(args[2]));
			r.setStage(Stage.PREPARING);
			r.setTime(0); // this is automatic in MGLib 0.3.1 but I'd rather not bump the required version for something so simple
			// resetting the players is handled by MGListener
			sender.sendMessage(ChatColor.GREEN + "Set stage in arena " + ChatColor.ITALIC + r.getArena() + ChatColor.GREEN + " to preparing");
		}
		else
			sender.sendMessage(ChatColor.RED + "Cannot find a round in arena " + ChatColor.ITALIC + arena + ChatColor.RED + "!");
	}
}
