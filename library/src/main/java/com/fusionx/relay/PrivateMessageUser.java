package com.fusionx.relay;

import com.fusionx.relay.event.PrivateEvent;
import com.fusionx.relay.misc.InterfaceHolders;
import com.fusionx.relay.util.Utils;

import java.util.ArrayList;
import java.util.List;

public final class PrivateMessageUser extends User {

    /**
     * Contains a copy of the messages when the conversation is not displayed to the user
     */
    private final List<Message> mBuffer = new ArrayList<Message>();

    /**
     * Stores whether the fragment corresponding to this conversation is cached by the
     * FragmentManager
     */
    private boolean mCached;

    public PrivateMessageUser(final String nick, final UserChannelInterface userChannelInterface,
            final String initalMessage) {
        super(nick, userChannelInterface);

        if (InterfaceHolders.getPreferences().shouldHandleInitialPrivateMessage() && Utils
                .isNotEmpty(initalMessage)) {
            mBuffer.add(new Message(InterfaceHolders.getEventResponses().getMessage(nick,
                    initalMessage)));
        }
    }

    public void onUserEvent(final PrivateEvent event) {
        if (Utils.isNotBlank(event.message)) {
            synchronized (mBuffer) {
                mBuffer.add(new Message(event.message));
            }
        }
    }

    // Getters and Setters
    public List<Message> getBuffer() {
        return mBuffer;
    }

    public boolean isCached() {
        return mCached;
    }

    public void setCached(boolean cached) {
        mCached = cached;
    }
}