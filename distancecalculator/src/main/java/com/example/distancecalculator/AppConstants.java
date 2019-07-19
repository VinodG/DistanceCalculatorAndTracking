package com.example.distancecalculator;

public class AppConstants {

    public static final String DATABASE_NAME = "distance.sqlite";
    public static final long INTERVAL = 3 * 1000;  //3 seconds.
    public static final long FASTEST_INTERVAL = 3 * 1000;  //3 seconds.
    public static final double DISTANCE_VALIDATION_RANGE = 50;           //50 meters.
    public static final int MAX_RESULTS = 1;

    public static String DATABASE_PATH = "";
    public static boolean DB_HASUpdate = false;
    public static boolean IS_APP_FIRST_INSTALL = false;
//    public static final long THREE_TIME_BW_UPDATES = 1000 * 60 * 3; // 3 minute.
    public static final long THREE_TIME_BW_UPDATES = 1000 * 10 * 1; //10seconds.
    public static final String LAT= "LATITUDE";
    public static final String LNG = "LONGITUDE";


//    public static final int CAPTURE_ON_ = 1;

}
