package com.istl.samples.faceverification.utils;

public class Utils {

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
