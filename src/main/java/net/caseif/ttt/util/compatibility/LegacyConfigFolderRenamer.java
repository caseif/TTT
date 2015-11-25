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
package net.caseif.ttt.util.compatibility;

import net.caseif.ttt.TTTCore;

import org.bukkit.Bukkit;

import java.io.File;

/**
 * Static utility class for maintaining compatibility with the old data folder.
 *
 * @author Max Roncace
 */
public class LegacyConfigFolderRenamer {

    public static void renameLegacyFolder() {
        final File old = new File(Bukkit.getWorldContainer() + File.separator + "plugins", "Trouble In Terrorist Town");
        if (old.exists() && !TTTCore.getPlugin().getDataFolder().exists()) {
            TTTCore.getInstance().logWarning("info.plugin.compatibility.rename");
            try {
                old.renameTo(TTTCore.getPlugin().getDataFolder());
            } catch (Exception ex) {
                TTTCore.getInstance().logWarning("error.plugin.folder-rename");
                ex.printStackTrace();
            }
        }
    }

}
