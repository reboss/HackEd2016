package com.vortecs.reboss.radardetector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.drivewyze.EventReceiver;
import com.drivewyze.GPXLocationProvider;
import com.drivewyze.JDRIVE;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private final String SERVER = "109.199.127.236";
    private static final String TAG = MainActivity.class.getSimpleName();

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://chat.socket.io");
        } catch (URISyntaxException e) {}
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
        setContentView(R.layout.activity_main);
        JDRIVE.instance().initialize(getFilesDir().getPath());
        JDRIVE.instance().setLocationProvider(
                new GPXLocationProvider(
                        new File(getFilesDir(), "master.gpx").getPath()));

        mSocket.on("new fence", onNewFence);
        mSocket.connect();
        //JDRIVE.instance().run();
    }

    private String GetTheOSUData() {return new String();}


    public void setPhotoRadar(View view) {

    }

    public void setAccident(View view) {

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

    // Device details??
    public void setDeviceDetails(String details){}

}
