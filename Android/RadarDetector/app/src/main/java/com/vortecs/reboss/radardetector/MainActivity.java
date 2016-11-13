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
        double[]coordinates = getCoordinates();
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
        json.put("_id", 1);
        json.put("id", 1);
        json.put("fenceType", type);
        location.put("type", "Polygon");
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
    public void setDeviceDetails(String details){

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

}
