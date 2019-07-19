package com.example.distancecalculator;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TABLE_CONTENT = "tblDistance";
    public static final String TABLE_UPDATEDD_CONTENT = "tblUpdatedDistance";
    public static final String TABLE_CAPTURED_CONTENT = "tblCapturedLocations";
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_ROW_ID    =  "rowid";
    private static final String COL_TIME_STAMP    =  "timeStamp";
    private static final String COL_PATH_CODE    =  "pathcode";

    public static String MyLock = "Lock";
    protected SQLiteDatabase database;



    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

//        db.execSQL("create table student(id INTEGER PRIMARY KEY AUTOINCREMENT ,name varchar )");
        db.execSQL("CREATE TABLE tblUpdatedDistance  ( rowid  INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  latitude VARCHAR, longitude VARCHAR, timeStamp VARCHAR , pathcode VARCHAR )");


        db.execSQL("CREATE TABLE tblDistance ( rowid INTEGER PRIMARY KEY AUTOINCREMENT," +
                " latitude VARCHAR, longitude VARCHAR, timeStamp VARCHAR, pathcode VARCHAR )");
        db.execSQL("CREATE TABLE tblCapturedLocations (  rowid INTEGER PRIMARY KEY AUTOINCREMENT," +
                " latitude VARCHAR, longitude VARCHAR, timeStamp VARCHAR, pathcode VARCHAR )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void insertContent(LatLangDo latLangDo) {
        synchronized ( MyLock) {
            SQLiteStatement stmtInsert = null, stmtUpdate = null, stmtRecCount = null;
            if (latLangDo != null) {
                try {
                    openTransaction();

                    String insertQuery = "INSERT INTO " + TABLE_CONTENT + "(" + COL_LATITUDE + ","
                            + COL_LONGITUDE + "," + COL_TIME_STAMP +"," + COL_PATH_CODE + ")" + " VALUES (?,?,?,?)";
                    stmtInsert = getSqlStatement(insertQuery);
                    stmtInsert.bindDouble(1, latLangDo.latitude);
                    stmtInsert.bindDouble(2, latLangDo.longitude);
                    stmtInsert.bindLong(3, latLangDo.timeStamp);
                    stmtInsert.bindString(4, latLangDo.pathcode);
                    stmtInsert.executeInsert();

                    setTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeTransaction();
                    if (stmtInsert != null)
                        stmtInsert.close();
                }
            }
        }
    }
    public void insertCapturedLocations(LatLangDo latLangDo) {
        synchronized ( MyLock) {
            SQLiteStatement stmtInsert = null, stmtUpdate = null, stmtRecCount = null;
            if (latLangDo != null) {
                try {
                    openTransaction();

                    String insertQuery = "INSERT INTO " + TABLE_CAPTURED_CONTENT + "(" + COL_LATITUDE + "," + COL_LONGITUDE + ","
                            + COL_TIME_STAMP +"," + COL_PATH_CODE + ")" + " VALUES (?,?,?,?)";
                    stmtInsert = getSqlStatement(insertQuery);
                    stmtInsert.bindDouble(1, latLangDo.latitude);
                    stmtInsert.bindDouble(2, latLangDo.longitude);
                    stmtInsert.bindLong(3, latLangDo.timeStamp);
                    stmtInsert.bindString(4, latLangDo.pathcode);
                    stmtInsert.executeInsert();

                    setTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeTransaction();
                    if (stmtInsert != null)
                        stmtInsert.close();
                }
            }
        }
    }
    public void insertContents(Vector<LatLangDo> latLangDo) {
        synchronized ( MyLock) {
            SQLiteStatement stmtInsert = null, stmtUpdate = null, stmtRecCount = null;
            if (latLangDo != null && latLangDo.size()>0) {
                try {
                    openTransaction();

                    String insertQuery = "INSERT INTO " + TABLE_UPDATEDD_CONTENT + "(" + COL_LATITUDE + ","
                            + COL_LONGITUDE + "," + COL_TIME_STAMP +"," + COL_PATH_CODE + ")" + " VALUES (?,?,?,?)";
                    stmtInsert = getSqlStatement(insertQuery);
                    for(int i = 0;i<latLangDo.size();i++)
                    {
                        stmtInsert.bindDouble(1, latLangDo.get(i).latitude);
                        stmtInsert.bindDouble(2, latLangDo.get(i).longitude);
                        stmtInsert.bindLong(3, latLangDo.get(i).timeStamp);
                        stmtInsert.bindString(4, latLangDo.get(i).pathcode);
                        stmtInsert.executeInsert();

                    }


                    setTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeTransaction();
                    if (stmtInsert != null)
                        stmtInsert.close();
                }
            }
        }
    }

    public Vector<LatLangDo> getAllContents() {
        Vector<LatLangDo> vecFiles = new Vector<LatLangDo>();
        synchronized ( MyLock) {
            Cursor cursor = null;
            try {
                openDataBaseForReading();
                String query = "SELECT * FROM " + TABLE_CONTENT;
                cursor = getCursor(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        LatLangDo latLangDo = new LatLangDo();
                        latLangDo.latitude = cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE));
                        latLangDo.longitude = cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE));
                        latLangDo.rowid     =   cursor.getInt(cursor.getColumnIndex(COL_ROW_ID));
                        latLangDo.timeStamp     =   cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP));
                        latLangDo.pathcode     =   cursor.getString(cursor.getColumnIndex(COL_PATH_CODE));

                        vecFiles.add(latLangDo);

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return vecFiles;
    }

    public  HashMap<String, Vector<LatLangDo>>  getContents(int limit) {
        Vector<LatLangDo> vecFiles = new Vector<LatLangDo>();
        HashMap<String, Vector<LatLangDo>> hm= new HashMap<String, Vector<LatLangDo>>();
        synchronized ( MyLock) {
            Cursor cursor = null;
            try {
                openDataBaseForReading();
                String query = "SELECT * FROM " + TABLE_CONTENT+" ORDER BY "+COL_PATH_CODE+" ASC LIMIT "+limit;
                cursor = getCursor(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        LatLangDo latLangDo = new LatLangDo();
                        latLangDo.latitude  = cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE));
                        latLangDo.longitude = cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE));
                        latLangDo.rowid     = cursor.getInt(cursor.getColumnIndex(COL_ROW_ID));
                        latLangDo.timeStamp     = cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP));
                        latLangDo.pathcode     =   cursor.getString(cursor.getColumnIndex(COL_PATH_CODE));
                        String key =latLangDo.pathcode;

                        if(!hm.containsKey(key))
                        {
                            vecFiles = new Vector<LatLangDo>();
                            hm.put(key, new Vector<LatLangDo>());
                        }

                        vecFiles = hm.get(key);
                        vecFiles.add(latLangDo);

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return hm;
    }
    public  HashMap<String, Vector<LatLng>>  getAllPaths() {
        Vector<LatLng> vecFiles = new Vector<LatLng>();
        HashMap<String, Vector<LatLng>> hm= new HashMap<String, Vector<LatLng>>();
        synchronized ( MyLock) {
            Cursor cursor = null;
            try {
                openDataBaseForReading();
                String query = "SELECT * FROM " + TABLE_UPDATEDD_CONTENT+" ORDER BY "+COL_PATH_CODE+" , "+COL_TIME_STAMP;
                cursor = getCursor(query, null);
                if (cursor.moveToFirst()) {

                    do {
                        LatLng latLangDo = new LatLng( cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE)));
                        String key =cursor.getString(cursor.getColumnIndex(COL_PATH_CODE));

                        if(!hm.containsKey(key))
                        {
                            vecFiles = new Vector<LatLng>();
                            hm.put(key, vecFiles);
                        }

                        vecFiles = hm.get(key);
                        vecFiles.add(latLangDo);

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return hm;
    }

    private void openDataBaseForReading()  throws SQLException
    {
        if(database == null || !database.isOpen())
            database = getReadableDatabase();
    }

    public void deleteAllRecords() {
        synchronized ( MyLock) {
            try {
                openDataBase();
                String query = "DELETE FROM " + TABLE_CONTENT;
                excuteQuery(query);
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                closeDatabase();
            }
        }
    }

    public Vector<LatLangDo> getAllContentsFromUpdatedTable() {
        Vector<LatLangDo> vecFiles = new Vector<LatLangDo>();
        synchronized ( MyLock) {
            Cursor cursor = null;
            try {
                openDataBaseForReading();
                String query = "SELECT * FROM " + TABLE_UPDATEDD_CONTENT;
                cursor = getCursor(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        LatLangDo latLangDo = new LatLangDo();
                        latLangDo.latitude = cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE));
                        latLangDo.longitude = cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE));
                        latLangDo.rowid     =   cursor.getInt(cursor.getColumnIndex(COL_ROW_ID));
                        latLangDo.timeStamp     =   cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP));
                        latLangDo.pathcode     =   cursor.getString(cursor.getColumnIndex(COL_PATH_CODE));


                        vecFiles.add(latLangDo);

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return vecFiles;
    }
    public Vector<LatLangDo> getAllContentsFromCapturedLocations() {
        Vector<LatLangDo> vecFiles = new Vector<LatLangDo>();
        synchronized ( MyLock) {
            Cursor cursor = null;
            try {
                openDataBaseForReading();
                String query = "SELECT * FROM " + TABLE_CAPTURED_CONTENT;
                cursor = getCursor(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        LatLangDo latLangDo = new LatLangDo();
                        latLangDo.latitude = cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE));
                        latLangDo.longitude = cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE));
                        latLangDo.rowid     =   cursor.getInt(cursor.getColumnIndex(COL_ROW_ID));
                        latLangDo.timeStamp     =   cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP));
                        latLangDo.pathcode     =   cursor.getString(cursor.getColumnIndex(COL_PATH_CODE));


                        vecFiles.add(latLangDo);

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return vecFiles;
    }
    public Vector<LatLng> getCapturedLatLng() {
        Vector<LatLng> vecFiles = new Vector<LatLng>();
        synchronized ( MyLock) {
            Cursor cursor = null;
            try {
                openDataBaseForReading();
                String query = "SELECT * FROM " + TABLE_CAPTURED_CONTENT;
                cursor = getCursor(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        LatLng latLangDo = new LatLng(cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE)));

                        vecFiles.add(latLangDo);

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return vecFiles;
    }
    public void deleteAllRecordsFromUpdatedTable() {
        synchronized ( MyLock) {
            try {
                openDataBase();
                String query = "DELETE FROM " + TABLE_UPDATEDD_CONTENT;
                excuteQuery(query);
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                closeDatabase();
            }
        }
    }
    public void deleteAllRecordsFromCapturedTable() {
        synchronized ( MyLock) {
            try {
                openDataBase();
                String query = "DELETE FROM " + TABLE_CAPTURED_CONTENT;
                excuteQuery(query);
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                closeDatabase();
            }
        }
    }
    public void deleteAllRecords(int rowId,String pathCode)
    {
        synchronized ( MyLock)
        {
            try
            {
                openDataBase();
                String query = "DELETE FROM " + TABLE_CONTENT +" WHERE "+COL_PATH_CODE+" =  "+pathCode+" AND "+COL_ROW_ID +" <="+rowId;
                excuteQuery(query);
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                closeDatabase();
            }
        }
    }
    public void deleteAllRecordsNew(Vector<LatLangDo> vec,String pathCode)
    {
        synchronized ( MyLock)
        {
            if(vec!=null && vec.size()>0)
            {
                StringBuilder strRowIds =new StringBuilder("");

                for (int i = 0;i<vec.size();i++)
                {
                    strRowIds=strRowIds.append(vec.get(i).rowid+",");
                }
                strRowIds.setLength(Math.max(strRowIds.length() - 1, 0));
//                strRowIds.replace(strRowIds.capacity()-1,strRowIds.capacity()-1,"");
                try
                {
                    openDataBase();
//                    String query = "DELETE FROM " + TABLE_CONTENT +" WHERE "+COL_PATH_CODE+" =  "+pathCode+" AND "+COL_ROW_ID +" <="+rowId;
                    String query = "DELETE FROM " + TABLE_CONTENT +" WHERE "+COL_PATH_CODE+" =  "+pathCode+" AND "+COL_ROW_ID +" in ("+strRowIds+")";
                    excuteQuery(query);
                } catch (Exception e) {
                    e.getLocalizedMessage();
                } finally {
                    closeDatabase();
                }
            }

        }
    }
    public Vector<LatLng> getLatLngs() {
        Vector<LatLng> vecFiles = new Vector<LatLng>();
        synchronized (MyLock) {
            Cursor cursor = null;
            try {
                openDataBase();
                String query = "SELECT * FROM " + TABLE_UPDATEDD_CONTENT;
                cursor = getCursor(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        LatLng  latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE) ));
                        vecFiles.add(latLng);

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return vecFiles;
    }

    public HashMap<String, RouteDO> getAllRoutes()
    {
        HashMap<String, RouteDO>  hm = new HashMap<String, RouteDO> ();
        RouteDO routeDO = new RouteDO();
        synchronized (MyLock) {
            Cursor cursor = null;
            try {
                openDataBase();
//                String query = "SELECT * FROM " + TABLE_CAPTURED_CONTENT+" Order by "+COL_TIME_STAMP+" ASC "  ;
                String query = "select max("+COL_TIME_STAMP+")-min("+COL_TIME_STAMP+"), "+COL_PATH_CODE+",count(*) from " + TABLE_CAPTURED_CONTENT+
                        " group  by "+COL_PATH_CODE  ;
                cursor = getCursor(query, null);

                boolean isFirstLocation = true;

                Location previousLocation,CurrentLocation;
                previousLocation = new Location("Previous");
                CurrentLocation = new Location("Current");


                float  distance = 0.0f ;

                if (cursor.moveToFirst()) {

                    do {

                        routeDO = new RouteDO();
                        routeDO.timeEllapse = cursor.getLong(0);
                        routeDO.pathcode= cursor.getString(1);
                        routeDO.noOfCapturedRecords= cursor.getLong(2);
                        hm.put(routeDO.pathcode,routeDO);
                    } while (cursor.moveToNext());
                }
                closeDatabase();
                if(hm!=null && hm.keySet()!=null && hm.keySet().size()>0)
                {
                    HashMap<String,Vector<LatLng>> hmPaths = new HashMap<String,Vector<LatLng>>();
                    hmPaths= getAllPaths();
                    if(hmPaths!=null && hmPaths.keySet()!=null && hmPaths.keySet().size()>0) {
                        Iterator iterator = hm.keySet().iterator();
                        while (iterator.hasNext())
                        {
                            String key=(String) iterator.next();
                            RouteDO temp = hm.get(key);
                            temp.path = hmPaths.get(key);
                            temp.travelledDistance = getPathDistance(temp.path);
                        }

                    }

                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return hm;
    }
    private float getPathDistance(Vector<LatLng> vec)
    {
        float distance = 0;
        if (vec != null && vec.size() > 1)
            for (int i = 0; i < (vec.size() - 1); i++) {

                Location startPoint = new Location("locationA");
                startPoint.setLatitude(vec.get(i).latitude);
                startPoint.setLongitude(vec.get(i).longitude);

                Location endPoint = new Location("locationA");
                endPoint.setLatitude(vec.get(i + 1).latitude);
                endPoint.setLongitude(vec.get(i + 1).longitude);

                distance = distance + startPoint.distanceTo(endPoint);
            }
        return distance;
    }
    public Object[] getDistanceWithLatLngs(int pathcode) {
        Object object [] = new Object[2];
        Vector<LatLng> vecFiles = new Vector<LatLng>();
        synchronized (MyLock) {
            Cursor cursor = null;
            try {
                openDataBase();
                String query = "SELECT * FROM " + TABLE_UPDATEDD_CONTENT+" where pathcode = "+pathcode;
                cursor = getCursor(query, null);

                boolean isFirstLocation = true;

                Location previousLocation,CurrentLocation;
                previousLocation = new Location("Previous");
                CurrentLocation = new Location("Current");


                float  distance = 0.0f ;

                if (cursor.moveToFirst()) {
                    do {

                        LatLng  latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE) ));
                        if(isFirstLocation)
                        {
                            CurrentLocation.setLatitude(latLng.latitude);
                            CurrentLocation.setLongitude(latLng.longitude);
                            previousLocation.setLatitude(latLng.latitude);
                            previousLocation.setLongitude(latLng.longitude);

                            isFirstLocation =false;
                        }
                        else
                        {
                            CurrentLocation.setLatitude(latLng.latitude);
                            CurrentLocation.setLongitude(latLng.longitude);
                            distance= distance+previousLocation.distanceTo(CurrentLocation);

                        }

                        vecFiles.add(latLng);

                    } while (cursor.moveToNext());
                }
                object[0] = distance;
                object[1] = vecFiles;
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return object;
    }
    public Object[] getEllapsedTime(int pathcode) {
        Object object [] = new Object[4];
        Vector<LatLng> vecFiles = new Vector<LatLng>();
        long endingTimeStamp=0, startingTimeStamp=0;
        synchronized (MyLock) {
            Cursor cursor = null;
            try {
                openDataBase();
                String query = "SELECT * FROM " + TABLE_CAPTURED_CONTENT+" where pathcode = "+pathcode;
                cursor = getCursor(query, null);

                boolean isFirstLocation = true;

                Location previousLocation,CurrentLocation;
                previousLocation = new Location("Previous");
                CurrentLocation = new Location("Current");


                float  distance = 0.0f ;

                if (cursor.moveToFirst()) {
                    do {

                        LatLng  latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex(COL_LATITUDE)),
                                cursor.getDouble(cursor.getColumnIndex(COL_LONGITUDE) ));
                        if(isFirstLocation)
                        {
                            CurrentLocation.setLatitude(latLng.latitude);
                            CurrentLocation.setLongitude(latLng.longitude);
                            previousLocation.setLatitude(latLng.latitude);
                            previousLocation.setLongitude(latLng.longitude);
                            startingTimeStamp = cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP));
                            endingTimeStamp= cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP));

                            isFirstLocation =false;
                        }
                        else
                        {
                            CurrentLocation.setLatitude(latLng.latitude);
                            CurrentLocation.setLongitude(latLng.longitude);
                            distance= distance+previousLocation.distanceTo(CurrentLocation);

                        }
                        if(endingTimeStamp <cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP)))
                        {
                            endingTimeStamp = cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP));

                        }
                        if(startingTimeStamp >cursor.getLong(cursor.getColumnIndex(COL_TIME_STAMP)))
                        {
                            startingTimeStamp = cursor.getLong (cursor.getColumnIndex(COL_TIME_STAMP));

                        }

                        vecFiles.add(latLng);

                    } while (cursor.moveToNext());
                }
                object[0] = distance;
                object[1] = vecFiles;
                object[2] = startingTimeStamp;
                object[3] = endingTimeStamp;
            } catch (Exception e) {
                e.getLocalizedMessage();
            } finally {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                closeDatabase();
            }
        }
        return object;
    }

    protected  void openTransaction() throws SQLException
    {
        if(database == null || !database.isOpen())
            database = getWritableDatabase();
        database.beginTransaction();
    }

    protected  void openDataBase() throws SQLException
    {
//        database = DatabaseHelper.openDataBase();
        database =getWritableDatabase();
    }

    protected  void closeDatabase()
    {
        if(database != null && database.isOpen())
            database.close();
    }

    protected  void closeTransaction()
    {
        if(database != null && database.isOpen())
        {
            database.endTransaction();
            database.close();
        }
    }

    protected void setTransaction()
    {
        database.setTransactionSuccessful();
    }

    protected SQLiteStatement getSqlStatement(String sqlQuery)
    {
        SQLiteStatement statement = database.compileStatement(sqlQuery);
        return statement;
    }
    protected Cursor getCursor(String rawQuery, String[] selArgs)
    {
        return database.rawQuery(rawQuery, selArgs);
    }

    protected void excuteQuery(String rawQuery)
    {
        database.execSQL(rawQuery);
    }


}
