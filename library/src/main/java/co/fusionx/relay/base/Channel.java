package co.fusionx.relay.base;

import java.util.Collection;

import co.fusionx.relay.event.channel.ChannelEvent;
import co.fusionx.relay.sender.ChannelSender;
import com.google.common.base.Optional;

public interface Channel extends Conversation<ChannelEvent>, ChannelSender {

    String getName();

    Optional<String> getChannelKey();

    Collection<? extends ChannelUser> getUsers();

    void setChannelModes(String modeString);
}