package co.fusionx.relay.internal.base;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import co.fusionx.relay.base.Server;
import co.fusionx.relay.base.UserChannelInterface;
import co.fusionx.relay.constants.UserLevel;
import co.fusionx.relay.internal.sender.BaseSender;
import co.fusionx.relay.util.ParseUtils;

public class RelayUserChannelInterface implements UserChannelInterface {

    private final Collection<RelayQueryUser> mQueryUsers;

    private final Set<RelayChannelUser> mUsers;

    private final RelayMainUser mUser;

    private final Server mServer;

    private final BaseSender mBaseSender;

    RelayUserChannelInterface(final Server server, final BaseSender baseSender) {
        mServer = server;
        mBaseSender = baseSender;

        // Set the nick name to the first choice nick
        mUser = new RelayMainUser(server.getConfiguration().getNickStorage().getFirst());

        mUsers = new HashSet<>();
        mUsers.add(mUser);

        mQueryUsers = new LinkedHashSet<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RelayChannel> getChannel(final String name) {
        // Channel names have to unique disregarding case - not having ignore-case here leads
        // to null channels when the channel does actually exist
        return FluentIterable.from(mUser.getChannels())
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .first();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RelayChannelUser> getUser(final String nick) {
        return FluentIterable.from(mUsers)
                .filter(u -> nick.equals(u.getNick().getNickAsString()))
                .first();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<RelayQueryUser> getQueryUsers() {
        return mQueryUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RelayQueryUser> getQueryUser(final String nick) {
        return FluentIterable.from(mQueryUsers)
                .filter(u -> nick.equals(u.getNick().getNickAsString()))
                .first();
    }

    /**
     * Add the channel to the user and user to the channel. Also add the user to the global list
     * of users. The user is given a default user level in the channel of {@link
     * co.fusionx.relay.constants.UserLevel#NONE}
     *
     * @param user    the user to add to the channel
     * @param channel the channel to add to the user
     */
    public void coupleUserAndChannel(final RelayChannelUser user, final RelayChannel channel) {
        coupleUserAndChannel(user, channel, UserLevel.NONE);
    }

    /**
     * Add the channel to the user and user to the channel. Also add the user to the global list
     * of users. The user is given the user level in the channel as specified by userLevel
     *
     * @param user      the user to add to the channel
     * @param channel   the channel to add to the user
     * @param userLevel the level to give the user in the channel
     */
    public void coupleUserAndChannel(final RelayChannelUser user, final RelayChannel channel,
            final UserLevel userLevel) {
        addUserToChannel(channel, user);
        addChannelToUser(channel, user, userLevel);
    }

    /**
     * Remove the channel from the user and the user from the channel. Also if this channel is
     * the last one that we know the user has joined then remove the user from the global list
     *
     * @param user    the user to remove from the channel and/or remove it from the global list
     * @param channel the channel to remove from the user
     */
    public void decoupleUserAndChannel(final RelayChannelUser user, final RelayChannel channel) {
        removeUserFromChannel(channel, user);
        removeChannelFromUser(channel, user);
    }

    /**
     * Remove the user from the global list and return the channels the user joined
     *
     * @param user the user to remove from the global list
     * @return the channels the user had joined
     */
    public Collection<RelayChannel> removeUser(final RelayChannelUser user) {
        mUsers.remove(user);
        return user.getChannels();
    }

    /**
     * Remove the channel from our list of channels and return the users in the channel
     *
     * @param channel the channel to remove
     * @return the users that were in the channel
     */
    public Collection<RelayChannelUser> removeChannel(final RelayChannel channel) {
        mServer.getUser().getChannels().remove(channel);
        channel.markInvalid();
        return channel.getUsers();
    }

    /**
     * Add the user to the list of users of the channel
     *
     * @param channel the channel to add the user to
     * @param user    the user to add to the channel
     */
    void addUserToChannel(final RelayChannel channel, final RelayChannelUser user) {
        channel.addUser(user);
    }

    /**
     * Add the channel to the list of channels of the user
     *
     * @param channel   the channel to add to the user
     * @param user      the user to add to the channel to
     * @param userLevel the level to give the user in the channel
     */
    void addChannelToUser(final RelayChannel channel, final RelayChannelUser user,
            final UserLevel userLevel) {
        user.addChannel(channel, userLevel);

        // Also remember to add the user to the global list
        mUsers.add(user);
    }

    /**
     * Removes the channel from the list of channels in the user
     *
     * @param channel the channel to remove from the user
     * @param user    the user the channel is to be removed from
     */
    public void removeUserFromChannel(RelayChannel channel, RelayChannelUser user) {
        channel.removeUser(user);
    }

    /**
     * Removes the channel from the user and if this was the last channel we knew the user was
     * in, remove the channel from the global list of users
     *
     * @param channel the channel to remove from the user
     * @param user    the user to remove the channel from or remove from the global list
     */
    public void removeChannelFromUser(final RelayChannel channel, final RelayChannelUser user) {
        final Collection<RelayChannel> setOfChannels = user.getChannels();
        user.removeChannel(channel);

        // The app user check is to make sure that the app user isn't removed from the list of
        // users
        if (setOfChannels.size() == 0 && !(user instanceof RelayMainUser)) {
            mUsers.remove(user);
        }
    }

    /**
     * Get the user by source from the list of users which are in all the channels we know about
     *
     * @param rawSource the source of the user to retrieve
     * @return the user matching the source or null of none match
     */
    public RelayChannelUser getUserFromPrefix(final String rawSource) {
        final String nick = ParseUtils.getNickFromPrefix(rawSource);
        return getNonNullUser(nick);
    }

    /**
     * {@inheritDoc}
     */
    public RelayChannelUser getNonNullUser(final String nick) {
        return getUser(nick).or(new RelayChannelUser(nick));
    }

    public RelayChannel getNewChannel(final String channelName) {
        return new RelayChannel(mServer, mUser, mBaseSender, channelName);
    }

    public RelayQueryUser addQueryUser(final String nick) {
        final RelayQueryUser user = new RelayQueryUser(mServer, this, mBaseSender, nick);
        mQueryUsers.add(user);
        return user;
    }

    public void removeQueryUser(final RelayQueryUser user) {
        mQueryUsers.remove(user);
        user.markInvalid();
    }

    public Set<RelayChannelUser> getUsers() {
        return mUsers;
    }

    public RelayMainUser getMainUser() {
        return mUser;
    }

    public void onConnectionTerminated() {
        // Clear the global list of users - it's now invalid
        mUsers.clear();

        // Keep our own user inside though
        mUsers.add(mUser);
    }
}