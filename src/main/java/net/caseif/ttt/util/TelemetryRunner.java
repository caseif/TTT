/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2017, Max Roncace <me@caseif.net>
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

package net.caseif.ttt.util;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.config.ConfigKey;
import net.caseif.ttt.util.constant.TelemetryKey;
import net.caseif.ttt.util.helper.data.TelemetryStorageHelper;
import net.caseif.ttt.util.helper.math.ByteHelper;

import net.caseif.flint.FlintCore;
import net.caseif.jtelemetry.JTelemetry;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Runner class for telemetry data submission.
 */
public class TelemetryRunner implements Runnable {

    private static final int TICKS_PER_HOUR = 60 * 60 * 20;
    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    private static final String UUID_FILE_NAME = "telemetry/uuid.dat";
    private static final String TIMESTAMP_FILE_NAME = "telemetry/timestamp.dat";

    private static final String TELEMETRY_SERVER = "http://telemetry.caseif.net/ttt.php";

    private final JTelemetry jt = new JTelemetry(TELEMETRY_SERVER);

    public TelemetryRunner() {
        Bukkit.getScheduler().runTaskTimer(TTTCore.getPlugin(), this, 0L, TICKS_PER_HOUR);
    }

    @Override
    public void run() {
        if (shouldRun()) {
            JTelemetry.Payload payload = constructPayload();

            try {
                writeRunTime();
                JTelemetry.HttpResponse response = payload.submit();
                if (response.getStatusCode() / 100 != 2) { // not 2xx response code
                    TTTCore.log.warning("Telemetry server responded with non-success status code ("
                            + response.getStatusCode() + " " + response.getMessage() + "). Please report this.");
                }
            } catch (IOException ex) {
                throw new RuntimeException("Failed to submit telemetry data to remote server", ex);
            }
        }
    }

    private static boolean shouldRun() {
        try {
            File tsFile = new File(TTTCore.getPlugin().getDataFolder(), TIMESTAMP_FILE_NAME);
            if (!tsFile.exists()) {
                writeRunTime();
                return false;
            }

            byte[] bytes = new byte[8];
            try (FileInputStream is = new FileInputStream(tsFile)) {
                int readBytes = is.read(bytes);
                if (readBytes < 8) {
                    TTTCore.log.warning("Telemetry timestamp file is malformed - regenerating");
                    Files.delete(tsFile.toPath());
                    writeRunTime();
                    return false;
                }
                long timestamp = ByteHelper.bytesToLong(bytes);
                return (System.currentTimeMillis() - timestamp) / 1000 >= SECONDS_PER_DAY;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read timestamp file - not submitting telemetry data", ex);
        }
    }

    private static void writeRunTime() throws IOException {
        File tsFile = new File(TTTCore.getPlugin().getDataFolder(), TIMESTAMP_FILE_NAME);
        if (tsFile.exists()) {
            Files.delete(tsFile.toPath());
        }

        Files.createDirectories(tsFile.getParentFile().toPath());
        Files.createFile(tsFile.toPath());

        try (FileOutputStream os = new FileOutputStream(tsFile)) {
            os.write(ByteHelper.longToBytes(System.currentTimeMillis()));
        }
    }

    private static UUID getUuid() throws IOException {
        File uuidFile = new File(TTTCore.getPlugin().getDataFolder(), UUID_FILE_NAME);
        if (!uuidFile.exists()) {
            Files.createDirectories(uuidFile.getParentFile().toPath());
            //noinspection ResultOfMethodCallIgnored
            Files.createFile(uuidFile.toPath());
        }
        try (FileInputStream is = new FileInputStream(uuidFile)) {
            UUID uuid = null;

            byte[] most = new byte[8];
            byte[] least = new byte[8];
            int read1 = is.read(most);
            int read2 = is.read(least);
            if (read1 == 8 || read2 == 8) {
                uuid = new UUID(ByteHelper.bytesToLong(most), ByteHelper.bytesToLong(least));
            } else {
                TTTCore.log.warning("UUID file is missing or malformed - regenerating");
            }

            try {
                if (uuid == null) {
                    throw new IllegalArgumentException();
                }
                return uuid;
            } catch (IllegalArgumentException ex) {
                UUID newUuid = UUID.randomUUID();
                try (FileOutputStream os = new FileOutputStream(uuidFile)) {
                    os.write(ByteHelper.longToBytes(newUuid.getMostSignificantBits()));
                    os.write(ByteHelper.longToBytes(newUuid.getLeastSignificantBits()));
                }
                return newUuid;
            }
        }
    }

    private JTelemetry.Payload constructPayload() {
        JTelemetry.Payload payload = jt.createPayload();

        UUID uuid;
        try {
            uuid = getUuid();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to get telemetry UUID - not submitting data", ex);
        }

        payload.addData(TelemetryKey.UUID, uuid.toString());
        payload.addData(TelemetryKey.VERSION, TTTCore.getPlugin().getDescription().getVersion());
        payload.addData(TelemetryKey.PLATFORM, Bukkit.getName());
        payload.addData(TelemetryKey.FLINT_API, FlintCore.getApiRevision());
        payload.addData(TelemetryKey.OPERATING_MODE, TTTCore.config.get(ConfigKey.OPERATING_MODE).name());
        payload.addData(TelemetryKey.ARENA_COUNT, TTTCore.mg.getArenas().size());

        TelemetryStorageHelper.RoundSummaryStats stats = TelemetryStorageHelper.getSummaryStats();
        payload.addData(TelemetryKey.ROUND_COUNT, stats.getRoundCount());
        payload.addData(TelemetryKey.ROUND_MEAN_PLAYERS, stats.getMeanPlayerCount());
        payload.addData(TelemetryKey.ROUND_DURATION_MEAN, stats.getDurationMean());
        payload.addData(TelemetryKey.ROUND_DURATION_STD_DEV, stats.getDurationStdDev());
        payload.addData(TelemetryKey.ROUND_INNOCENT_WINS, stats.getInnoWins());
        payload.addData(TelemetryKey.ROUND_TRAITOR_WINS, stats.getTraitorWins());
        payload.addData(TelemetryKey.ROUND_FORFEITS, stats.getForfeits());

        return payload;
    }

}
