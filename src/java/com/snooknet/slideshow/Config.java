package com.snooknet.slideshow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Config {

	public static String getMediaRootDir() {
		return "\\\\Media-pc\\c\\Users\\Snook\\Pictures";
	}

	/** number of days either side of anniversary to allow. */
	public static int getAgeToleranceDays() {
		return 7;
	}

	public static List<String> getMediaSuffixes() {
		List<String> suffixes = new ArrayList<String>(1);
		suffixes.add(".jpg");
		return suffixes;
	}

	public static List<String> getExclusionWildcards() {
		List<String> wildcards = new ArrayList<String>(2);
		wildcards.add("*.picasaoriginals*");
		wildcards.add("*Originals*");
		wildcards.add("*corrupt*");
		wildcards.add("*\\new\\*");
		return wildcards;
	}

	public static String getMediaListFilename() {
		return "C:\\Users\\Snook\\AppData\\Local\\ScreenSaverFileList.json";
	}

	public static int getDisplaySeconds() {
		return 10;
	}

	public static String getFlickPassword() {
		try {
			return new String(Files.readAllBytes(Paths.get("C:\\Users\\Snook\\flickpw.txt")));
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}
