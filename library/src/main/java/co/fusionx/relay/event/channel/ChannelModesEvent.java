package co.fusionx.relay.event.channel;

import co.fusionx.relay.base.Channel;

public class ChannelModesEvent extends ChannelEvent {

    public final Channel channel;

    public final String modeString;

    public ChannelModesEvent(final Channel channel, final String modeString) {
        super(channel);

        this.channel = channel;
        this.modeString = modeString;
    }
}
