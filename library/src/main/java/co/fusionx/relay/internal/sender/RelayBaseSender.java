package co.fusionx.relay.internal.sender;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.fusionx.relay.internal.packet.Packet;

import static co.fusionx.relay.misc.RelayConfigurationProvider.getPreferences;

public class RelayBaseSender implements BaseSender {

    private final Object mLock = new Object();

    private final ExecutorService mExecutorService;

    private BufferedWriter mBufferedWriter;

    public RelayBaseSender() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    @Override
    public void sendPacket(final Packet packet) {
        final String line = packet.getLine();
        mExecutorService.submit(() -> sendLine(line));
    }

    @Override
    public void onOutputStreamCreated(final BufferedWriter writer) {
        synchronized (mLock) {
            mBufferedWriter = writer;
        }
    }

    @Override
    public void onConnectionTerminated() {
        synchronized (mLock) {
            mBufferedWriter = null;
        }
    }

    private void sendLine(final String line) {
        synchronized (mLock) {
            if (mBufferedWriter == null) {
                getPreferences().logServerLine(line);
                return;
            }

            try {
                mBufferedWriter.write(String.format("%s\r\n", line));
                mBufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}