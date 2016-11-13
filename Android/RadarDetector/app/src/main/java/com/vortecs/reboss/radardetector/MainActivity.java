package com.vortecs.reboss.radardetector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.drivewyze.LocationListener;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.lang.Math;

public class MainActivity extends AppCompatActivity {

    private final String SERVER = "109.199.127.236";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GPXLocationProvider gpxLocation;
    private Location currentBestLocation = null;

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://chat.socket.io");
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
        return new String();
    }

    public void setPhotoRadar(View view) {
        //double[]coordinates = getCoordinates();
        double[] coordinates = {49.202011, 113.020292};

    }

    public void setAccident(View view) {
        double[] coordinates = getCoordinates();
    }

    private double[] getCoordinates() {
        MyLocationListener myLocation = new MyLocationListener();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = (LocationListener) myLocation;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // if permissions not granted
            return null;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10,
                (android.location.LocationListener) locationListener);

        double[] coord = {myLocation.getLat(), myLocation.getLong()};
        return coord;

    }

    private void pushToServer() throws IOException, JSONException {

        JSONObject json = makeJSONObject(1, 1, 'B');
        JSONObject location = makeLocation();

        String post = json.toString();
        URL server = new URL(SERVER);
        HttpURLConnection connection = (HttpURLConnection) server.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(post.getBytes().length);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.connect();

        DataOutputStream reqStream = new DataOutputStream(connection.getOutputStream());
        reqStream.writeBytes(post);
        reqStream.flush();

        Toast.makeText(getApplicationContext(), "Posted message, Thanks for being a good samaritan",
                Toast.LENGTH_LONG).show();
    }

    // Device details??
    public void setDeviceDetails(String details) {

        JDRIVE.instance().addListenerForEvent("myFenceEnterListener", "fence-enter", new EventReceiver() {
            @Override
            public void receive(String event, String ts) {
                System.out.println("fence has been entered");
            }
        });

        // how to remove fences??
        JDRIVE.instance().osr(new EventReceiver() {
            @Override
            public void receive(String event, String ts) {
                // get the fence and NLR objects from your backend or local store etc.
                String payload = GetTheOSUData();
                JDRIVE.instance().osu(payload, ts);
            }
        });
    }


    JSONObject makeJSONObject(int id, int _id, char type) throws JSONException {
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
        double R = 6371e3;
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
        double[][] coordinates = {north, east, west, south};
        location.put("coordinates", coordinates);

        return location;
    }
}