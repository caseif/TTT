package net.amigocraft.TTT.managers;

import java.io.File;
import java.text.DecimalFormat;

import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.Stage;
import net.amigocraft.TTT.TTT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class LobbyManager {

	private static DecimalFormat df = new DecimalFormat("##");

	/**
	 * Starts a task which manipulates a lobby sign each second.
	 * @param b The block representing the sign to be managed.
	 * @param world The name of the round to be tracked.
	 * @param type The type of lobby sign to be created.
	 * @param p The player who created the sign.
	 */
	public static void manageSign(Block b, String world, String type, Player p){
		if (b.getState() instanceof Sign){
			final Sign s = (Sign)b.getState();
			Round ro = Round.getRound(world);
			if (ro == null){
				File folder = null;
				File tttFolder = null;
				for (String str : Bukkit.getWorldContainer().list()){
					if (str.equalsIgnoreCase(world)){
						folder = new File(str);
						world = str;
					}
					else if (str.equalsIgnoreCase("TTT_" + world))
						tttFolder = new File(str);
					if (folder != null && tttFolder != null)
						break;
					
				}
				if (folder != null && tttFolder != null)
					ro = new Round(world);
				else {
					p.sendMessage(ChatColor.RED + TTT.local.getMessage("map-invalid"));
					return;
				}
			}
			if (type.equalsIgnoreCase("status")){
				s.setLine(0, ChatColor.DARK_RED + ro.getWorld());
				ro = null;
				final String w = world;
				Bukkit.getScheduler().runTaskTimer(TTT.plugin, new Runnable(){
					public void run(){
						Round r = Round.getRound(w);
						String max = TTT.plugin.getConfig().getInt("maximum-players") + "";
						if (max.equals("-1"))
							max = "∞";
						String players = r.getPlayers().size() + "/" + max;
						if (!max.equals("∞")){
							if (r.getPlayers().size() >= Integer.parseInt(max))
								players = ChatColor.RED + players;
							else
								players = ChatColor.GOLD + players;
						}
						else
							players = ChatColor.GOLD + players;
						s.setLine(1, players);
						String status = r.getStage().toString();
						if (status.equals("PLAYING"))
							status = ChatColor.RED + "INGAME";
						else if (status.equals("WAITING") || status.equals("RESETTING"))
							status = ChatColor.GRAY + status;
						else if (status.equals("PREPARING"))
							status = ChatColor.GREEN + status;
						s.setLine(2, status);
						String time = "";
						if (r.getStage() != Stage.WAITING && r.getStage() != Stage.RESETTING){
							String seconds = Integer.toString(r.getTime() % 60);
							if (seconds.length() == 1)
								seconds = "0" + seconds;
							time = df.format(r.getTime() / 60) + ":" + seconds;
							if (r.getTime() <= 60)
								time = ChatColor.RED + time;
							else
								time = ChatColor.GREEN + time;
						}
						s.setLine(3, time);
						s.update();
					}
				}, 0L, 20L);
			}
		}
	}

}
