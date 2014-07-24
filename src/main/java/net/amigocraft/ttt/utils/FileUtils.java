package net.amigocraft.ttt.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;

public class FileUtils {

	// world checking method from Multiverse
	public static boolean isWorld(String worldName){
		File[] files = new File(Bukkit.getWorldContainer(), worldName).listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File file, String name){
				return name.equalsIgnoreCase("level.dat");
			}
		});
		if (files != null && files.length > 0){
			return true;
		}
		return false;
	}

	public static void copyFile(File sourceLocation, File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()){
			if (!targetLocation.exists()){
				targetLocation.mkdir();
			}
			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++){
				copyFile(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		}
		else {
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}
}
