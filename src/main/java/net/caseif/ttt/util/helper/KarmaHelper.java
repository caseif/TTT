/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncacé <mproncace@lapis.blue>
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
package net.caseif.ttt.util.helper;

import static net.caseif.ttt.util.MiscUtil.isTraitor;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.Round;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Static utility class for karma-related functionality.
 */
public class KarmaHelper {

    private static HashMap<UUID, Integer> playerKarma = new HashMap<>();

    public static void saveKarma(Round round) {
        for (Challenger ch : round.getChallengers()) {
            saveKarma(ch);
        }
    }

    public static void saveKarma(Challenger challenger) {
        playerKarma.put(challenger.getUniqueId(), getKarma(challenger));
        File karmaFile = new File(TTTCore.getInstance().getDataFolder(), "karma.yml");
        try {
            if (karmaFile.exists()) {
                YamlConfiguration karmaYaml = new YamlConfiguration();
                karmaYaml.load(karmaFile);
                karmaYaml.set(challenger.getUniqueId().toString(), getKarma(challenger));
                karmaYaml.save(karmaFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void loadKarma(UUID uuid) throws InvalidConfigurationException, IOException {
        File karmaFile = new File(TTTCore.getInstance().getDataFolder(), "karma.yml");
        if (!karmaFile.exists()) {
            TTTCore.getInstance().createFile("karma.yml");
        }
        YamlConfiguration karmaYaml = new YamlConfiguration();
        karmaYaml.load(karmaFile);
        if (karmaYaml.isSet(uuid.toString())) {
            if (karmaYaml.getInt(uuid.toString()) > ConfigHelper.MAX_KARMA) {
                playerKarma.put(uuid, ConfigHelper.MAX_KARMA);
            } else {
                playerKarma.put(uuid, karmaYaml.getInt(uuid.toString()));
            }
        } else {
            playerKarma.put(uuid, ConfigHelper.DEFAULT_KARMA);
        }
    }

    public static int getKarma(UUID uuid) {
        if (!playerKarma.containsKey(uuid)) {
            try {
                loadKarma(uuid);
            } catch (InvalidConfigurationException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return playerKarma.get(uuid);
    }

    public static void allocateKarma(Round round) {
        for (Challenger challenger : round.getChallengers()) {
            addKarma(challenger, ConfigHelper.KARMA_HEAL);
            if (!challenger.getMetadata().has("hasTeamKilled")) {
                int karmaHeal = ConfigHelper.KARMA_CLEAN_BONUS;
                if (getKarma(challenger) > 1000) {
                    if ((ConfigHelper.MAX_KARMA - 1000) > 0) {
                        karmaHeal = (int) Math.round(
                                ConfigHelper.KARMA_CLEAN_BONUS * Math.pow(.5, (getKarma(challenger) - 1000.0)
                                                / ((double) (ConfigHelper.MAX_KARMA - 1000)
                                                * ConfigHelper.KARMA_CLEAN_HALF)
                                )
                        );
                    }
                }
                addKarma(challenger, karmaHeal);
            }
        }
    }

    public static void applyDamageKarma(Challenger damager, Challenger victim, double damage) {
        if (damager != null && victim != null) {
            // team damage
            if (isTraitor(damager) == isTraitor(victim)) {
                int penalty = (int) (getKarma(victim) * (damage * ConfigHelper.DAMAGE_PENALTY));
                subtractKarma(damager, penalty);
            } else if (!isTraitor(damager) && isTraitor(victim)) {
                // innocent damaging traitor
                int reward = (int) (ConfigHelper.MAX_KARMA * damage * ConfigHelper.T_DAMAGE_REWARD);
                addKarma(damager, reward);
            }
        }
    }

    public static void applyKillKarma(Challenger killer, Challenger victim) {
        if (isTraitor(killer) == isTraitor(killer)) {
            applyDamageKarma(killer, victim, ConfigHelper.KILL_PENALTY);
        } else if (!isTraitor(killer)) { // isTraitor(victim) is implied to be true
            int reward = ConfigHelper.TBONUS * ConfigHelper.T_DAMAGE_REWARD * getKarma(victim);
            addKarma(killer, reward);
        }
    }

    private static void handleKick(Challenger player) {
        @SuppressWarnings("deprecation")
        Player p = TTTCore.getInstance().getServer().getPlayer(player.getName());
        if (p != null) {
            player.removeFromRound();
            if (ConfigHelper.KARMA_BAN) {
                BanHelper.ban(p.getUniqueId(), ConfigHelper.KARMA_BAN_TIME);
                if (ConfigHelper.KARMA_BAN_TIME < 0) {
                    TTTCore.locale.getLocalizable("info.personal.ban.perm.karma")
                            .withPrefix(Constants.Color.INFO.toString()).withReplacements(ConfigHelper.KARMA_KICK + "")
                            .sendTo(p);
                } else {
                    TTTCore.locale.getLocalizable("info.personal.ban.temp.karma")
                            .withPrefix(Constants.Color.INFO.toString())
                            .withReplacements(ConfigHelper.KARMA_BAN_TIME + "", ConfigHelper.KARMA_KICK + "").sendTo(p);
                }
            } else {
                TTTCore.locale.getLocalizable("info.personal.kick.karma").withPrefix(Constants.Color.INFO.toString())
                        .withReplacements(ConfigHelper.KARMA_KICK + "").sendTo(p);
            }
        }
    }

    public static int getKarma(Challenger mp) {
        return mp.getMetadata().has("karma") ? mp.getMetadata().<Integer>get("karma").get() : 0;
    }

    public static void applyDamageReduction(Challenger challenger) {
        int baseKarma = getKarma(challenger) - 1000;
        double damageRed =
                ConfigHelper.KARMA_STRICT
                        ? (-2e-6 * Math.pow(baseKarma, 2)) + (7e-4 * baseKarma) + (1)
                        : (-2.5e-6 * Math.pow(baseKarma, 2)) + (1);
        if (damageRed <= 0) {
            damageRed = 0.01;
        }
        challenger.getMetadata().set("damageRed", damageRed);
    }

    public static double getDamageReduction(Challenger challenger) {
        return challenger.getMetadata().has("damageRed") ? challenger.getMetadata().<Double>get("damageRed").get() : 1;
    }

    public static void applyKarma(Challenger challenger) {
        int karma = getKarma(challenger.getUniqueId());
        challenger.getMetadata().set("karma",
                playerKarma.get(challenger.getUniqueId()));
        challenger.getMetadata().set("displayKarma",
                playerKarma.get(challenger.getUniqueId()));
    }

    public static void resetKarma(UUID uuid) {
        playerKarma.remove(uuid);
    }

    private static void addKarma(Challenger challenger, int amount) {
        int karma = getKarma(challenger);
        if (amount == 0 && ConfigHelper.KARMA_ROUND_TO_ONE) {
            amount = 1;
        }
        if (karma + amount < TTTCore.maxKarma) {
            karma += amount;
        } else if (karma < TTTCore.maxKarma) {
            karma = TTTCore.maxKarma;
        }

        challenger.getMetadata().set("karma", karma);

        if (karma < ConfigHelper.KARMA_KICK) {
            handleKick(challenger);
        }

        if (ConfigHelper.KARMA_DEBUG) {
            TTTCore.kLog.info("[TTT Karma Debug] " + challenger.getName() + ": +" + amount + ". "
                    + "New value: " + challenger.getMetadata().get("karma"));
        }
    }

    private static void subtractKarma(Challenger challenger, int amount) {
        addKarma(challenger, -amount);
        challenger.getMetadata().set("hasTeamKilled", true);
    }

}
