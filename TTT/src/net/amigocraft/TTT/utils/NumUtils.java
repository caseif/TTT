package net.amigocraft.TTT.utils;

public class NumUtils {
	
	public static boolean isInt(String s){
		try {
			Integer.parseInt(s);
			return true;
		}
		catch (NumberFormatException ex){
			return false;
		}
	}
	
}
