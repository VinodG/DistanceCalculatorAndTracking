package com.example.distancecalculator;

/**
 * Created by ANURAG on 19-01-2018.
 */


public interface GPSCallback {
    void gotGpsValidationResponse(Object response, GPSErrorCode code);
}
