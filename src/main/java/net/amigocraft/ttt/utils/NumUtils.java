package net.amigocraft.ttt.utils;

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
