package com.example.tk_employee.myapplication;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.distancecalculator.GpsUtils;
import com.example.distancecalculator.RouteDO;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Vector;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    RouteDO routeDO =new RouteDO();
    public Vector<LatLng> path= new Vector<LatLng>();

    GpsUtils gpsUtils=null;
    private long time;
    private float distance;
    private TextView tvTime;
    private TextView tvDistance;
    private static String API_KEY = "AIzaSyAZnalksXcmp7aNCRrTtDxGRHaWvT5Ny3A";

    private String pathCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        tvTime = (TextView)findViewById(R.id.tvTime);
        tvDistance = (TextView)findViewById(R.id.tvDistance);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        addPathToMap();
    }

    private void addPathToMap()
    {
        time = (Long) getIntent().getSerializableExtra("TIME");
        pathCode = (String) getIntent().getSerializableExtra("CODE");
        distance = (float) getIntent().getSerializableExtra("DISTANCE");
        gpsUtils = GpsUtils.getInstance(MapsActivity.this,API_KEY);

        new Thread( new Runnable() {
                    @Override
                    public void run() {
                        path =   gpsUtils.getPathForCode(Integer.parseInt(pathCode));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvTime = (TextView)findViewById(R.id.tvTime);
                                tvDistance = (TextView)findViewById(R.id.tv);
                                // Obtain the SupportMapFragment and get notified when the map is ready to be used.

                                tvDistance.setText(getDistanceInMeters(distance)+"");
                                tvTime.setText(getTime(time )+"");

                                if(path!=null &&path.size() > 0)
                                {

                                    LatLng[] copyArr = new LatLng[ path.size()];
                                     path.copyInto(copyArr);
                                    mMap.addPolyline((new PolylineOptions())
                                            .add(copyArr).width(7).color(Color.RED)
                                            .geodesic(true));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(copyArr[0],
                                            17));

                                }
                            }
                        });
                    }
                }
        ).start();



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
