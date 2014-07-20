package net.amigocraft.ttt.managers;

import java.io.File;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.amigocraft.ttt.Main;
import net.amigocraft.ttt.TTTPlayer;
import net.amigocraft.ttt.Variables;

public class KarmaManager {

	public static HashMap<String, Integer> playerKarma = new HashMap<String, Integer>();

	public static void saveKarma(String worldName){
		for (TTTPlayer t : TTTPlayer.players)
			if (t.getWorld().equals(worldName))
				saveKarma(t);
	}

	public static void saveKarma(TTTPlayer t){
		playerKarma.remove(t.getName());
		playerKarma.put(t.getName(), t.getKarma());
		File karmaFile = new File(Main.plugin.getDataFolder(), "karma.yml");
		try {
			if (karmaFile.exists()){
				YamlConfiguration karmaYaml = new YamlConfiguration();
				karmaYaml.load(karmaFile);
				karmaYaml.set(t.getName(), t.getKarma());
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
			if (karmaFile.exists()){
				YamlConfiguration karmaYaml = new YamlConfiguration();
				karmaYaml.load(karmaFile);
				if (karmaYaml.isSet(pName))
					if (karmaYaml.getInt(pName) > Variables.MAX_KARMA)
						playerKarma.put(pName, Variables.MAX_KARMA);
					else
						playerKarma.put(pName, karmaYaml.getInt(pName));
				else
					playerKarma.put(pName, 1000);
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}

	public static void allocateKarma(String worldName){
		for (TTTPlayer t : TTTPlayer.players){
			if (t.getWorld().equals(worldName)){
				t.addKarma(Variables.KARMA_HEAL);
				if (!t.hasTeamKilled()){
					int add = Variables.KARMA_CLEAN_BONUS;
					if (t.getKarma() > Variables.DEFAULT_KARMA){
						if ((Variables.MAX_KARMA -
								Variables.DEFAULT_KARMA) > 0){
							int above = t.getKarma() - Variables.DEFAULT_KARMA;
							double percentage = above /
									(Variables.MAX_KARMA -
											Variables.DEFAULT_KARMA);
							double divide = percentage / Variables.KARMA_CLEAN_HALF;
							add /= 2 * divide;
						}
					}
					t.addKarma(add);
				}
			}
		}
	}

	public static void handleDamageKarma(TTTPlayer damager, TTTPlayer victim, int damage){
		if (damager != null && victim != null){
			if (damager.isTraitor() == victim.isTraitor())
				damager.subtractKarma((int)(
						victim.getKarma() * (damage * Variables.DAMAGE_PENALTY)));
			else if (!damager.isTraitor() && victim.isTraitor())
				damager.addKarma((int)(Variables.MAX_KARMA *
						damage * Variables.T_DAMAGE_REWARD));
		}
	}

	public static void handleKillKarma(TTTPlayer killer, TTTPlayer victim){
		if (killer.isTraitor() == victim.isTraitor())
			handleDamageKarma(killer, victim, Variables.KILL_PENALTY);
		else if (!killer.isTraitor() && victim.isTraitor())
			killer.addKarma(Variables.TBONUS *
					Variables.T_DAMAGE_REWARD * victim.getKarma());
	}

	public static void handleKick(TTTPlayer t){
		@SuppressWarnings("deprecation")
		Player p = Main.plugin.getServer().getPlayer(t.getName());
		if (p != null){
			RoundManager.resetPlayer(p);
			if (Variables.KARMA_BAN){
				File f = new File(Main.plugin.getDataFolder(), "bans.yml");
				YamlConfiguration y = new YamlConfiguration();
				try {
					y.load(f);
					if (Variables.KARMA_BAN_TIME < 0){
						y.set(t.getName(), -1);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + Main.local.getMessage("karma-permaban")
								.replace("%", Variables.KARMA_KICK + "."));
					}
					else {
						// store unban time as a Unix timestamp
						int unbanTime = (int)System.currentTimeMillis() / 1000 +
								(Variables.KARMA_BAN_TIME * 60);
						y.set(t.getName(), unbanTime);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + Main.local.getMessage("karma-ban")
								.replace("&", Integer.toString(Variables.KARMA_BAN_TIME))
								.replace("%", Variables.KARMA_KICK + "."));
					}
				}
				catch (Exception ex){
					ex.printStackTrace();
					Main.log.warning(Main.local.getMessage("ban-fail").replace("%", t.getName()));
				}
			}
			else
				p.sendMessage(ChatColor.DARK_PURPLE + Main.local.getMessage("karma-kick")
						.replace("%", Integer.toString(Variables.KARMA_KICK)));
		}
	}
}
