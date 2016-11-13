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

public class MainActivity extends AppCompatActivity {

    private final String SERVER = "http://hacked2016.herokuapp.com";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GPXLocationProvider gpxLocation;
    private final double ABOUT_ONE_KILOMETER = 0.0085;

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
        // readd timed out object with delete flag set to true
        return new String();
    }

    public void setPhotoRadar(View view) {
        //double[]coordinates = getCoordinates();
        final double[] coordinates = {49.202011, 113.020292};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pushToServer(coordinates, 'P');
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void setAccident(View view) {
        //double[]coordinates = getCoordinates();
        final double[] coordinates = {49.202011, 113.020292};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pushToServer(coordinates, 'A');
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
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

        double [] coord = {myLocation.getLat(), myLocation.getLong()};
        return coord;

    }

    private void pushToServer(double[] coordinates, char type) throws IOException, JSONException {

        JSONObject json = new JSONObject();
        JSONObject location = new JSONObject();
        double [][] polygon = {
                {coordinates[0]+ABOUT_ONE_KILOMETER, coordinates[1]},
                {coordinates[0]-ABOUT_ONE_KILOMETER, coordinates[1]},
                {coordinates[0], coordinates[1]+ABOUT_ONE_KILOMETER},
                {coordinates[0], coordinates[1]-ABOUT_ONE_KILOMETER}
        };
        json.put("_id", 1);
        json.put("id", 1);
        json.put("fenceType", type);
        location.put("type", "Polygon");
        location.put("coordinates", polygon);
        json.put("location", location);
        json.put("type", "fence");
        json.put("time", System.currentTimeMillis());

        mSocket.emit("new data", json);

        Toast.makeText(getApplicationContext(), "Posted message, Thanks for being a good samaritan",
                Toast.LENGTH_LONG).show();
    }

    // Device details??
    public void setDeviceDetails(String details){


        JDRIVE.instance().addListenerForEvent("myFenceEnterListener", "fence-enter", new EventReceiver() {
            @Override
            public void receive(String event, String ts) {
                // notify user about photo radar
                // UI event?
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

}
