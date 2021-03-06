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

import java.util.ArrayList;
import java.util.List;

/**
 * Static utility class for collections-related functionality.
 */
public final class CollectionsHelper {

    private CollectionsHelper() {
    }

    public static String prettyList(List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toString());
            if (i < list.size() - 2) {
                sb.append(", ");
            } else if (i == list.size() - 2) {
                if (list.size() > 2) {
                    sb.append(",");
                }
                sb.append(" and ");
            }
        }
        return sb.toString();
    }

    public static List<String> formatLore(String str) {
        final int lineLength = 36;
        List<String> list = new ArrayList<>();
        String current = "";
        for (String s : str.split(" ")) {
            if (current.length() + s.trim().length() + 1 > lineLength) {
                list.add(current);
                current = "";
            } else if (!current.isEmpty()) {
                current += " ";
            }
            current += s.trim();
        }
        if (!current.isEmpty()) {
            list.add(current);
        }
        return list;
    }

}
