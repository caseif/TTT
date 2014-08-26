package net.amigocraft.ttt.managers;

import net.amigocraft.mglib.api.LogLevel;
import net.amigocraft.mglib.api.MGPlayer;
import net.amigocraft.mglib.api.Minigame;
import net.amigocraft.mglib.api.Round;
import net.amigocraft.mglib.exception.NoSuchPlayerException;
import net.amigocraft.mglib.exception.PlayerOfflineException;
import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.TTTPlayer;
import net.amigocraft.ttt.Variables;
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
				if (karmaYaml.getInt(uuid) > Variables.MAX_KARMA){
					playerKarma.put(pName, Variables.MAX_KARMA);
				}
				else {
					playerKarma.put(pName, karmaYaml.getInt(uuid));
				}
			}
			else {
				playerKarma.put(pName, Variables.DEFAULT_KARMA);
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public static void allocateKarma(Round round){
		for (MGPlayer mp : round.getPlayerList()){
			TTTPlayer t = (TTTPlayer) mp;
			t.addKarma(Variables.KARMA_HEAL);
			if (!t.hasTeamKilled()){
				int add = Variables.KARMA_CLEAN_BONUS;
				if (t.getKarma() > Variables.DEFAULT_KARMA){
					if ((Variables.MAX_KARMA - Variables.DEFAULT_KARMA) > 0){
						add = (int) Math.round(Variables.KARMA_CLEAN_BONUS * Math.pow(.5, (t.getKarma() - (double) Variables.DEFAULT_KARMA) / ((double) (Variables.MAX_KARMA - Variables.DEFAULT_KARMA) * Variables.KARMA_CLEAN_HALF)));
					}
				}
				t.addKarma(add);
			}
		}
	}

	public static void handleDamageKarma(TTTPlayer damager, TTTPlayer victim, double damage){
		if (damager != null && victim != null){
			if (damager.getTeam().equals("Traitor") == victim.getTeam().equals("Traitor")) // team damage
			{
				damager.subtractKarma((int) (victim.getKarma() * (damage * Variables.DAMAGE_PENALTY)));
			}
			else if (!damager.getTeam().equals("Traitor") && victim.getTeam().equals("Traitor")){ // innocent damaging traitor
				damager.addKarma((int) (Variables.MAX_KARMA *
						damage * Variables.T_DAMAGE_REWARD));
			}
		}
	}

	public static void handleKillKarma(TTTPlayer killer, TTTPlayer victim){
		if (killer.isTraitor() == victim.isTraitor()){
			handleDamageKarma(killer, victim, Variables.KILL_PENALTY);
		}
		else if (!killer.isTraitor()){
			killer.addKarma(Variables.TBONUS *
					Variables.T_DAMAGE_REWARD * victim.getKarma());
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
			if (Variables.KARMA_BAN){
				File f = new File(Main.plugin.getDataFolder(), "bans.yml");
				YamlConfiguration y = new YamlConfiguration();
				try {
					y.load(f);
					if (Variables.KARMA_BAN_TIME < 0){
						y.set(t.getName(), -1);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("karma-permaban").replace("%", Variables.KARMA_KICK + "."));
					}
					else {
						// store unban time as a Unix timestamp
						int unbanTime = (int) System.currentTimeMillis() / 1000 + (Variables.KARMA_BAN_TIME * 60);
						y.set(t.getName(), unbanTime);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("karma-ban").replace("&", Integer.toString(Variables.KARMA_BAN_TIME)).replace("%", Variables.KARMA_KICK + "."));
					}
				}
				catch (Exception ex){
					ex.printStackTrace();
					Main.mg.log(Main.locale.getMessage("ban-fail").replace("%", t.getName()), LogLevel.WARNING);
				}
			}
			else {
				p.sendMessage(ChatColor.DARK_PURPLE + Main.locale.getMessage("karma-kick").replace("%", Integer.toString(Variables.KARMA_KICK)));
			}
		}
	}
}
