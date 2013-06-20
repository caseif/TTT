package net.amigocraft.TTT.localization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import net.amigocraft.TTT.TTT;

import org.bukkit.ChatColor;

public class Localization {

	public String getMessage(String key){
		InputStream is = null;
		is = Localization.class.getClassLoader().getResourceAsStream("net/amigocraft/TTT/localization/" + TTT.lang + ".properties");
		if (is == null){
			try {
				File file = new File(TTT.plugin.getDataFolder() + File.separator + "locales" + File.separator + TTT.lang + ".properties");
				is = new FileInputStream(file);
				if (TTT.plugin.getConfig().getBoolean("verbose-logging"))
					TTT.log.info("Loaded locale from " + file.getAbsolutePath());
			}
			catch (Exception ex){
				is = Localization.class.getClassLoader().getResourceAsStream("net/amigocraft/TTT/localization/enUS.properties");
				if (TTT.plugin.getConfig().getBoolean("verbose-logging"))
					TTT.log.info("Locale defined in config not found in JAR or plugin folder; defaulting to enUS");
			}
		}
		try {
			BufferedReader br;
			String line;
			br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				String[] params = line.split("\\|");
				if (params[0].equalsIgnoreCase(key))
					return params[1].replace("\r", "");
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
		is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/enUS.properties");
		try {
			BufferedReader br;
			String line;
			br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				String[] params = line.split("\\|");
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
