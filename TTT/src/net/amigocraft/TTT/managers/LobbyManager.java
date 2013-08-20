package net.amigocraft.TTT.managers;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.amigocraft.TTT.LobbySign;
import net.amigocraft.TTT.Role;
import net.amigocraft.TTT.Round;
import net.amigocraft.TTT.Stage;
import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class LobbyManager {

	private static DecimalFormat df = new DecimalFormat("##");

	public static List<LobbySign> playerSigns = new ArrayList<LobbySign>();

	/**
	 * Creates a new LobbySign to be managed
	 * @param b The Block containing the sign
	 * @param world The name of the world containing the sign
	 * @param type The type of the sign
	 * @param number The number of the sign (applicable only for "players" signs)
	 * @param p The player creating the sign
	 */
	public static void addSign(Block b, String world, String type, int number, Player p){
		if (b.getState() instanceof Sign){
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
				playerSigns.add(new LobbySign(b.getX(), b.getY(), b.getZ(), b.getWorld().getName(), world, 0,
						type.toLowerCase()));
				updateSigns(world);
			}
			else if (type.equalsIgnoreCase("players")){
				if (number > 0){
					playerSigns.add(new LobbySign(b.getX(), b.getY(), b.getZ(), b.getWorld().getName(), world, number,
							type.toLowerCase()));
					updateSigns(world);
				}
				else
					p.sendMessage(ChatColor.RED + TTT.local.getMessage("invalid-sign"));
			}
			else
				p.sendMessage(ChatColor.RED + TTT.local.getMessage("invalid-sign"));
		}
	}

	/**
	 * Updates all LobbySigns linked to a specific TTT world
	 * @param world The world to update signs for
	 */
	public static void updateSigns(String world){
		Round r = Round.getRound(world);
		if (r != null){
			List<TTTPlayer> players = r.getPlayers();
			for (LobbySign s : playerSigns){
				if (s.getRound().equals(world)){
					World w = Bukkit.getWorld(s.getWorld());
					if (w != null){
						Block b = w.getBlockAt(s.getX(), s.getY(), s.getZ());
						if (b != null){
							if (b.getState() instanceof Sign){
								final Sign sign = (Sign)b.getState();
								if (s.getType().equals("status")){
									sign.setLine(0, "§4" + r.getWorld());
									String max = TTT.plugin.getConfig().getInt("maximum-players") + "";
									if (max.equals("-1"))
										max = "∞";
									String playerCount = r.getPlayers().size() + "/" + max;
									if (!max.equals("∞")){
										if (r.getPlayers().size() >= Integer.parseInt(max))
											playerCount = "§c" + playerCount;
										else
											playerCount = "§a" + playerCount;
									}
									else
										playerCount = "§6" + playerCount;
									sign.setLine(1, playerCount);
									String status = r.getStage().toString();
									String color = null;
									if (status.equals("PLAYING")){
										color = "§c";
										status = "INGAME";
									}
									else if (status.equals("WAITING") || status.equals("RESETTING"))
										color = "§7";
									else if (status.equals("PREPARING"))
										color = "§a";
									if (status.equals("WAITING"))
										status = TTT.local.getMessage(status.toLowerCase() + "-sign");
									else
										status = TTT.local.getMessage(status.toLowerCase());
									sign.setLine(2, color + status);
									String time = "";
									if (r.getStage() != Stage.WAITING && r.getStage() != Stage.RESETTING){
										String seconds = Integer.toString(r.getTime() % 60);
										if (seconds.length() == 1)
											seconds = "0" + seconds;
										time = df.format(r.getTime() / 60) + ":" + seconds;
										if (r.getTime() <= 60)
											time = "§c" + time;
										else
											time = "§a" + time;
									}
									sign.setLine(3, time);
								}
								else if (s.getType().equals("players") && s.getNumber() > 0){
									for (int i = 0; i <= 3; i++){
										if (players.size() >= (s.getNumber() - 1) * 4 + i + 1){
											TTTPlayer t = players.get((s.getNumber() - 1) * 4 + i);
											String name = t.getName();
											if (t.getRole() == Role.DETECTIVE)
												name = "§1" + name;
											if (!t.isDead())
												name = "§l" + name;
											else if (t.isTraitor() && t.isBodyFound())
												name = "§4§m" + name;
											else if (t.isBodyFound())
												name = "§m" + name;
											sign.setLine(i, name);
										}
										else
											sign.setLine(i, "");
									}
								}
								Bukkit.getScheduler().runTask(TTT.plugin, new Runnable(){
									public void run(){
										sign.update();
									}
								});
							}
						}
					}
				}
			}
		}
	}
}
