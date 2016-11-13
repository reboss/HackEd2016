package com.vortecs.reboss.radardetector;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by reboss on 11/12/2016.
 */
public class MyLocationListener implements LocationListener {

    private double latitude;
    private double longitude;

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public double getLat() {
        return latitude;
    }

    public double getLong() {
        return longitude;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
