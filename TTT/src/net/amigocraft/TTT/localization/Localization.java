package net.amigocraft.TTT.localization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import net.amigocraft.TTT.TTT;

import org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;

public class Localization {

	public String getMessage(String key){
		InputStream is = null;
		is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/" + TTT.lang + ".properties");
		if (is == null){
			try {
				File file = new File(TTT.plugin.getDataFolder() + File.separator + "locales" + File.separator + TTT.lang + ".properties");
				is = new FileInputStream(file);
				if (TTT.plugin.getConfig().getBoolean("verbose-logging"))
					TTT.plugin.log.info("Loaded locale from " + file.getAbsolutePath());
			}
			catch (Exception ex){
				is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/enUS.properties");
				if (TTT.plugin.getConfig().getBoolean("verbose-logging"))
					TTT.plugin.log.info("Locale defined in config not found in JAR or plugin folder; defaulting to enUS");
			}
		}
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(is, writer, "ISO-8859-1");
			is.close();
			String contents = writer.toString();
			String[] lines = contents.split("\n");
			for (int i = 0; i < lines.length; i++){
				String[] params = lines[i].split("\\|");
				if (params[0].equalsIgnoreCase(key))
					return params[1].replace("\r", "");
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
		is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/enUS.properties");
		try {
			IOUtils.copy(is, writer, "ISO-8859-1");
			String contents = writer.toString();
			String[] lines = contents.split("\n");
			for (int i = 0; i < lines.length; i++){
				String[] params = lines[i].split("\\|");
				if (params[0].equalsIgnoreCase(key))
					return params[1].replace("\r", "");
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return ChatColor.DARK_RED + "Could not get message from localization!";
	}
}
