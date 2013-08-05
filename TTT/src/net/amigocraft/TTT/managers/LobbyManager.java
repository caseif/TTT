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
	public static void manageSign(Block b, final String world, String type, Player p){
		if (b.getState() instanceof Sign){
			final Sign s = (Sign)b.getState();
			Round ro = Round.getRound(world);
			if (ro == null){
				File folder = new File(world);
				File tttFolder = new File("TTT_" + world);
				if (folder.exists() && tttFolder.exists()){
					ro = new Round(world);
				}
				else {
					p.sendMessage(ChatColor.RED + TTT.local.getMessage("map-invalid"));
					return;
				}
			}
			final Round r = ro;
			ro = null;
			if (type.equalsIgnoreCase("status")){
				s.setLine(0, r.getWorld());
				Bukkit.getScheduler().runTaskTimer(TTT.plugin, new Runnable(){
					public void run(){
						String max = TTT.plugin.getConfig().getInt("maximum-players") + "";
						if (max.equals("-1"))
							max = "∞";
						s.setLine(1, r.getPlayers().size() + "/" + max);
						String status = r.getStage().toString();
						if (status.equals("PLAYING"))
							status = "INGAME";
						s.setLine(2, status);
						String time = "";
						if (r.getStage() != Stage.WAITING)
							time = df.format(r.getTime() / 60) + ":" + (r.getTime() % 60);
						s.setLine(3, time);
						s.update();
					}
				}, 0L, 20L);
			}
		}
	}

}
