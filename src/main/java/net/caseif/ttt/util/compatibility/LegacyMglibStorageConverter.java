/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2019, Max Roncace <me@caseif.net>
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

package net.caseif.ttt.util.compatibility;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.helper.platform.MaterialHelper;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Static utility class for maintaining compatibility with MGLib's storage format.
 *
 * @author Max Roncace
 */
public final class LegacyMglibStorageConverter {

    private LegacyMglibStorageConverter() {
    }

    public static void convertArenaStore() {
        File arenaStore = new File(TTTCore.getPlugin().getDataFolder(), "arenas.yml");
        if (arenaStore.exists()) {
            TTTCore.log.info("Converting legacy arena store - please wait");
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(arenaStore);
                int count = 0;
                for (String arenaId : yaml.getKeys(false)) {
                    ConfigurationSection cs = yaml.getConfigurationSection(arenaId);
                    String displayName = cs.contains("displayname") ? cs.getString("displayname") : arenaId;
                    if (!cs.contains("world")) {
                        TTTCore.log.warning("Arena with ID \"" + arenaId + "\" is missing required world attribute - "
                                + "cannot convert");
                        continue;
                    }
                    String world = cs.getString("world");
                    ArrayList<Location3D> spawnPoints = new ArrayList<>();
                    if (!cs.contains("spawns")) {
                        TTTCore.log.warning("Arena with ID \"" + arenaId + "\" does not contain spawn point "
                                + "definitions - cannot convert");
                        continue;
                    }
                    ConfigurationSection spawns = cs.getConfigurationSection("spawns");
                    for (String spawnOrdinal : spawns.getKeys(false)) {
                        ConfigurationSection spawn = spawns.getConfigurationSection(spawnOrdinal);
                        if (!(spawn.contains("x") && spawn.contains("y") && spawn.contains("z"))) {
                            TTTCore.log.warning("Spawn " + spawnOrdinal + " for arena with ID \"" + arenaId
                                    + "\" does not contain position data - cannot convert");
                            continue;
                        }
                        double x = spawn.getDouble("x", Double.NaN);
                        double y = spawn.getDouble("y", Double.NaN);
                        double z = spawn.getDouble("z", Double.NaN);
                        if (x != x || y != y || z != z) {
                            TTTCore.log.warning("Spawn " + spawnOrdinal + " for arena with ID \"" + arenaId
                                    + "\" contains malformed location data - cannot convert");
                            continue;
                        }
                        spawnPoints.add(new Location3D(world, x, y, z));
                    }
                    if (spawnPoints.isEmpty()) {
                        TTTCore.log.warning("Arena with ID \"" + arenaId + "\" does not contain spawn point "
                                + "definitions - cannot convert");
                        continue;
                    }
                    int j = 1;
                    String finalId = arenaId;
                    while (TTTCore.mg.getArena(finalId).isPresent()) {
                        finalId = finalId + "-converted" + (j > 1 ? j : "");
                        j++;
                    }
                    if (arenaId.contains("-converted")) {
                        TTTCore.log.warning("Converted arena \"" + finalId + "\" with new ID \"" + arenaId + "\"");
                    }
                    Location3D[] spawnArr = new Location3D[spawnPoints.size()];
                    spawnPoints.toArray(spawnArr);
                    Arena arena = TTTCore.mg.createBuilder(Arena.class).id(finalId).displayName(displayName)
                            .spawnPoints(spawnArr).boundary(Boundary.INFINITE).build();
                    count++;
                }
                TTTCore.log.info("Successfully converted " + count + " legacy arenas");
                try {
                    Files.move(arenaStore, new File(TTTCore.getPlugin().getDataFolder(), "arenas.yml.old"));
                } catch (IOException ex) {
                    TTTCore.log.severe("Failed to rename old arenas.yml file - you may need to do this manually");
                    ex.printStackTrace();
                }
            } catch (InvalidConfigurationException | IOException ex) {
                ex.printStackTrace();
                TTTCore.log.severe("Failed to convert legacy arena store!");
            }
        }
    }

    public static void convertLobbyStore() {
        File arenaStore = new File(TTTCore.getPlugin().getDataFolder(), "lobbies.yml");
        if (arenaStore.exists()) {
            TTTCore.log.info("Converting legacy lobby sign store - please wait");
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(arenaStore);

                int count = 0;

                outer:
                for (String lobbyOrdinal : yaml.getKeys(false)) {
                    ConfigurationSection cs = yaml.getConfigurationSection(lobbyOrdinal);
                    String[] required = new String[]{"world", "x", "y", "z", "arena", "type", "number"};
                    for (String str : required) {
                        if (!cs.contains(str)) {
                            TTTCore.log.warning("Lobby sign at ordinal " + lobbyOrdinal + " is missing required "
                                    + "attribute \"" + str + "\" - cannot convert");
                            continue outer;
                        }
                    }

                    String world = cs.getString("world");
                    int x = cs.getInt("x", Integer.MIN_VALUE);
                    int y = cs.getInt("y", Integer.MIN_VALUE);
                    int z = cs.getInt("z", Integer.MIN_VALUE);
                    String arenaId = cs.getString("arena");
                    String type = cs.getString("type");
                    int number = cs.getInt("number", Integer.MIN_VALUE);
                    if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE || z == Integer.MIN_VALUE
                            || number == Integer.MIN_VALUE) {
                        TTTCore.log.warning("Malformed location data for lobby sign at ordinal " + lobbyOrdinal
                                + " - cannot convert");
                        continue;
                    }

                    Optional<Arena> arena = TTTCore.mg.getArena(arenaId);
                    if (!arena.isPresent()) {
                        TTTCore.log.warning("Cannot get arena \"" + arenaId + "\" for lobby sign at ordinal "
                                + lobbyOrdinal + " - cannot convert");
                        continue;
                    }
                    Location l = new Location(Bukkit.getWorld(world), x, y, z);
                    if (!MaterialHelper.instance().isWallSign(l.getBlock().getType())
                            && !MaterialHelper.instance().isStandingSign(l.getBlock().getType())) {
                        l.getBlock().setType(Material.OAK_WALL_SIGN);
                    }
                    Location3D loc = new Location3D(world, x, y, z);
                    switch (type) {
                        case "STATUS":
                            arena.get().createStatusLobbySign(loc);
                            break;
                        case "PLAYERS":
                            arena.get().createChallengerListingLobbySign(loc, number);
                            break;
                        default:
                            TTTCore.log.warning("Invalid type \"" + type + "\" for lobby sign at ordinal "
                                    + lobbyOrdinal + " - cannot convert");
                            break;
                    }
                    count++;
                }
                TTTCore.log.info("Successfully converted " + count + " legacy lobby signs");
                try {
                    Files.move(arenaStore, new File(TTTCore.getPlugin().getDataFolder(), "lobbies.yml.old"));
                } catch (IOException ex) {
                    TTTCore.log.severe("Failed to rename old lobbies.yml file - you may need to do this manually");
                    ex.printStackTrace();
                }
            } catch (InvalidConfigurationException | IOException ex) {
                ex.printStackTrace();
                TTTCore.log.severe("Failed to convert legacy lobby store!");
            }
        }
    }

}
