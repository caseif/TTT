package net.amigocraft.ttt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class BuildChecker implements Runnable {

	public static int response = 0;

	public void run(){
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			URL url = new URL("http://amigocraft.net/plugins/TTT/checkbuild.php?v=" +
					Main.plugin.getDescription().getVersion());
			URLConnection conn = url.openConnection();
			conn.connect();
			if (conn instanceof HttpURLConnection){
				HttpURLConnection http = (HttpURLConnection)conn;
				response = http.getResponseCode();
				isr = new InputStreamReader(url.openStream());
				br = new BufferedReader(isr);
				String status = br.readLine();
				if (status.equals("STABLE")){
					if (Variables.VERBOSE_LOGGING)
						Main.log.info(Main.local.getMessage("stable-build"));
				}
				else if (status.equals("UNSTABLE")){
					if (Variables.UNSTABLE_BUILD_WARNING)
						Main.log.warning(Main.ANSI_RED + Main.local.getMessage("unstable-build") + Main.ANSI_WHITE);
					Main.stability = "unstable";
				}
				else if (status.equals("UNKNOWN")){
					if (Variables.UNKNOWN_BUILD_WARNING)
						Main.log.warning(Main.ANSI_RED + Main.local.getMessage("unknown-build") + Main.ANSI_WHITE);
					Main.stability = "unknown";
				}
				else if (status.equals("PRE")){
					Main.log.info(Main.ANSI_RED + Main.local.getMessage("prerelease") + Main.ANSI_WHITE);
					Main.stability = "pre";
				}
			}
		}
		catch (Exception ex){

		}
		finally {
			try {
				br.close();
				isr.close();
			}
			catch (Exception ex){}
		}
	}

}
