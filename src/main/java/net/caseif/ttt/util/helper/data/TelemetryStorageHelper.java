/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016, Max Roncace <me@caseif.net>
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
import net.caseif.ttt.util.Constants;

import net.caseif.flint.round.Round;
import net.obnoxint.xnbt.NBTInputStream;
import net.obnoxint.xnbt.NBTOutputStream;
import net.obnoxint.xnbt.Tag;
import net.obnoxint.xnbt.types.ByteTag;
import net.obnoxint.xnbt.types.CompoundTag;
import net.obnoxint.xnbt.types.IntegerTag;
import net.obnoxint.xnbt.types.ListTag;
import net.obnoxint.xnbt.types.NBTTag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TelemetryStorageHelper {

    private static final String STORE_FILE_NAME = "tel_data.dat";

    private static final String KEY_ROUND_DURATION = "dur";
    private static final String KEY_ROUND_RESULT = "res";

    public static void pushRound(Round round) {
        int duration = round.getMetadata().<Integer>get(Constants.MetadataTag.ROUND_DURATION).get();
        byte result = round.getMetadata().<Byte>get(Constants.MetadataTag.ROUND_RESULT).get();

        File store = getStoreFile();

        List<CompoundTag> tags = store.exists() ? loadStore() : new ArrayList<CompoundTag>();

        CompoundTag newTag = new CompoundTag(null);
        newTag.put(new IntegerTag(KEY_ROUND_DURATION, duration));
        newTag.put(new ByteTag(KEY_ROUND_RESULT, result));
        tags.add(newTag);

        ListTag listTag = new ListTag("root");
        for (NBTTag tag : tags) {
            listTag.add(tag);
        }

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

    private static List<CompoundTag> loadStore() {
        File store = getStoreFile();

        if (!store.exists()) {
            return new ArrayList<>();
        }

        try (NBTInputStream is = new NBTInputStream(new FileInputStream(store))) {
            NBTTag tag = is.readTag();

            if (!(tag instanceof ListTag)) {
                is.close();
                Files.delete(store.toPath());
                throw new IllegalStateException("Root tag of telemetry data store is not a list! This won't do...");
            }

            ListTag list = (ListTag) tag;
            List<CompoundTag> tagList = new ArrayList<>();
            for (NBTTag element : list) {
                //TODO: this shit's broken, but I'm about to switch the NBT library anyway
                if (element.getHeader().getType() != 0x0A) {
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
            int duration = ((IntegerTag) tag.get(KEY_ROUND_DURATION)).getPayload();
            byte result = ((ByteTag) tag.get(KEY_ROUND_RESULT)).getPayload();
            rounds.add(new RoundSummary(duration, result));
        }

        return rounds;
    }

    private static File getStoreFile() {
        return new File(TTTCore.getPlugin().getDataFolder(), STORE_FILE_NAME);
    }

    private static class RoundSummary {

        private final int duration;
        // for reference: 0=inno, 1=traitor, 2=stalemate
        private final byte result;

        private RoundSummary(int duration, byte result) {
            this.duration = duration;
            this.result = result;
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

    }

    public static class RoundSummaryStats {

        private final int roundCount;
        private final float durationMean;
        private final float durationStdDev;
        private final int innoWins;
        private final int traitorWins;
        private final int forfeits;

        private RoundSummaryStats(List<RoundSummary> rounds) {
            this.roundCount = rounds.size();

            int sum = 0;
            int[] results = new int[3];
            for (RoundSummary round : rounds) {
                sum += round.getDuration();
                results[round.getResult()]++;
            }

            durationMean = (float) sum / roundCount;

            float stdDevSum = 0f;
            for (RoundSummary round : rounds) {
                stdDevSum += Math.pow(round.getDuration() - durationMean, 2);
            }
            durationStdDev = stdDevSum / roundCount;

            innoWins = results[0];
            traitorWins = results[1];
            forfeits = results[2];
        }

        public int getRoundCount() {
            return roundCount;
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
