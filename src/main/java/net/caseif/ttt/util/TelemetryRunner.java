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

package net.caseif.ttt.util;

import net.caseif.ttt.TTTCore;

import net.caseif.jtelemetry.JTelemetry;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Runner class for telemetry data submission.
 */
public class TelemetryRunner implements Runnable {

    private static final int TICKS_PER_HOUR = 60 * 60 * 20;
    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    private static final String UUID_FILE_NAME = "uuid.txt";
    private static final String TIMESTAMP_FILE_NAME = "tel_ts.txt";

    private static final String TELEMETRY_SERVER = "http://telemetry.caseif.net/ttt.php";

    private static final String KEY_UUID = "uuid";
    private static final String KEY_VERSION = "version";

    private final JTelemetry jt = new JTelemetry(TELEMETRY_SERVER);

    public TelemetryRunner() {
        Bukkit.getScheduler().runTaskTimer(TTTCore.getPlugin(), this, 0L, TICKS_PER_HOUR);
    }

    @Override
    public void run() {
        if (shouldRun()) {
            JTelemetry.Payload payload = constructPayload();

            try {
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

            String contents = Files.readAllLines(tsFile.toPath(), StandardCharsets.UTF_8).get(0);
            try {
                long timestamp = Long.parseLong(contents);
                return (System.currentTimeMillis() - timestamp) / 1000 >= SECONDS_PER_DAY;
            } catch (NumberFormatException ex) {
                TTTCore.log.warning("Invalid timestamp in telemetry timestamp file - resetting file");
                Files.delete(tsFile.toPath());
                writeRunTime();
                return false;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read timestamp file - not submitting telemetry data");
        }
    }

    private static void writeRunTime() throws IOException {
        File tsFile = new File(TTTCore.getPlugin().getDataFolder(), TIMESTAMP_FILE_NAME);
        if (tsFile.exists()) {
            Files.delete(tsFile.toPath());
        }

        try (FileWriter writer = new FileWriter(tsFile)) {
            writer.write("" + System.currentTimeMillis());
        }
    }

    private static UUID getUuid() throws IOException {
        File uuidFile = new File(TTTCore.getPlugin().getDataFolder(), UUID_FILE_NAME);
        if (!uuidFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            uuidFile.createNewFile();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(uuidFile))) {
            String uuid = reader.readLine();
            try {
                if (uuid == null) {
                    throw new IllegalArgumentException();
                }
                return UUID.fromString(uuid);
            } catch (IllegalArgumentException ex) {
                UUID newUuid = UUID.randomUUID();
                try (FileWriter writer = new FileWriter(uuidFile)) {
                    writer.write(newUuid.toString());
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

        payload.addData(KEY_UUID, uuid.toString());
        payload.addData(KEY_VERSION, TTTCore.getPlugin().getDescription().getVersion());

        return payload;
    }

}