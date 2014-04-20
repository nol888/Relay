package com.fusionx.relay.connection;

import com.fusionx.relay.Server;
import com.fusionx.relay.ServerConfiguration;

import android.os.Handler;
import android.os.Looper;

public class ConnectionUtils {

    public static ServerConnection getConnection(final ServerConfiguration configuration) {
        final Handler handler = new Handler(Looper.getMainLooper());
        return new ServerConnection(configuration, handler, null);
    }

    public static Server getServerFromConnection(final ServerConnection connection) {
        return connection.getServer();
    }
}