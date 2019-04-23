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

package net.caseif.ttt.util.helper.data;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.constant.MetadataKey;

import net.caseif.flint.round.Round;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelemetryStorageHelper {

    private static final String STORE_FILE_NAME = "telemetry/rounds.dat";

    private static final String KEY_ROUND_DURATION = "dur";
    private static final String KEY_ROUND_RESULT = "res";
    private static final String KEY_ROUND_PLAYERS = "plc";

    public static void pushRound(Round round) {
        int duration = round.getMetadata().<Integer>get(MetadataKey.Round.ROUND_DURATION).get();
        byte result = round.getMetadata().<Byte>get(MetadataKey.Round.ROUND_RESULT).get();
        int players = round.getMetadata().<Integer>get(MetadataKey.Round.ROUND_PLAYER_COUNT).get();

        File store = getStoreFile();

        List<CompoundTag> tags = store.exists() ? loadStore() : new ArrayList<CompoundTag>();

        Map<String, Tag> tagMap = new HashMap<>();
        tagMap.put(KEY_ROUND_DURATION, new IntTag(KEY_ROUND_DURATION, duration));
        tagMap.put(KEY_ROUND_RESULT, new ByteTag(KEY_ROUND_RESULT, result));
        tagMap.put(KEY_ROUND_PLAYERS, new IntTag(KEY_ROUND_PLAYERS, players));
        CompoundTag newTag = new CompoundTag(null, tagMap);
        tags.add(newTag);

        List<Tag> list = new ArrayList<>();
        for (Tag tag : tags) {
            list.add(tag);
        }
        ListTag listTag = new ListTag("root", CompoundTag.class, list);

        try (NBTOutputStream os = new NBTOutputStream(new FileOutputStream(store))) {
            if (!store.exists()) {
                Files.createFile(store.toPath());
            }
            os.writeTag(listTag);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to save telemetry data store to disk", ex);
        }
    }

    public static RoundSummaryStats getSummaryStats() {
        return new RoundSummaryStats(readAndPopStore());
    }

    @SuppressWarnings("try")
    private static List<CompoundTag> loadStore() {
        File store = getStoreFile();

        if (!store.exists()) {
            return new ArrayList<>();
        }

        try (NBTInputStream is = new NBTInputStream(new FileInputStream(store))) {
            Tag tag = is.readTag();

            if (!(tag instanceof ListTag)) {
                is.close(); // release file
                Files.delete(store.toPath());
                throw new IllegalStateException("Root tag of telemetry data store is not a list!");
            }

            ListTag list = (ListTag) tag;
            List<CompoundTag> tagList = new ArrayList<>();
            for (Tag element : list.getValue()) {
                if (!(element instanceof CompoundTag)) {
                    TTTCore.log.warning("Found non-compound root tag in telemetry data store! Ignoring...");
                    continue;
                }

                tagList.add((CompoundTag) element);
            }

            return tagList;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read telemetry data store", ex);
        }
    }

    private static List<RoundSummary> readAndPopStore() {
        List<RoundSummary> rounds = new ArrayList<>();
        for (CompoundTag tag : loadStore()) {
            int duration = ((IntTag) tag.getValue().get(KEY_ROUND_DURATION)).getValue();
            byte result = ((ByteTag) tag.getValue().get(KEY_ROUND_RESULT)).getValue();
            int players = ((IntTag) tag.getValue().get(KEY_ROUND_PLAYERS)).getValue();
            rounds.add(new RoundSummary(duration, result, players));
        }

        File store = getStoreFile();
        if (store.exists()) {
            try {
                Files.delete(store.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
                TTTCore.log.severe("Failed to delete telemetry database! Attempting to manually erase data...");
                try (FileOutputStream os = new FileOutputStream(store)) {
                    os.write(new byte[0]);
                } catch (IOException exc) {
                    throw new RuntimeException("Failed to erase telemetry database!", exc);
                }
            }
        }

        return rounds;
    }

    private static File getStoreFile() {
        return new File(TTTCore.getPlugin().getDataFolder(), STORE_FILE_NAME);
    }

    static class RoundSummary {

        private final int duration;
        // for reference: 0=inno, 1=traitor, 2=stalemate
        private final byte result;
        private final int playerCount;

        RoundSummary(int duration, byte result, int playerCount) {
            this.duration = duration;
            this.result = result;
            this.playerCount = playerCount;
        }

        private int getDuration() {
            return duration;
        }

        /**
         * Returns a magic number denoting the result of the round. The possible
         * values are as follows:
         *
         * <blockquote>
         *     <strong>0</strong> - innocent victory<br>
         *     <strong>1</strong> - traitor victory<br>
         *     <strong>2</strong> - stalemate (timer ran out)<br>
         * </blockquote>
         *
         * @return The number denoting the result of the round
         */
        private byte getResult() {
            return result;
        }

        private int getPlayerCount() {
            return playerCount;
        }

    }

    public static class RoundSummaryStats {

        private final int roundCount;
        private final float playerCount;
        private final float durationMean;
        private final float durationStdDev;
        private final int innoWins;
        private final int traitorWins;
        private final int forfeits;

        RoundSummaryStats(List<RoundSummary> rounds) {
            this.roundCount = rounds.size();

            int durationSum = 0;
            int playerSum = 0;
            int[] results = new int[3];
            for (RoundSummary round : rounds) {
                durationSum += round.getDuration();
                playerSum += round.getPlayerCount();
                results[round.getResult()]++;
            }

            durationMean = roundCount > 0 ? (float) durationSum / roundCount : 0f;
            playerCount = playerSum > 0 ? (float) playerSum / roundCount : 0f;

            if (roundCount > 0) {
                float stdDevSum = 0f;
                for (RoundSummary round : rounds) {
                    stdDevSum += Math.pow(round.getDuration() - durationMean, 2);
                }
                durationStdDev = (float) Math.sqrt(stdDevSum / (roundCount - 1));
            } else {
                durationStdDev = 0f;
            }

            innoWins = results[0];
            traitorWins = results[1];
            forfeits = results[2];
        }

        public int getRoundCount() {
            return roundCount;
        }

        public float getMeanPlayerCount() {
            return playerCount;
        }

        public float getDurationMean() {
            return durationMean;
        }

        public float getDurationStdDev() {
            return durationStdDev;
        }

        public int getInnoWins() {
            return innoWins;
        }

        public int getTraitorWins() {
            return traitorWins;
        }

        public int getForfeits() {
            return forfeits;
        }

    }

}
