package net.caseif.ttt.util.compatibility;

import net.caseif.ttt.TTTCore;

import org.bukkit.Bukkit;

import java.io.File;

/**
 * Static utility class for maintaining compatibility with the old data folder.
 *
 * @author Max Roncacé
 */
public class LegacyConfigFolderRenamer {

    public static void renameLegacyFolder() {
        final File old = new File(Bukkit.getWorldContainer() + File.separator + "plugins", "Trouble In Terrorist Town");
        if (old.exists() && !TTTCore.getInstance().getDataFolder().exists()) {
            TTTCore.getInstance().logWarning("info.plugin.compatibility.rename");
            try {
                old.renameTo(TTTCore.getInstance().getDataFolder());
            } catch (Exception ex) {
                TTTCore.getInstance().logWarning("error.plugin.folder-rename");
                ex.printStackTrace();
            }
        }
    }

}
