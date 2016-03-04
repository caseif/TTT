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

package net.caseif.ttt.util.helper.gamemode;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.util.Constants;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ContributorListHelper {

    private Map<String, Set<UUID>> contributors = new HashMap<>();
    private InputStream stream = null;

    public ContributorListHelper(InputStream in) {
        this.stream = in;
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
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }

    private void read() {
        String[] lines = readIn().split("\n");
        for (String line : lines) {
            int commentIndex = line.indexOf('#');
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            String[] entry = line.split(" ");
            if (entry.length == 2) {
                String key = entry[0];
                try {
                    UUID value = UUID.fromString(entry[1]);
                    if (contributors.containsKey(key)) {
                        contributors.get(key).add(value);
                    } else {
                        Set<UUID> set = new HashSet<>();
                        set.add(value);
                        contributors.put(key, set);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private boolean hasRole(UUID uuid, String role) {
        return contributors.containsKey(role) && contributors.get(role).contains(uuid);
    }

    public String getContributorString(Player player) {
        UUID uuid = player.getUniqueId();
        String str = "";
        if (hasRole(uuid, Constants.Contributor.DEVELOPER)) {
            str += ", " + TTTCore.locale.getLocalizable("fragment.special.dev")
                    .withPrefix(Constants.Color.TRAITOR).localizeFor(player) + "," + Constants.Color.INFO;
        } else if (hasRole(uuid, Constants.Contributor.ALPHA_TESTER)) {
            str += ", " + TTTCore.locale.getLocalizable("fragment.special.tester.alpha")
                    .withPrefix(Constants.Color.TRAITOR).localizeFor(player) + "," + Constants.Color.INFO;
        }
        return str;
    }

}
