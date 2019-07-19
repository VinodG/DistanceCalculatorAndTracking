package com.example.distancecalculator;



import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static com.google.android.gms.internal.zzir.runOnUiThread;

public class GpsUtils {

    private static GpsUtils gpsUtills;
    private String TAG = "GPSTrack";
    private long TIME_PERIOD = 5 * 1000;
    private int capturingType = 0;


    private Context context;
    private GPSTrackerService gpsTrackerService;
    private GPSCallback gpsCallback;
    private List<Address> address;
    private LatLng currentLatLng;
    public static DatabaseHelper db;

    //    capturing type
    public static final int CAPTURE_DEFAULT = 0;
    public static final int CAPTURE_ON_TIME_SLOT = 1;
    public static final int CAPTURE_ON_MIN_DISTANCE = 2;


    public static final int ERROR = 0;
    public static final int ERROR_EMPTY_RESPONSE = 1;
    public static final int ERROR_NETWORK_PROVIDER = 2;
    public static final int ERROR_INTERNET_CONNECTION = 3;
    public static final int ERROR_GPS_PROVIDER = 4;
    public static final int ERROR_EXCEPTIONS = 5;
    public static final int ERROR_DISABLED_LOCATIONS_PERMISSIONS = 6;


    //to call api
    private Timer calDisTimer;
    private TimerTask calDisTimerTask;

    private DistanceCalculationListener distanceCalculationListener;
    private float totalDistance;//final distance;
    private static String API_KEY = "AIzaSyAZnalksXcmp7aNCRrTtDxGRHaWvT5Ny3A";
    private int TIME_TO_UPDATE_DISTANCE = 3 * 60 * 1000;  // 3 minutes
    public boolean isDistanceUpdateAutomatic = true;

    PreferenceUtils preferenceUtils ;


    public static  int  PATH_CODE = 1;

    private GpsUtils(Context context) {
        this.context = context;
        gpsTrackerService = new GPSTrackerService(context);
        preferenceUtils = new PreferenceUtils(context);

    }

    public void updateDistanceBasedOnTime(int timeToUpdateDistance) {
        TIME_TO_UPDATE_DISTANCE = timeToUpdateDistance;
    }

    public void stop(DistanceCalculationListener distanceCalculationListener) {
        pause();
        getDistance(distanceCalculationListener);
    }

    public void setCapturingType(long timeperiod) {
        TIME_PERIOD = timeperiod;
        this.capturingType = CAPTURE_ON_TIME_SLOT;
    }


    public void setCapturingType(int capturingtype) {
        this.capturingType = capturingtype;
    }

    public synchronized static GpsUtils getInstance(Context context, String api_key) {
        if (gpsUtills == null) {
            gpsUtills = new GpsUtils(context);
            API_KEY = api_key;

            db = new DatabaseHelper(context, "distance.db", null, 1);

        }

        return gpsUtills;
    }


