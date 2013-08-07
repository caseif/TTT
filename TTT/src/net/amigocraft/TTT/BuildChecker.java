package net.amigocraft.TTT;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class BuildChecker implements Runnable {

	public void run(){
		try {
			URL url = new URL("http://amigocraft.net/plugins/TTT/checkbuild.php?v=" +
					TTT.plugin.getDescription().getVersion());
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String status = in.readLine();
			if (status.equals("STABLE")){
				if (TTT.plugin.getConfig().getBoolean("verbose-logging"))
					TTT.log.info(TTT.local.getMessage("stable-build"));
			}
			else if (status.equals("UNSTABLE")){
				if (TTT.plugin.getConfig().getBoolean("unstable-build-warning"))
					TTT.log.warning(TTT.ANSI_RED + TTT.local.getMessage("unstable-build") + TTT.ANSI_WHITE);
				TTT.unstable = true;
			}
			else if (status.equals("UNKNOWN")){
				if (TTT.plugin.getConfig().getBoolean("unstable-build-warning")){
					TTT.log.warning(TTT.ANSI_RED + TTT.local.getMessage("unknown-build") + TTT.ANSI_WHITE);
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			TTT.log.warning(TTT.local.getMessage("build-check-fail"));
		}
	}

}
