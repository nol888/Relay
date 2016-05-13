package co.fusionx.relay.internal.parser.main.code;

import co.fusionx.relay.event.channel.ChannelModesEvent;
import co.fusionx.relay.internal.base.RelayChannel;
import co.fusionx.relay.internal.base.RelayServer;
import com.google.common.base.Optional;

import java.util.List;

class ChannelModesParser extends CodeParser {

    ChannelModesParser(final RelayServer server) {
        super(server);
    }

    @Override
    public void onParseCode(final List<String> parsedArray, final int code) {
        final String channelName = parsedArray.get(0);
        final Optional<RelayChannel> channelOptional = mUserChannelInterface.getChannel(channelName);

        if (!channelOptional.isPresent()) {
            return;
        }

        final RelayChannel channel = channelOptional.get();
        final StringBuilder modeStringBuilder = new StringBuilder();
        for (String s : parsedArray.subList(1, parsedArray.size())) {
            modeStringBuilder.append(s).append(' ');
        }

        String modeString = modeStringBuilder.toString().trim();
        channel.setChannelModes(modeString);
        channel.postAndStoreEvent(new ChannelModesEvent(channel, modeString));
    }
}