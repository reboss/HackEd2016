package com.vortecs.reboss.radardetector;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.drivewyze.EventReceiver;
import com.drivewyze.GPXLocationProvider;
import com.drivewyze.JDRIVE;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private final String SERVER = "http://hacked2016.herokuapp.com";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GPXLocationProvider gpxLocation;
    private final double ABOUT_ONE_KILOMETER = 0.0085;
    private MyLocationListener myLocation;
    private boolean GpsPermission;
    private double latitude = 1.1;
    private double longitude = 1.1;
    private LocationManager locationManager;
    private View alert;

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
                    JSONObject payload = (JSONObject) args[0];
                    Long ts = System.currentTimeMillis() / 1000;
                    JDRIVE.instance().osu(payload.toString(), ts.toString());
                    Log.d(TAG, payload.toString());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alert = this.findViewById(R.id.colorAlert);
        copyGPXFile();
        gpxLocation = new GPXLocationProvider(new File(getFilesDir(), "master.gpx").getPath());

        setContentView(R.layout.activity_main);
        JDRIVE.instance().initialize(getFilesDir().getPath());
        JDRIVE.instance().setLocationProvider(gpxLocation);

        mSocket.on("push data", onNewFence);
        mSocket.connect();

        JDRIVE.instance().run();

        JDRIVE.instance().osr(new EventReceiver() {
            @Override
            public void receive(String event, String ts) {
                String payload = GetTheOSUData();
                JDRIVE.instance().osu(payload, ts);
            }
        });
        JDRIVE.instance().addListenerForEvent("myFenceEnterListener", "fence-enter", new EventReceiver() {
            @Override
            public void receive(String event, String ts) {
                Toast.makeText(getApplicationContext(), "There's photo radar nearby",
                        Toast.LENGTH_LONG).show();
                alert.setBackgroundColor(getResources().getColor(R.color.red));
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 1, this);
    }

    private void copyGPXFile() {
        try {
            InputStream istream = getAssets().open("gpx/master.gpx");
            int size = istream.available();
            byte[] buffer = new byte[size];
            istream.read(buffer);
            istream.close();

            FileOutputStream ostream = new FileOutputStream(new File(getFilesDir() + "/master.gpx"));
            ostream.write(buffer);
            ostream.close();
        } catch (Exception e) {

        }
    }

    private void sendOSU(String ts) {
        try {
            Log.v(TAG, "preparing OSU");
            String[] files = getAssets().list("os");
            StringBuilder payload = new StringBuilder();
            int numFiles = files.length;
            int count = 0;
            for (String file : files) {
                count++;
                Log.v(TAG, "reading file: " + file);
                InputStream stream = getAssets().open("os/" + file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    payload.append(line);
                }
                if (count < numFiles)
                    payload.append(',');
            }
            Log.v(TAG, "sending OSU");
            JDRIVE.instance().osu(payload.toString(), ts);
        } catch (Exception ex) {
        }
    }

    private String GetTheOSUData() {

        // read osreplica.json, check for timed out
        // read timed out object with delete flag set to true
        return new String();
    }

    public void setPhotoRadar(View view) {

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
        Toast.makeText(getApplicationContext(), "Message posted, Thanks for being a good samaritan... Also, your coordinates are: (" + latitude + "," + longitude + ")",
                Toast.LENGTH_LONG).show();
    }

    public void setAccident(View view) {

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
        while (thread.isAlive()) {
        }
        Toast.makeText(getApplicationContext(), "Data posted, Thanks for being a good samaritan",
                Toast.LENGTH_LONG).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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


    private void pushToServer(String type) throws IOException, JSONException {

        JSONObject json = makeJSONObject(1, 1, type);
        JSONObject location = makeLocation();

        json.put("location", location);
        mSocket.emit("new data", json);
    }

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

        double d = 1000;
        double R = 6371 * 1000;
        double lat = Math.toRadians(latitude);
        double lon = Math.toRadians(longitude);

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

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}