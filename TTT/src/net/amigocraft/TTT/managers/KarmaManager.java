package net.amigocraft.TTT.managers;

import java.io.File;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.amigocraft.TTT.TTT;
import net.amigocraft.TTT.TTTPlayer;

public class KarmaManager {

	public static HashMap<String, Integer> playerKarma = new HashMap<String, Integer>();

	public static void saveKarma(String worldName){
		for (TTTPlayer t : TTTPlayer.players){
			if (t.getWorld().equals(worldName)){
				playerKarma.remove(t.getName());
				playerKarma.put(t.getName(), t.getKarma());
				saveKarma(t);
			}
		}

	}

	public static void saveKarma(TTTPlayer t){
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

	public static void swapDisplayKarma(String worldName){
		for (TTTPlayer t : TTTPlayer.players){
			if (t.getWorld().equals(worldName)){
				t.setDisplayKarma(t.getKarma());
			}
		}
	}

	public static void loadKarma(String pName){
		File karmaFile = new File(TTT.plugin.getDataFolder(), "karma.yml");
		try {
			if (karmaFile.exists()){
				YamlConfiguration karmaYaml = new YamlConfiguration();
				karmaYaml.load(karmaFile);
				if (karmaYaml.isSet(pName))
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
				t.addKarma(TTT.plugin.getConfig().getInt("karma-heal"));
				if (!t.hasTeamKilled()){
					int add = TTT.plugin.getConfig().getInt("karma-clean-bonus");
					if (t.getKarma() > TTT.plugin.getConfig().getInt("default-karma")){
						int above = t.getKarma() - TTT.plugin.getConfig().getInt("default-karma");
						double percentage = above /
								(TTT.plugin.getConfig().getInt("max-karma") -
										TTT.plugin.getConfig().getInt("default-karma"));
						double divide = percentage / TTT.plugin.getConfig().getInt("karma-clean-half");
						add /= 2 * divide;
					}
					t.addKarma(add);
				}
			}
		}
	}

	public static void handleDamageKarma(TTTPlayer damager, TTTPlayer victim, int damage){
		if (damager.isTraitor() == victim.isTraitor())
			damager.subtractKarma((int)(
					victim.getKarma() * (damage * TTT.plugin.getConfig().getDouble("damage-penalty"))));
		else if (!damager.isTraitor() && victim.isTraitor())
			damager.addKarma((int)(TTT.plugin.getConfig().getInt("max-karma") *
					damage * TTT.plugin.getConfig().getDouble("t-damage-reward")));
	}

	public static void handleKillKarma(TTTPlayer killer, TTTPlayer victim){
		if (killer.isTraitor() == victim.isTraitor())
			handleDamageKarma(killer, victim, TTT.plugin.getConfig().getInt("kill-penalty"));
		else if (!killer.isTraitor() && victim.isTraitor())
			killer.addKarma(TTT.plugin.getConfig().getInt("tbonus") *
					TTT.plugin.getConfig().getInt("t-damage-bonus") * victim.getKarma());
	}

	public static void handleKick(TTTPlayer t){
		Player p = TTT.plugin.getServer().getPlayer(t.getName());
		if (p != null){
			RoundManager.resetPlayer(p);
			if (TTT.plugin.getConfig().getBoolean("karma-ban")){
				File f = new File(TTT.plugin.getDataFolder(), "bans.yml");
				YamlConfiguration y = new YamlConfiguration();
				try {
					y.load(f);
					if (TTT.plugin.getConfig().getInt("karma-ban-time") < 0){
						y.set(t.getName(), -1);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + "You have been permanently banned from using TTT " +
								"on this server because your karma has fallen below " +
								TTT.plugin.getConfig().getInt("karma-ban") + ".");
					}
					else {
						// store unban time as a Unix timestamp
						int unbanTime = (int)System.currentTimeMillis() / 1000 +
								(TTT.plugin.getConfig().getInt("karma-ban-time") * 60);
						y.set(t.getName(), unbanTime);
						y.save(f);
						p.sendMessage(ChatColor.DARK_PURPLE + "You have been banned from using TTT " +
								"on this server for " + TTT.plugin.getConfig().getInt("karma-ban-time") +
								" because your karma has fallen below " +
								TTT.plugin.getConfig().getInt("karma-ban") + ".");
					}
				}
				catch (Exception ex){
					ex.printStackTrace();
					TTT.log.warning("Failed to register player ban for " + t.getName());
				}
			}
			else
				p.sendMessage(ChatColor.DARK_PURPLE + "You have been automatically removed from the round " +
						"because your karma has fallen below " + TTT.plugin.getConfig().getInt("karma-kick"));
		}
		else
			TTT.log.warning("Could not remove \"" + t.getName() + "\" from round \"" + t.getWorld() +
					"\" because an instance of the player could not be created.");
	}
}
