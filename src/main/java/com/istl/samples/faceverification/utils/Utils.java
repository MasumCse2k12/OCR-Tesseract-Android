package com.istl.samples.faceverification.utils;

import java.util.List;

import android.os.Environment;

public class Utils {

	public static String imeiNo ="";
	public static String countryName ="";
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	public static String getMessage(Throwable th) {
		if (th == null) throw new NullPointerException("exception");
		return th.getMessage() != null ? th.getMessage() : th.toString();
	}

	public static String combinePath(String... folders) {
		String path = "";
		for (String folder : folders) {
			path = path.concat(FILE_SEPARATOR).concat(folder);
		}
		return path;
	}
}
