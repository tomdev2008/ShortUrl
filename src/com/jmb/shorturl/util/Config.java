package com.jmb.shorturl.util;

import org.apache.log4j.Logger;

public class Config {

	private static final Logger log = Logger.getLogger(Config.class);

	public static String SQL_USERNAME = "short_url";
	public static String SQL_PASSWORD = "short_url";
	public static String SQL_HOST = "localhost";
	public static String DB_NAME = "short_url";
	public static String TABLE_NAME = "short_url";
	public static Integer CACHE_TIMEOUT_MINUTE = 10;

}