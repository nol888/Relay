package co.fusionx.relay.internal.packet.server.internal;

import co.fusionx.relay.internal.packet.Packet;

public class PingPacket implements Packet {

    public final String mToken;

    public PingPacket(final String token) {
        mToken = token;
    }

    @Override
    public String getLine() {
        return String.format("PING :%s", mToken);
    }
}