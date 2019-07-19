package com.example.distancecalculator;

import com.example.distancecalculator.LatLangDo;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Vector;

public class RouteDO implements Serializable

{
    public String pathcode  = "";
    public Vector<LatLng> path= new Vector<LatLng>();
    public long timeEllapse = 0;
    public float travelledDistance = 0;
    public long noOfCapturedRecords = 0;

    @Override
    public String toString() {

        return "PathCode: "+pathcode+
                "\n Time :"+getTime(timeEllapse)+",  Distance :  "+getDistanceInMeters(travelledDistance)+", Records : "+noOfCapturedRecords ;
    }
    private String getTime(long timeEllapse)
    {
        long h,m,s,remainder;
        timeEllapse=timeEllapse/1000;
        h=timeEllapse/(60*60);
        remainder = timeEllapse%(60*60);
        m=remainder/60;
        s= remainder%60;
        return h+":"+m+":"+s;

    }
    private String getDistanceInMeters(float distance )
    {
        long d =(long)distance;
        return ""+(d/1000.0);

    }
}
