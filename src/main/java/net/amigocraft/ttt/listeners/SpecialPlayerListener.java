package net.amigocraft.ttt.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpecialPlayerListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if (e.getPlayer().hasPermission("ttt.build.warn")){
			e.getPlayer().sendMessage(ChatColor.RED + "This version of TTT requires MGLib version 0.3.0 or higher. You can download and install it from " +
					"http://dev.bukkit.org/bukkit-plugins/mglib/. Note that TTT " + ChatColor.ITALIC + "will not function " +
					ChatColor.RED + "without it!");
		}
	}

}
