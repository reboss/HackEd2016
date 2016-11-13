package com.vortecs.reboss.radardetector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.drivewyze.EventReceiver;
import com.drivewyze.GPXLocationProvider;
import com.drivewyze.JDRIVE;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.lang.Math;

public class MainActivity extends AppCompatActivity {

    private final String SERVER = "http://hacked2016.herokuapp.com";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GPXLocationProvider gpxLocation;
    private final double ABOUT_ONE_KILOMETER = 0.0085;
    MyLocationListener myLocation;
    boolean GpsPermission;

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(SERVER);
        } catch (URISyntaxException e) {
        }
    }

    private Emitter.Listener onNewFence = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gpxLocation = new GPXLocationProvider(
                new File(getFilesDir(), "master.gpx").getPath());

        setContentView(R.layout.activity_main);
        JDRIVE.instance().initialize(getFilesDir().getPath());
        JDRIVE.instance().setLocationProvider(gpxLocation);

        mSocket.on("new fence", onNewFence);
        mSocket.connect();
        //JDRIVE.instance().run();
    }

    private String GetTheOSUData() {

        // read osreplica.json, check for timed out
        // read timed out object with delete flag set to true
        return new String();
    }

    public void setPhotoRadar(View view) {
        //double[]coordinates = getCoordinates();
        final double[] coordinates = {49.202011, 113.020292};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pushToServer("Photoradar");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        while (thread.isAlive()) {
        }
        Toast.makeText(getApplicationContext(), "Posted message, Thanks for being a good samaritan",
                Toast.LENGTH_LONG).show();
    }

    public void setAccident(View view) {
        //double[]coordinates = getCoordinates();
        final double[] coordinates = {49.202011, 113.020292};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pushToServer("Accident");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        while (thread.isAlive()) {}
        Toast.makeText(getApplicationContext(), "Posted message, Thanks for being a good samaritan",
                Toast.LENGTH_LONG).show();

    }

    @Override
    public  void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GpsPermission = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private double[] getCoordinates() {
        myLocation = new MyLocationListener();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = (LocationListener) myLocation;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return null;
        }
        if (GpsPermission) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10,
                    (LocationListener) locationListener);
            double[] coord = {myLocation.getLat(), myLocation.getLong()};
            return coord;
        }
        else {
            double[] coord = {20.03202902, 100.1212044};
            return coord;
        }

    }

    private void pushToServer(String type) throws IOException, JSONException {

        JSONObject json = makeJSONObject(1, 1, type);
        JSONObject location = makeLocation();

        json.put("location", location);
        mSocket.emit("new data", json);
    }


//    public void setDeviceDetails(String details) {
//
//
//        JDRIVE.instance().addListenerForEvent("myFenceEnterListener", "fence-enter", new EventReceiver() {
//            @Override
//            public void receive(String event, String ts) {
//                // notify user about photo radar
//                // UI event?
//            }
//        });
//
//        // how to remove fences??
//        JDRIVE.instance().osr(new EventReceiver() {
//            @Override
//            public void receive(String event, String ts) {
//                // get the fence and NLR objects from your backend or local store etc.
//                String payload = GetTheOSUData();
//                JDRIVE.instance().osu(payload, ts);
//            }
//        });
//    }


    JSONObject makeJSONObject(int id, int _id, String type) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("_id", id);
        json.put("id", _id);
        json.put("fenceType", type);
        return json;
    }

    JSONObject makeLocation() throws JSONException {
        JSONObject location = new JSONObject();
        location.put("type", "Polygon");
        double[] center = getCoordinates();

        double d = 1000;
        double R = 6371 * 1000;
        double lat = Math.toRadians(center[0]);
        double lon = Math.toRadians(center[1]);

        double northLat = Math.asin(Math.sin(lat) * Math.cos(d / R) + Math.cos(lat) * Math.sin(d / R) * 1);
        double northLon = lon + Math.atan2(0, Math.cos(d / R) - Math.sin(lat) * Math.sin(northLat));
        northLat = Math.toDegrees(northLat);
        northLon = (Math.toDegrees(northLon) + 540) % 360 - 180;
        double[] north = {northLat, northLon};


        double eastLat = Math.asin(Math.sin(lat) * Math.cos(d / R));
        double eastLon = lon + Math.atan2(1 * Math.sin(d / R) * Math.cos(lat), Math.cos(d / R) - Math.sin(lat) * Math.sin(eastLat));
        eastLat = Math.toDegrees(eastLat);
        eastLon = (Math.toDegrees(eastLon) + 540) % 360 - 180;
        double[] east = {eastLat, eastLon};

        double southLat = Math.asin(Math.sin(lat) * Math.cos(d / R) + Math.cos(lat) * Math.sin(d / R) * 1);
        double southLon = lon + Math.atan2(0, Math.cos(d / R) - Math.sin(lat) * Math.sin(southLat));
        southLat = Math.toDegrees(southLat);
        southLon = (Math.toDegrees(southLon) + 540) % 360 - 180;
        double[] south = {southLat, southLon};

        double westLat = Math.asin(Math.sin(lat) * Math.cos(d / R));
        double westLon = lon + Math.atan2(1 * Math.sin(d / R) * Math.cos(lat), Math.cos(d / R) - Math.sin(lat) * Math.sin(westLat));
        westLat = Math.toDegrees(westLat);
        westLon = (Math.toDegrees(westLon) + 540) % 360 - 180;
        double[] west = {westLat, westLon};
        double[][] coordinates = {north, east, south, west};
        location.put("coordinates", coordinates);

        for (int i = 0; i < 4; i++) {
            double[][] locx = (double[][]) location.get("coordinates");
            Log.d(TAG, "Lat = " + locx[i][0] + " Long = " + locx[i][1]);
        }
        return location;
    }
}