package com.example.tk_employee.myapplication;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.distancecalculator.DatabaseHelper;
import com.example.distancecalculator.GPSCallback;
import com.example.distancecalculator.GPSErrorCode;
import com.example.distancecalculator.GpsUtils;
import com.example.distancecalculator.PreferenceUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.Vector;

public class MainActivity extends AppCompatActivity  implements GpsUtils.DistanceCalculationListener, OnMapReadyCallback {

    public   GpsUtils gpsUtils;
    private TextView tv;
    private static String API_KEY = "AIzaSyAZnalksXcmp7aNCRrTtDxGRHaWvT5Ny3A";
    private TextView tvError,tvTime;
    private boolean isStart=true;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.tv);
        tvError = (TextView)findViewById(R.id.tvError);
        tvTime = (TextView)findViewById(R.id.tvTime);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        gpsUtils = GpsUtils.getInstance(MainActivity.this,API_KEY);
//        gpsUtils.setListner(new GPSCallback() {
//            @Override
//            public void gotGpsValidationResponse(Object response, GPSErrorCode code) {
//
//            }
//        });

        gpsUtils.setCapturingType(GpsUtils.CAPTURE_DEFAULT);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Clicked",Toast.LENGTH_LONG).show();
                gpsUtils.stop(MainActivity.this);

            }
        });


    }


    /**
     * Here initialising location utils
     */
    private void setUpLocationUtils() {
//        gpsUtils = GpsUtils.getInstance(MainActivity.this,API_KEY);
//        gpsUtils.setListner(new GPSCallback() {
//            @Override
//            public void gotGpsValidationResponse(Object response, GPSErrorCode code) {
//                Log.d("sdf","XXXXXXx");
//            }
//        });
//
//        gpsUtils.setCapturingType(GpsUtils.CAPTURE_DEFAULT);
//        gpsUtils.setCapturingType(482002L);
//        gpsUtils.isDistanceUpdateAutomatic= false;
//        gpsUtils.updateDistanceBasedOnTime(20*1000);
        gpsUtils.start(this);
        tv.setText("");

        tvError.setText("");
        tvTime.setText("");

//        gpsUtils.clearCache();
    }

    public void onClkDisconnect(View view)
    {
        gpsUtils.pause();
    }


    public void onClkDistance(View view)
    {
        if(isStart) {
            onClkConnect(view);
            ((Button)view).setText("STOP");

        }
        else {
            ((Button)view).setText("START");
            gpsUtils.stop(this);
        }
        isStart = !isStart;
    }

    public void onClkConnect(View view)
    {
        setUpLocationUtils();
    }

    public void onClkRestart(View view) {
        gpsUtils.restart();

    }

    public void onClkLocations(View view)
    {
        tv.setText(gpsUtils.getLocations().size()+"  "+gpsUtils.getCapturedLocations().size());
    }


    @Override
    public void onSuccess(float distanceInMeters, long timeEllapsed, Vector<LatLng> path) {
        tv.setText(getDistanceInMeters(distanceInMeters));
        tvTime.setText(getTime(timeEllapsed));


    }

    @Override
    public void onFailure(int errorCode, String reason) {
        tvError.setText(errorCode+" : "+reason+"");

    }

    @Override
    public void onLocationUpdate(Location location)
    {
        if(mMap!=null )
        {
            mMap.clear();
            LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(sydney).title(""));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,
                    17));
        }

    }

    @Override
    public void gotGpsValidationResponse(Object response, GPSErrorCode code) {
        Log.d("Error","gotGpsValidationResponse");

    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    public void onClkMoveToShowRoutes(View view)
    {
        startActivity(new Intent(MainActivity.this, RoutesActivity.class));

    }
    private String getTime(long timeEllapse)
    {
        long h,m,s,remainder;
        timeEllapse=timeEllapse/1000;
        h=timeEllapse/(60*60);
        remainder = timeEllapse%(60*60);
        m=remainder/60;
        s= remainder%60;
        return h+" : "+m+" : "+s;
    }
    private String getDistanceInMeters(float distance )
    {
        long d =(long)distance;
        return ""+(d/1000.0);

    }
}

