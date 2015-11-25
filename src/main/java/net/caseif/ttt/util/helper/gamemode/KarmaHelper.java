/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015, Maxim Roncace <mproncace@lapis.blue>
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
package net.caseif.ttt.util.helper.gamemode;

import static net.caseif.ttt.util.helper.misc.MiscHelper.isTraitor;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants;
import net.caseif.ttt.util.Constants.Color;
import net.caseif.ttt.util.Constants.MetadataTag;

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

    private static final int BASE_KARMA = 1000;

    private static HashMap<UUID, Integer> playerKarma = new HashMap<>();

    public static void saveKarma(Round round) {
        for (Challenger ch : round.getChallengers()) {
            saveKarma(ch);
        }
    }

    public static void saveKarma(Challenger challenger) {
        if (challenger.getMetadata().has(MetadataTag.PURE_SPECTATOR)) {
            return; // we don't want to save karma for a player that's simply spectating
        }

        playerKarma.put(challenger.getUniqueId(), getKarma(challenger));
        File karmaFile = new File(TTTCore.getPlugin().getDataFolder(), "karma.yml");
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
        File karmaFile = new File(TTTCore.getPlugin().getDataFolder(), "karma.yml");
        if (!karmaFile.exists()) {
            TTTCore.getInstance().createFile("karma.yml");
        }
        YamlConfiguration karmaYaml = new YamlConfiguration();
        karmaYaml.load(karmaFile);
        if (karmaYaml.isSet(uuid.toString())) {
            if (karmaYaml.getInt(uuid.toString()) > TTTCore.config.KARMA_MAX) {
                playerKarma.put(uuid, TTTCore.config.KARMA_MAX);
            } else {
                playerKarma.put(uuid, karmaYaml.getInt(uuid.toString()));
            }
        } else {
            playerKarma.put(uuid, TTTCore.config.KARMA_STARTING);
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
            addKarma(challenger, TTTCore.config.KARMA_ROUND_INCREMENT);
            if (!challenger.getMetadata().has(MetadataTag.TEAM_KILLED)) {
                int karmaHeal = TTTCore.config.KARMA_CLEAN_BONUS;
                if (getKarma(challenger) > BASE_KARMA) {
                    if ((TTTCore.config.KARMA_MAX - BASE_KARMA) > 0) {
                        karmaHeal = (int) Math.round(
                                TTTCore.config.KARMA_CLEAN_BONUS * Math.pow((1 / 2), (getKarma(challenger) - BASE_KARMA)
                                                / ((double) (TTTCore.config.KARMA_MAX - BASE_KARMA)
                                                * TTTCore.config.KARMA_CLEAN_HALF)
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
                int penalty = (int) (getKarma(victim) * (damage * TTTCore.config.KARMA_RATIO));
                subtractKarma(damager, penalty);
            } else if (!isTraitor(damager) && isTraitor(victim)) {
                // innocent damaging traitor
                int reward = (int) (TTTCore.config.KARMA_MAX * damage * TTTCore.config.KARMA_TRAITORDMG_RATIO);
                addKarma(damager, reward);
            }
        }
    }

    public static void applyKillKarma(Challenger killer, Challenger victim) {
        if (isTraitor(killer) == isTraitor(killer)) {
            applyDamageKarma(killer, victim, TTTCore.config.KARMA_KILL_PENALTY);
        } else if (!isTraitor(killer)) { // isTraitor(victim) is implied to be true
            int reward
                    = TTTCore.config.KARMA_TRAITORKILL_BONUS * TTTCore.config.KARMA_TRAITORDMG_RATIO * getKarma(victim);
            addKarma(killer, reward);
        }
    }

    private static void handleKick(Challenger player) {
        @SuppressWarnings("deprecation")
        Player p = TTTCore.getPlugin().getServer().getPlayer(player.getName());
        assert p != null;
        player.removeFromRound();
        if (TTTCore.config.KARMA_LOW_BAN) {
            try {
                BanHelper.ban(p.getUniqueId(), TTTCore.config.KARMA_LOW_BAN_MINUTES);
                if (TTTCore.config.KARMA_LOW_BAN_MINUTES < 0) {
                    TTTCore.locale.getLocalizable("info.personal.ban.perm.karma")
                            .withPrefix(Color.INFO)
                            .withReplacements(TTTCore.config.KARMA_LOW_AUTOKICK + "").sendTo(p);
                } else {
                    TTTCore.locale.getLocalizable("info.personal.ban.temp.karma")
                            .withPrefix(Color.INFO)
                            .withReplacements(TTTCore.config.KARMA_LOW_BAN_MINUTES + "",
                                    TTTCore.config.KARMA_LOW_AUTOKICK + "").sendTo(p);
                }
            } catch (InvalidConfigurationException | IOException ex) {
                ex.printStackTrace();
                TTTCore.log.severe(TTTCore.locale.getLocalizable("error.plugin.ban")
                        .withPrefix(Color.ERROR + "").withReplacements(player.getName()).localize());
            }
        } else {
            TTTCore.locale.getLocalizable("info.personal.kick.karma").withPrefix(Color.INFO)
                    .withReplacements(TTTCore.config.KARMA_LOW_AUTOKICK + "").sendTo(p);
        }
    }

    public static int getKarma(Challenger mp) {
        return mp.getMetadata().has(MetadataTag.KARMA)
                ? mp.getMetadata().<Integer>get(MetadataTag.KARMA).get()
                : TTTCore.config.KARMA_STARTING;
    }

    public static void applyDamageReduction(Challenger challenger) {
        int baseKarma = getKarma(challenger) - BASE_KARMA;

        final double a = -2e-6;
        final double b = 7e-4;
        final double strictA = -2.5e-6;
        final double minDamageRed = 0.01;

        double damageRed;
        if (TTTCore.config.KARMA_STRICT) {
            damageRed = (a * Math.pow(baseKarma, 2)) + (b * baseKarma) + (1);
        } else {
            damageRed = (strictA * Math.pow(baseKarma, 2)) + (1);
        }
        if (damageRed <= 0) {
            damageRed = minDamageRed;
        }
        challenger.getMetadata().set(MetadataTag.DAMAGE_REDUCTION, damageRed);
    }

    public static double getDamageReduction(Challenger challenger) {
        return challenger.getMetadata().has(MetadataTag.DAMAGE_REDUCTION)
                ? challenger.getMetadata().<Double>get(MetadataTag.DAMAGE_REDUCTION).get()
                : 1;
    }

    public static void applyKarma(Challenger challenger) {
        int karma = Math.max(getKarma(challenger.getUniqueId()), TTTCore.config.KARMA_LOW_AUTOKICK);
        challenger.getMetadata().set(MetadataTag.KARMA, karma);
        challenger.getMetadata().set(MetadataTag.DISPLAY_KARMA, karma);
    }

    public static void resetKarma(UUID uuid) {
        playerKarma.remove(uuid);
    }

    private static void addKarma(Challenger challenger, int amount) {
        if (challenger.getRound().getLifecycleStage() == Constants.Stage.ROUND_OVER) {
            return;
        }

        int karma = getKarma(challenger);
        if (amount == 0 && TTTCore.config.KARMA_ROUND_TO_ONE) {
            amount = 1;
        }
        if (karma + amount < TTTCore.config.KARMA_MAX) {
            karma += amount;
        } else if (karma < TTTCore.config.KARMA_MAX) {
            karma = TTTCore.config.KARMA_MAX;
        }

        challenger.getMetadata().set(MetadataTag.KARMA, karma);

        if (karma < TTTCore.config.KARMA_LOW_AUTOKICK) {
            handleKick(challenger);
        }

        if (TTTCore.config.KARMA_DEBUG) {
            TTTCore.kLog.info("[TTT Karma Debug] " + challenger.getName() + ": " + (amount > 0 ? "+" : "") + amount
                    + ". " + "New value: " + karma);
        }
    }

    private static void subtractKarma(Challenger challenger, int amount) {
        addKarma(challenger, -amount);
        challenger.getMetadata().set(MetadataTag.TEAM_KILLED, true);
    }

}
