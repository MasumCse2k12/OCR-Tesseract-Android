package com.istl.samples.faceverification.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FVDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "FVDatabaseHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FaceVerification.db";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE " +
            FeedReaderContract.FeedEntry.TABLE_NAME + " (" +
            FeedReaderContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
            FeedReaderContract.FeedEntry.SUBJECT_ID + " TEXT," +
            FeedReaderContract.FeedEntry.N_ID + " TEXT," +
            FeedReaderContract.FeedEntry.IMEI_NO + " TEXT," +
            FeedReaderContract.FeedEntry.COUNTRY + " TEXT," +
            FeedReaderContract.FeedEntry.SUBJECT_TEMPLATE + " BLOB)";

    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.TABLE_NAME;

    public FVDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_DELETE_TABLE);
        db.execSQL(SQL_CREATE_TABLE);
    }

    public boolean insert(String subjectID,String nid, String imeiNo, String coutry, byte[] template) {
        Log.e(TAG,"mDBHelper.listNIDs()(main) > " + listNIDs());
        if (listSubjectIDs().contains(subjectID) || listNIDs().contains(nid))
            throw new IllegalArgumentException("DB already contains this ID");

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.SUBJECT_ID, subjectID);
        values.put(FeedReaderContract.FeedEntry.N_ID, nid);
        values.put(FeedReaderContract.FeedEntry.IMEI_NO, imeiNo);
        values.put(FeedReaderContract.FeedEntry.COUNTRY, coutry);
        values.put(FeedReaderContract.FeedEntry.SUBJECT_TEMPLATE, template);

        long rowID = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        return rowID != -1;
    }

    public List<String> listNIDs() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = {FeedReaderContract.FeedEntry.N_ID};
        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        List nIDs = new ArrayList<String>();
        while (cursor.moveToNext()) {
            String NID = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.N_ID));
            nIDs.add(NID);
        }
        cursor.close();
        return nIDs;
    }

    public List<String> listSubjectIDs() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = {FeedReaderContract.FeedEntry.SUBJECT_ID};
        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        List subjectIDs = new ArrayList<String>();
        while (cursor.moveToNext()) {
            String subjectID = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.SUBJECT_ID));
            subjectIDs.add(subjectID);
        }
        cursor.close();
        return subjectIDs;
    }

    public byte[] getTemplate(String subjectID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = {FeedReaderContract.FeedEntry.SUBJECT_TEMPLATE};
        String selection = FeedReaderContract.FeedEntry.SUBJECT_ID + " = ?";
        String[] selectionArgs = {subjectID};
        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if (cursor.getColumnCount() > 1) {
            throw new IllegalStateException("DB returned few elements: " + cursor.getColumnCount());
        } else {
            cursor.moveToFirst();
            return cursor.getBlob(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.SUBJECT_TEMPLATE));
        }
    }

}

