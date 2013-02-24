package net.amigocraft.TTT.utils;

import java.io.File;
import java.io.FilenameFilter;

public class WorldUtils {
	// world checking method from Multiverse
	public static boolean isWorld(File worldFolder){
		File[] files = worldFolder.listFiles(new FilenameFilter(){
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
}
