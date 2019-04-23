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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TelemetryCalculationTest {

    private int[] durations = {0, 1, 2, 3, 4, 5};
    private byte[] results  = {0, 0, 0, 1, 1, 2};
    private int[] pCounts   = {0, 1, 2, 3, 4, 5};

    private TelemetryStorageHelper.RoundSummaryStats stats;

    @BeforeAll
    public void initialize() {
        List<TelemetryStorageHelper.RoundSummary> summaries = new ArrayList<>();
        for (int i = 0; i < durations.length; i++) {
            summaries.add(new TelemetryStorageHelper.RoundSummary(durations[i], results[i], pCounts[i]));
        }
        stats = new TelemetryStorageHelper.RoundSummaryStats(summaries);
    }

    @Test
    public void testDurationStats() {
        assertEquals(2.5f, stats.getDurationMean(), 0f);
        assertEquals(1.87f, stats.getDurationStdDev(), 0.01f);
    }

    @Test
    public void testResultStats() {
        assertEquals(3, stats.getInnoWins());
        assertEquals(2, stats.getTraitorWins());
        assertEquals(1, stats.getForfeits());
    }

    @Test
    public void testPlayerCountStats() {
        assertEquals(2.5f, stats.getMeanPlayerCount(), 0f);
    }

}
