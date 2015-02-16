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
package net.caseif.ttt.util;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContributorsReader {

    private Map<String, Set<String>> contributors = new HashMap<String, Set<String>>();
    private InputStream stream = null;

    public ContributorsReader(InputStream in) {
        this.stream = in;
    }

    public Map<String, Set<String>> getContributors() {
        return contributors;
    }

    private String readIn() {
        char[] buffer = new char[1024];
        StringBuilder builder = new StringBuilder();
        try {
            Reader in = new InputStreamReader(stream, "UTF-8");
            try {
                int piece;
                while ((piece = in.read(buffer, 0, buffer.length)) >= 0) {
                    builder.append(buffer, 0, piece);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public Map<String, Set<String>> read() {
        String[] lines = readIn().split("\n");
        for (String line : lines) {
            int commentIndex = line.indexOf('#');
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            String[] entry = line.split(" ");
            if (entry.length == 2) {
                String key = entry[0];
                String value = entry[1];
                if (contributors.containsKey(key)) {
                    contributors.get(key).add(value);
                } else {
                    Set<String> set = new HashSet<String>();
                    set.add(value);
                    contributors.put(key, set);
                }
            }
        }
        return contributors;
    }

}
