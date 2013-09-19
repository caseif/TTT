package net.amigocraft.TTT;

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
					TTT.plugin.getDescription().getVersion());
			URLConnection conn = url.openConnection();
			conn.connect();
			if (conn instanceof HttpURLConnection){
				HttpURLConnection http = (HttpURLConnection)conn;
				response = http.getResponseCode();
				isr = new InputStreamReader(url.openStream());
				br = new BufferedReader(isr);
				String status = br.readLine();
				if (status.equals("STABLE")){
					if (Variables.verbose_logging)
						TTT.log.info(TTT.local.getMessage("stable-build"));
				}
				else if (status.equals("UNSTABLE")){
					if (Variables.unstable_build_warning)
						TTT.log.warning(TTT.ANSI_RED + TTT.local.getMessage("unstable-build") + TTT.ANSI_WHITE);
					TTT.stability = "unstable";
				}
				else if (status.equals("UNKNOWN")){
					if (Variables.unknown_build_warning)
						TTT.log.warning(TTT.ANSI_RED + TTT.local.getMessage("unknown-build") + TTT.ANSI_WHITE);
					TTT.stability = "unknown";
				}
				else if (status.equals("PRE")){
					TTT.log.info(TTT.ANSI_RED + TTT.local.getMessage("prerelease") + TTT.ANSI_WHITE);
					TTT.stability = "pre";
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
