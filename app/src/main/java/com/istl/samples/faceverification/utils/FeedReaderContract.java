package com.istl.samples.faceverification.utils;

import android.provider.BaseColumns;

public class FeedReaderContract {

	private FeedReaderContract() {
	}

	public static class FeedEntry implements BaseColumns {
		public static final String TABLE_NAME = "entry";
		public static final String SUBJECT_ID = "subject";
		public static final String SUBJECT_TEMPLATE = "template";
	}

}
