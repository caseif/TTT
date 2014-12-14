/*
 * TTT
 * Copyright (c) 2014, Maxim Roncacé <http://bitbucket.org/mproncace>
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
package net.amigocraft.ttt.managers;

import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.api.Minigame;
import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.exception.NoSuchPlayerException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.TTTPlayer;
import net.amigocraft.ttt.Config;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;

public class KarmaManager {

	public static HashMap<String, Integer> playerKarma = new HashMap<String, Integer>();

	public static void saveKarma(Round round){
		for (MGPlayer mp : round.getPlayerList()){
			KarmaManager.saveKarma((TTTPlayer) mp);
		}
	}

	public static void saveKarma(TTTPlayer t){
		playerKarma.remove(t.getName());
		playerKarma.put(t.getName(), t.getKarma());
		File karmaFile = new File(Main.plugin.getDataFolder(), "karma.yml");
		try {
			if (karmaFile.exists()){
				YamlConfiguration karmaYaml = new YamlConfiguration();
				karmaYaml.load(karmaFile);
				karmaYaml.set(Minigame.getOnlineUUIDs().get(t.getName()).toString(), t.getKarma());
				karmaYaml.save(karmaFile);
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public static void loadKarma(String pName){
		File karmaFile = new File(Main.plugin.getDataFolder(), "karma.yml");
		try {
			String uuid = Minigame.getOnlineUUIDs().get(pName).toString();
			if (!karmaFile.exists()){
				Main.createFile("karma.yml");
			}
			YamlConfiguration karmaYaml = new YamlConfiguration();
			karmaYaml.load(karmaFile);
			if (karmaYaml.isSet(uuid)){
				if (karmaYaml.getInt(uuid) > Config.MAX_KARMA){
					playerKarma.put(pName, Config.MAX_KARMA);
				}
				else {
					playerKarma.put(pName, karmaYaml.getInt(uuid));
				}
			}
			else {
				playerKarma.put(pName, Config.DEFAULT_KARMA);
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public static void allocateKarma(Round round){
		for (MGPlayer mp : round.getPlayerList()){
			TTTPlayer t = (TTTPlayer) mp;
			t.addKarma(Config.KARMA_HEAL);
			if (!t.hasTeamKilled()){
				int add = Config.KARMA_CLEAN_BONUS;
				if (t.getKarma() > Config.DEFAULT_KARMA){
					if ((Config.MAX_KARMA - Config.DEFAULT_KARMA) > 0){
						add = (int) Math.round(
								Config.KARMA_CLEAN_BONUS * Math.pow(
										.5, (t.getKarma() - (double) Config.DEFAULT_KARMA) /
												((double) (Config.MAX_KARMA - Config.DEFAULT_KARMA) * Config.KARMA_CLEAN_HALF)
								)
						);
					}
				}
				t.addKarma(add);
			}
		}
	}

	public static void handleDamageKarma(TTTPlayer damager, TTTPlayer victim, double damage){
		if (damager != null && victim != null){
			if (damager.getTeam().equals("Traitor") == victim.getTeam().equals("Traitor")){ // team damage
				damager.subtractKarma((int) (victim.getKarma() * (damage * Config.DAMAGE_PENALTY)));
			}
			else if (!damager.getTeam().equals("Traitor") && victim.getTeam().equals("Traitor")){ // innocent damaging traitor
				damager.addKarma((int) (Config.MAX_KARMA *
						damage * Config.T_DAMAGE_REWARD));
			}
		}
	}

	public static void handleKillKarma(TTTPlayer killer, TTTPlayer victim){
		if (killer.isTraitor() == victim.isTraitor()){
			handleDamageKarma(killer, victim, Config.KILL_PENALTY);
		}
		else if (!killer.isTraitor()){
			killer.addKarma(Config.TBONUS *
					Config.T_DAMAGE_REWARD * victim.getKarma());
		}
	}

	public static void handleKick(TTTPlayer t){
		@SuppressWarnings("deprecation") Player p = Main.plugin.getServer().getPlayer(t.getName());
		if (p != null){
			try {
				t.removeFromRound();
			}
			catch (NoSuchPlayerException ex){
				ex.printStackTrace();
			}
			catch (PlayerOfflineException ex){ // neither can be thrown
				ex.printStackTrace();
			}
			if (Config.KARMA_BAN){
				File f = new File(Main.plugin.getDataFolder(), "bans.yml");
				YamlConfiguration y = new YamlConfiguration();
				try {
					y.load(f);
					if (Config.KARMA_BAN_TIME < 0){
						y.set(t.getName(), -1);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("karma-permaban").replace("%", Config.KARMA_KICK + "."));
					}
					else {
						// store unban time as a Unix timestamp
						int unbanTime = (int) System.currentTimeMillis() / 1000 + (Config.KARMA_BAN_TIME * 60);
						y.set(t.getName(), unbanTime);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("karma-ban").replace("&", Integer.toString(Config.KARMA_BAN_TIME)).replace("%", Config.KARMA_KICK + "."));
					}
				}
				catch (Exception ex){
					ex.printStackTrace();
					Main.mg.log(Main.locale.getMessage("ban-fail").replace("%", t.getName()), LogLevel.WARNING);
				}
			}
			else {
				p.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("karma-kick").replace("%", Integer.toString(Config.KARMA_KICK)));
			}
		}
	}
}
