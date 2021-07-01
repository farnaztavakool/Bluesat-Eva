package com.bluesat.eva;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DBhelper extends SQLiteOpenHelper {

    public String databaseName = "location.sql";
    public static final String TABLE = "eva";
    public static final String COLOMN_ALTITUDE = "altitude";
    public static final String COLOMN_LONGTITUDE = "longtitude";
    public static final String COLOMN_LATITUDE = "latitude";
    public static final String COLOMN_TIME = "time";
    public static final String COLUMN_RECORD_TYPE = "record_type";
    private int numberOfRecords = 0;

    private DBhelper(Context c, String filename) {
        super(c, databaseFileLocation( c, filename ) ,null,1);
    }

    public static DBhelper createInstance(Context c) {
        String filename = generateFilename();

        DBhelper helper = new DBhelper( c, filename);;
        helper.databaseName = filename;

        return helper;
    }

    private static String generateFilename() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        return String.format(
                Locale.ENGLISH,
                "%04d%02d%02d_%02d%02d_datapoints.sql",
                calendar.get( Calendar.YEAR ),
                calendar.get( Calendar.MONTH ),
                calendar.get( Calendar.DAY_OF_MONTH ),
                calendar.get( Calendar.HOUR_OF_DAY ),
                calendar.get( Calendar.MINUTE ) );
    }

    public static String databaseFileLocation(Context c, String filename) {
        File root = c.getExternalFilesDir(null);
        // File eva = new File( root, "EVA" );

        if( !root.exists() ) {
            root.mkdir();
        }

        File db = new File(root, filename);

        return db.getAbsolutePath();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + TABLE +
                "(" + COLOMN_ALTITUDE + " INTEGER,"+
                COLOMN_LATITUDE + " INTEGER,"+
                COLOMN_LONGTITUDE + " INTEGER,"+
                COLOMN_TIME + " STRING," +
                COLUMN_RECORD_TYPE + " CHAR(1)" +
                ")";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+TABLE);
        onCreate(db);
    }

    public void insertLocation(double altitude, double longtitude, double latitude, String time, boolean isSnapshot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLOMN_ALTITUDE,altitude);
        cv.put(COLOMN_LONGTITUDE,longtitude);
        cv.put(COLOMN_LATITUDE,latitude);
        cv.put(COLOMN_TIME,time);
        if( isSnapshot ) {
            cv.put( COLUMN_RECORD_TYPE, "M" );
        } else {
            cv.put( COLUMN_RECORD_TYPE, "A" );
        }
        db.insert(TABLE,null,cv);

        this.numberOfRecords++;
    }

    public int getNumberOfRecords(){
        return this.numberOfRecords;
    }
}
