package net.amigocraft.TTT.managers;

import java.io.File;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;
import net.amigocraft.TTT.Variables;

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
		File karmaFile = new File(TTT.plugin.getDataFolder(), "karma.yml");
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
		File karmaFile = new File(TTT.plugin.getDataFolder(), "karma.yml");
		try {
			if (karmaFile.exists()){
				YamlConfiguration karmaYaml = new YamlConfiguration();
				karmaYaml.load(karmaFile);
				if (karmaYaml.isSet(pName))
					if (karmaYaml.getInt(pName) > Variables.max_karma)
						playerKarma.put(pName, Variables.max_karma);
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
				t.addKarma(Variables.karma_heal);
				if (!t.hasTeamKilled()){
					int add = Variables.karma_clean_bonus;
					if (t.getKarma() > Variables.default_karma){
						if ((Variables.max_karma -
								Variables.default_karma) > 0){
							int above = t.getKarma() - Variables.default_karma;
							double percentage = above /
									(Variables.max_karma -
											Variables.default_karma);
							double divide = percentage / Variables.karma_clean_half;
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
						victim.getKarma() * (damage * Variables.damage_penalty)));
			else if (!damager.isTraitor() && victim.isTraitor())
				damager.addKarma((int)(Variables.max_karma *
						damage * Variables.t_damage_reward));
		}
	}

	public static void handleKillKarma(TTTPlayer killer, TTTPlayer victim){
		if (killer.isTraitor() == victim.isTraitor())
			handleDamageKarma(killer, victim, Variables.kill_penalty);
		else if (!killer.isTraitor() && victim.isTraitor())
			killer.addKarma(Variables.tbonus *
					Variables.t_damage_reward * victim.getKarma());
	}

	public static void handleKick(TTTPlayer t){
		Player p = TTT.plugin.getServer().getPlayer(t.getName());
		if (p != null){
			RoundManager.resetPlayer(p);
			if (Variables.karma_ban){
				File f = new File(TTT.plugin.getDataFolder(), "bans.yml");
				YamlConfiguration y = new YamlConfiguration();
				try {
					y.load(f);
					if (Variables.karma_ban_time < 0){
						y.set(t.getName(), -1);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("karma-permaban")
								.replace("%", Variables.karma_kick + "."));
					}
					else {
						// store unban time as a Unix timestamp
						int unbanTime = (int)System.currentTimeMillis() / 1000 +
								(Variables.karma_ban_time * 60);
						y.set(t.getName(), unbanTime);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("karma-ban")
								.replace("&", Integer.toString(Variables.karma_ban_time))
								.replace("%", Variables.karma_kick + "."));
					}
				}
				catch (Exception ex){
					ex.printStackTrace();
					TTT.log.warning(TTT.local.getMessage("ban-fail").replace("%", t.getName()));
				}
			}
			else
				p.sendMessage(ChatColor.DARK_PURPLE + TTT.local.getMessage("karma-kick")
						.replace("%", Integer.toString(Variables.karma_kick)));
		}
	}
}
