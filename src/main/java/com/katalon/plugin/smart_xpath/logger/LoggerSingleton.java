package com.katalon.plugin.smart_xpath.logger;

public class LoggerSingleton {

    public static void logError(String msg) {
    	System.out.println(msg);
    }

    public static void logError(Throwable e) {
    	System.out.println(e.getMessage());
    }

    public static void logError(Throwable e, String msg) {
    	System.out.println(msg);
    }

    public static void logWarn(String msg) {
    	System.out.println(msg);
    }

    public static void logInfo(String msg) {
        System.out.println(msg);
    }

    public static void logDebug(String msg) {
    	System.out.println(msg);
    }
}