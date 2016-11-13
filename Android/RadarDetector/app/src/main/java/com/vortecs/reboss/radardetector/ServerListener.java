package com.vortecs.reboss.radardetector;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class ServerListener extends Service {

    private static final String TAG = ServerListener.class.getSimpleName();
    //IBinder binder = new MyBinder();

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://chat.socket.io");
        } catch (URISyntaxException e) {}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mSocket.connect();
        return null;
    }


}
