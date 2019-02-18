package com.katalon.plugin.smart_xpath.util;

public class StringUtils {
	public static String getStandardPath(String path) {
		return path.replace("\\", "/");
	}
}
