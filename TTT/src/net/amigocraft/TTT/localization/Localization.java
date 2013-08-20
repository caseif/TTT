package net.amigocraft.TTT.localization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

import net.amigocraft.TTT.TTT;

public class Localization {

	public static HashMap<String, String> messages = new HashMap<String, String>();

	public String getMessage(String key){
		String message = messages.get(key);
		if (message != null)
			return message;
		if (messages.get("locale-fail") != null)
			return messages.get("locale-fail");
		return TTT.ANSI_RED + "Could not get message from current locale!" + TTT.ANSI_WHITE;
	}

	public static void initialize(){
		InputStream is = null;
		try {
			File file = new File(TTT.plugin.getDataFolder() + File.separator + "locales" + File.separator +
					TTT.lang + ".properties");
			is = new FileInputStream(file);
			if (TTT.plugin.getConfig().getBoolean("verbose-logging"))
				TTT.log.info("Loaded locale from " + file.getAbsolutePath());
		}
		catch (Exception ex){
			is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/" + TTT.lang +
					".properties");
			if (is == null){
				is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/enUS.properties");
				TTT.log.info("Locale defined in config not found in JAR or plugin folder; defaulting to enUS");
			}
		}
		try {
			BufferedReader br;
			String line;
			br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				String[] params = line.split("\\|");
				if (params.length > 1)
					messages.put(params[0], params[1]);
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
		finally {
			try {is.close();}
			catch (Exception ex){ex.printStackTrace();}
		}
	}
}
