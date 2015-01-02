/*
 * TTT
 * Copyright (c) 2013-2015, Maxim Roncacé <mproncace@lapis.blue>
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
package net.amigocraft.ttt;

import net.amigocraft.mglib.api.LogLevel;

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
			URL url = new URL("http://amigocraft.net/plugins/TTT/checkbuild.php?v=" + Main.plugin.getDescription().getVersion());
			URLConnection conn = url.openConnection();
			conn.connect();
			if (conn instanceof HttpURLConnection){
				HttpURLConnection http = (HttpURLConnection) conn;
				response = http.getResponseCode();
				isr = new InputStreamReader(url.openStream());
				br = new BufferedReader(isr);
				String status = br.readLine();
				if (status.equals("STABLE")){
					if (Config.VERBOSE_LOGGING){
						Main.mg.log(Main.locale.getMessage("stable-build"), LogLevel.INFO);
					}
				}
				else if (status.equals("UNSTABLE")){
					if (Config.UNSTABLE_BUILD_WARNING){
						Main.mg.log(Main.ANSI_RED + Main.locale.getMessage("unstable-build") + Main.ANSI_WHITE, LogLevel.WARNING);
					}
					Main.stability = "unstable";
				}
				else if (status.equals("PRE")){
					Main.mg.log(Main.ANSI_RED + Main.locale.getMessage("prerelease") + Main.ANSI_WHITE, LogLevel.INFO);
					Main.stability = "pre";
				}
				else {
					if (Config.UNKNOWN_BUILD_WARNING){
						Main.mg.log(Main.ANSI_RED + Main.locale.getMessage("unknown-build") + Main.ANSI_WHITE, LogLevel.WARNING);
					}
					Main.stability = "unknown";
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		finally {
			try {
				if (br != null){
					br.close();
				}
				if (isr != null){
					isr.close();
				}
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}

}