    //    private void calculateTravelledDistance()
    public void getDistance(final DistanceCalculationListener listener) {
        boolean isGpsProviderEnabled;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGpsProviderEnabled) {
            boolean isNetworkProviderEnabled;
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkProviderEnabled) {
                if (isInternetConnectionAvailable()) {
                    calculateDistanceFromDB(listener);
                } else {
                    sendError(10000, "NO INTERNET CONNECTION");


                }

            } else {
                sendError(10000, "NETWORK IS DISABLED");

            }
        } else {
            sendError(10000, "GPS IS DISABLED");
        }
    }


    public void setListner(GPSCallback gpsCallback) {
        this.gpsCallback = gpsCallback;
    }


    public void isGpsProviderEnabled() {
        boolean isGpsProviderEnabled;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsProviderEnabled) {

            distanceCalculationListener.gotGpsValidationResponse(isGpsProviderEnabled, GPSErrorCode.EC_GPS_PROVIDER_ENABLED);
        } else {
            distanceCalculationListener.gotGpsValidationResponse(isGpsProviderEnabled, GPSErrorCode.EC_GPS_PROVIDER_NOT_ENABLED);
        }
    }


    public void isNetworkProviderEnabled() {
        boolean isNetworkProviderEnabled;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkProviderEnabled) {
            distanceCalculationListener.gotGpsValidationResponse(isNetworkProviderEnabled, GPSErrorCode.EC_NETWORK_PROVIDER_ENABLED);
        } else {
            distanceCalculationListener.gotGpsValidationResponse(isNetworkProviderEnabled, GPSErrorCode.EC_NETWORK_PROVIDER_NOT_ENABLED);
        }
    }

    public boolean isInternetConnectionAvailable() {
        boolean isNetworkConnectionAvailable = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null)
            isNetworkConnectionAvailable = activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED;
        if (isNetworkConnectionAvailable) {
            distanceCalculationListener.gotGpsValidationResponse(isNetworkConnectionAvailable, GPSErrorCode.EC_INTERNETCONNECTION_AVAILABLE);
        } else {
            distanceCalculationListener.gotGpsValidationResponse(isNetworkConnectionAvailable, GPSErrorCode.EC_INTERNETCONNECTION_NOT_AVAILABLE);
        }
        return isNetworkConnectionAvailable;
    }


    //**********************************Location Updates Methods*******************************************************//
    public void connectGoogleApiClient() {
        gpsTrackerService.connectGoogleApiClient();
    }

    public void pause() {
        gpsTrackerService.disConnectGoogleApiClient();
    }

    public GoogleApiClient getGoogleApiClient() {
        return gpsTrackerService.getGoogleApiClient();
    }

    public void startLocationUpdates() {
        if (getGoogleApiClient().isConnected()) {
            gpsTrackerService.startLocationUpdates();
        }
    }

    public void stopLocationUpdates() {
        gpsTrackerService.stopLocationUpdates();
    }

    public void start(final DistanceCalculationListener listener) {
        totalDistance = 0;
        distanceCalculationListener =listener;
        preferenceUtils.saveInt(PreferenceUtils.ROUTE_PATH_CODE,
                preferenceUtils.getIntFromPreference(PreferenceUtils.ROUTE_PATH_CODE,0)+1);

//        clear();
        restart();



        if (isDistanceUpdateAutomatic) {
            if (calDisTimer != null) {
                calDisTimer.cancel();
                calDisTimerTask.cancel();
            }

            calDisTimer = new Timer();
            calDisTimerTask = new TimerTask() {
                @Override
                public void run() {
                    getDistance(listener);
                }
            };
            calDisTimer.schedule(calDisTimerTask, 0, TIME_TO_UPDATE_DISTANCE);


        }
    }

    public void restart() {
        isDeviceConfiguredProperly();
        connectGoogleApiClient();
        startLocationUpdates();


        if (capturingType == CAPTURE_ON_TIME_SLOT)
            gpsTrackerService.startTimer();
    }

    public void stopTimer() {
        gpsTrackerService.stoptimertask();
    }

    public void clearCache() {
        db.deleteAllRecords();
        db.deleteAllRecordsFromUpdatedTable();
        db.deleteAllRecordsFromCapturedTable();
    }


    public class GPSTrackerService extends Service implements
            LocationListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
        private static final String TAG = "GPSTrack";
        // private static String lattitude, longitude;
        private String mLastUpdateTime;
        private Context mContext;
        private LocationRequest mLocationRequest;
        private GoogleApiClient mGoogleApiClient;
        private Location mCurrentLocation;
        private Timer timer;
        private TimerTask timerTask;
        private Handler handler = new Handler();


        public GPSTrackerService(Context mContext) {
            this.mContext = mContext;
            createLocationRequest();
            createGoogleApiClient();
        }

        public void createLocationRequest() {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(AppConstants.INTERVAL);
            mLocationRequest.setFastestInterval(AppConstants.FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
//            return super.oDnStartCommand(intent, flags, startId);
            return START_STICKY;
        }

        public void createGoogleApiClient() {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        @TargetApi(Build.VERSION_CODES.M)
        public void startLocationUpdates() {
            if ((Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
//                return;
                sendError(ERROR_DISABLED_LOCATIONS_PERMISSIONS, "Check Permission For Location");
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

        }

        public void stopLocationUpdates() {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        }

        public void connectGoogleApiClient() {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }

        public void disConnectGoogleApiClient() {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
                if (timer != null) {
                    timer.cancel();
                    timerTask.cancel();
                }
                stopSelf();

            }
        }

        public GoogleApiClient getGoogleApiClient() {
            return mGoogleApiClient;
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        public void startTimer() {
            if (timer != null) {
                timer.cancel();
                timerTask.cancel();
            }

            timer = new Timer();
            timerTask = new TimerTask() {
                public void run() {
                    if (mCurrentLocation != null) {
                        LatLangDo latLangDo = new LatLangDo();
                        latLangDo.latitude = mCurrentLocation.getLatitude();
                        latLangDo.longitude = mCurrentLocation.getLongitude();
                        latLangDo.timeStamp = Calendar.getInstance().getTimeInMillis();
                        latLangDo.pathcode = preferenceUtils.getIntFromPreference(PreferenceUtils.ROUTE_PATH_CODE,0)+"";

                        if(capturingType == CAPTURE_ON_TIME_SLOT)
                        {
                            Location location =new Location("UpdatedLocation");
                            location.setLatitude(latLangDo.latitude);
                            location.setLongitude(latLangDo.longitude);
                            distanceCalculationListener.onLocationUpdate(location);

                            db.insertContent(latLangDo);
                            db.insertCapturedLocations(latLangDo);
                        }

                    } else {
                        Log.d("LATLNG", "NULL is captured");
                    }
                }
            };

            timer.schedule(timerTask, TIME_PERIOD, TIME_PERIOD);
        }

        public void stoptimertask() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            Log.e("onLocationChanged", "location at " + mLastUpdateTime.toString());

            if (mCurrentLocation != null) {
                LatLangDo latLangDo = new LatLangDo();
                latLangDo.latitude = mCurrentLocation.getLatitude();
                latLangDo.longitude = mCurrentLocation.getLongitude();
                latLangDo.timeStamp = Calendar.getInstance().getTimeInMillis();
                latLangDo.pathcode = ""+
                        preferenceUtils.getIntFromPreference(PreferenceUtils.ROUTE_PATH_CODE,0);

                if(capturingType == CAPTURE_DEFAULT ) {
                    distanceCalculationListener.onLocationUpdate(location);

                    db.insertContent(latLangDo);
                    db.insertCapturedLocations(latLangDo);
                }
            }

        }

        @Override
        public void onConnected(Bundle arg0) {
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
        }


    }


    public void isDeviceConfiguredProperly() {
        boolean isGpsFeatureAvailbleOnDevice = isGpsFeatureAvailableOnDevice();
        if (isGpsFeatureAvailbleOnDevice) {
            boolean checkGooglePlayServices = checkGooglePlayServices();
            if (checkGooglePlayServices) {
                distanceCalculationListener.gotGpsValidationResponse(checkGooglePlayServices, GPSErrorCode.EC_DEVICE_CONFIGURED_PROPERLY);
            } else {
                distanceCalculationListener.gotGpsValidationResponse(checkGooglePlayServices, GPSErrorCode.EC_GOOGLEPLAY_SERVICES_UPDATE_REQUIRED);
            }

        } else {
            distanceCalculationListener.gotGpsValidationResponse(isGpsFeatureAvailbleOnDevice, GPSErrorCode.EC_GPS_HARDWARE_SETUP_NOTAVAILABLE_ONDEVICE);
        }
    }
    private boolean isGpsFeatureAvailableOnDevice() {
        PackageManager packageManager = context.getPackageManager();
        boolean hasGps = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        return hasGps;
    }
    private boolean checkGooglePlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (status != ConnectionResult.SUCCESS) {
            return false;
        } else {
            return true;
        }
    }
    private void sendError(final int errorCode,final String reason){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                distanceCalculationListener.onFailure(errorCode, reason);

            }
        });

    }


    public void calculateDistanceFromDB(final DistanceCalculationListener distanceListener) {
        distanceCalculationListener = distanceListener;
        StringBuilder latLangsStr = new StringBuilder();
        final Vector<LatLangDo> vecLatLangDos ;
        final HashMap<String, Vector<LatLangDo>> hm =   db.getContents(100);
        final String pathcode ;
        if(hm!=null  && hm.keySet().size()>0)
        {
            Set set = hm.keySet();
            Iterator iterator = set.iterator();
            if(iterator.hasNext()) {
                pathcode  =  (String)iterator.next();
                vecLatLangDos = hm.get(pathcode);



                if (vecLatLangDos != null && vecLatLangDos.size() > 0 && vecLatLangDos.size() >= 2) {
                    for (int i = 0; i < vecLatLangDos.size(); i++) {
                        LatLangDo latLangDo = vecLatLangDos.get(i);
                        if (latLangDo != null) {
                            latLangsStr.append(latLangDo.latitude);
                            latLangsStr.append(",");
                            latLangsStr.append(latLangDo.longitude);
                            if (i != (vecLatLangDos.size() - 1))
                                latLangsStr.append("|");
                        }
                    }
                    final String url = "https://roads.googleapis.com/v1/snapToRoads?path=" +
                            latLangsStr.toString() + "&interpolate=true&key="+API_KEY;//AIzaSyBb33af1Zt7ZoEy1h7J60FGj79yFCHkn7g";

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String finalstring = getData(url,distanceListener);
                            if(finalstring !=null && finalstring.equalsIgnoreCase("{}\n"))
                                sendError(ERROR_EMPTY_RESPONSE,"Empty Response-{}");
                            else{
                                parseString(finalstring,pathcode);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                final HashMap<String, Vector<LatLangDo>> hm =   db.getContents(100);
                                if(hm!=null  && hm.keySet().size()>0)
                                {
                                    Set set = hm.keySet();
                                    Iterator iterator = set.iterator();
                                    Vector<LatLangDo> vec ;
                                    if(iterator.hasNext()) {
                                        vec = hm.get(iterator.next());

                                        if (vec.size() > 2) {
                                            calculateDistanceFromDB(distanceListener);
                                        }
                                    }}
                            }

                        }

                        private void parseString(String responseStr, String pathcode) {
                            try {

                                if (responseStr != null && responseStr.length() > 0) {
                                    JSONObject jsonObject = new JSONObject(responseStr);
                                    JSONObject jsonObjectError = jsonObject.optJSONObject("error");
                                    final String message;
                                    if (jsonObjectError != null) {
                                        message = jsonObjectError.optString("message");
                                        sendError(ERROR, message);
                                    }




                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if(TextUtils.isEmpty(responseStr))
                            {
                                sendError(ERROR_EMPTY_RESPONSE, "Response is Empty");

                            }else {
                                Vector<LatLangDo> latLangDos = new Vector<>();
                                try {
                                    if (responseStr != null && responseStr.length() > 0) {
                                        JSONObject jsonObject = new JSONObject(responseStr);
                                        JSONArray snapRoadPointsArray = jsonObject.optJSONArray("snappedPoints");
                                        if (snapRoadPointsArray != null && snapRoadPointsArray.length() > 0) {

                                            for (int i = 0; i < snapRoadPointsArray.length(); i++) {
                                                JSONObject jsonLatLang = snapRoadPointsArray.optJSONObject(i);
                                                if (jsonLatLang != null) {
                                                    JSONObject jsonLocationObj = jsonLatLang.optJSONObject("location");
                                                    if (jsonLocationObj != null) {
                                                        LatLangDo latLangDo = new LatLangDo();
                                                        latLangDo.latitude = jsonLocationObj.optDouble("latitude");
                                                        latLangDo.longitude = jsonLocationObj.optDouble("longitude");
                                                        latLangDo.pathcode = pathcode;
                                                        PATH_CODE = (!TextUtils.isEmpty(pathcode)) ? Integer.parseInt(pathcode) : 1;
                                                        latLangDo.timeStamp  = Calendar.getInstance().getTimeInMillis();;
                                                        latLangDos.add(latLangDo);
                                                    }
                                                }
                                            }
                                        }
                                        else
                                        {
                                            if(snapRoadPointsArray ==null)
                                                sendError(ERROR_EMPTY_RESPONSE,responseStr);
                                        }
                                        float distance = 0;
                                        if (latLangDos != null && latLangDos.size() > 1)
                                            for (int i = 0; i < (latLangDos.size() - 1); i++) {

                                                Location startPoint = new Location("locationA");
                                                startPoint.setLatitude(latLangDos.get(i).latitude);
                                                startPoint.setLongitude(latLangDos.get(i).longitude);

                                                Location endPoint = new Location("locationA");
                                                endPoint.setLatitude(latLangDos.get(i + 1).latitude);
                                                endPoint.setLongitude(latLangDos.get(i + 1).longitude);

                                                distance = distance + startPoint.distanceTo(endPoint);
                                            }
                                        totalDistance = totalDistance + distance;
                                        db.insertContents(latLangDos);
//                                        db.deleteAllRecords((vecLatLangDos.get(vecLatLangDos.size() - 1).rowid) - 1,vecLatLangDos.get(vecLatLangDos.size() - 1).pathcode);
                                        db.deleteAllRecordsNew( vecLatLangDos,vecLatLangDos.get(vecLatLangDos.size() - 1).pathcode);

                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                calculateDistanceFromDB(distanceCalculationListener);
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendError(10000,e.getMessage());
                                }
                            }

                        }




                    }).start();


                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Object objects[] =db.getDistanceWithLatLngs(PATH_CODE);
                            Object objectsCaptured[] =db.getEllapsedTime(PATH_CODE);
                            clear();
                            distanceCalculationListener.onSuccess((float)objects[0],(long)objectsCaptured[3] -  (long)objectsCaptured[2],(Vector<LatLng>)objects[1]);
                        }
                    });
                }
            }}
    }
    public Vector<LatLng> getPathForCode(int i)
    {
        Object objects[] =db.getDistanceWithLatLngs(i);
        return (Vector<LatLng> )objects[1];
    }
    public interface DistanceCalculationListener
    {
        void onSuccess(float distanceInMeters, long timeEllapsedInMilliSec, Vector<LatLng> path);
        void onFailure(int errorCode,String reason);
        void onLocationUpdate(Location location);
        void gotGpsValidationResponse(Object response, GPSErrorCode code);
    }
    public String getData(String urlSring, DistanceCalculationListener distanceListener)
    {

        HttpURLConnection urlConnection = null;
        String str=null;
        URL url ;
        try {

            url = new URL(urlSring);

            urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();

            str = convertStreamToString(in);
        } catch (Exception e) {

            sendError(ERROR_INTERNET_CONNECTION,e.getMessage());
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
//                urlConnection.c();
            }
        }
        return  str;

    }
    public static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    public void clear()
    {
        db.deleteAllRecords();
//        db.deleteAllRecordsFromUpdatedTable();
//        db.deleteAllRecordsFromCapturedTable();

    }
    public Vector<LatLng> getLocations()
    {
        return  db.getLatLngs();

    }
    public Vector<LatLng> getCapturedLocations()
    {
        return  db.getCapturedLatLng();

    }
    public HashMap<String, RouteDO> getAllRoutes()
    {
        HashMap<String, RouteDO> hm= new HashMap<String, RouteDO>();
        hm=  db.getAllRoutes();
        return  hm;
    }
}