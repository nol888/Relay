package co.fusionx.relay.parser.connection;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import co.fusionx.relay.base.ServerConfiguration;
import co.fusionx.relay.base.relay.RelayServer;
import co.fusionx.relay.constants.ServerCommands;
import co.fusionx.relay.constants.ServerReplyCodes;
import co.fusionx.relay.event.server.GenericServerEvent;
import co.fusionx.relay.misc.NickStorage;
import co.fusionx.relay.sender.relay.RelayInternalSender;
import co.fusionx.relay.sender.relay.RelayServerLineSender;
import co.fusionx.relay.util.IRCUtils;

public class ServerConnectionParser {

    private final RelayServer mServer;

    private final ServerConfiguration mConfiguration;

    private final BufferedReader mBufferedReader;

    private final RelayInternalSender mInternalSender;

    private final CapParser mCapParser;

    private int mIndex;

    private int mSuffix;

    public ServerConnectionParser(final RelayServer server, final ServerConfiguration configuration,
            final BufferedReader bufferedReader, final RelayServerLineSender serverLineSender) {
        mServer = server;
        mConfiguration = configuration;
        mBufferedReader = bufferedReader;

        mInternalSender = new RelayInternalSender(serverLineSender);
        mCapParser = new CapParser(server, configuration);

        mIndex = 1;
        mSuffix = 1;
    }

    public String parseConnect() throws IOException {
        String line;
        while ((line = mBufferedReader.readLine()) != null) {
            final List<String> parsedArray = IRCUtils.splitRawLine(line, true);
            final String command = parsedArray.get(0);
            switch (command) {
                case ServerCommands.PING:
                    // Immediately return
                    final String source = parsedArray.get(1);
                    mInternalSender.pongServer(source);
                    break;
                case ServerCommands.ERROR:
                    // We are finished - the server has kicked us out for some reason
                    return null;
                case ServerCommands.AUTHENTICATE:
                    mCapParser.parseCommand(parsedArray);
                    break;
                default:
                    if (StringUtils.isNumeric(parsedArray.get(1))) {
                        final String nick = parseConnectionCode(mConfiguration.isNickChangeable(),
                                parsedArray, mConfiguration.getNickStorage());
                        if (nick != null) {
                            return nick;
                        }
                    } else {
                        parseConnectionCommand(parsedArray);
                    }
                    break;
            }
        }
        return null;
    }

    private String parseConnectionCode(final boolean canChangeNick,
            final List<String> parsedArray, final NickStorage nickStorage) {
        final int code = Integer.parseInt(parsedArray.get(1));
        switch (code) {
            case ServerReplyCodes.RPL_WELCOME:
                // We are now logged in.
                final String nick = parsedArray.get(2);
                IRCUtils.removeFirstElementFromList(parsedArray, 3);
                return nick;
            case ServerReplyCodes.ERR_NICKNAMEINUSE:
                onNicknameInUse(canChangeNick, nickStorage);
                break;
            case ServerReplyCodes.ERR_NONICKNAMEGIVEN:
                mServer.sendNick(nickStorage.getFirst());
                break;
            default:
                if (ServerReplyCodes.saslCodes.contains(code)) {
                    mCapParser.parseCode(code, parsedArray);
                }
                break;
        }
        return null;
    }

    private void onNicknameInUse(final boolean canChangeNick, final NickStorage nickStorage) {
        if (mIndex < nickStorage.getNickCount()) {
            mServer.sendNick(nickStorage.getNickAtPosition(mIndex));
            mIndex++;
        } else if (canChangeNick) {
            mServer.sendNick(nickStorage.getFirst() + mSuffix);
            mSuffix++;
        } else {
            // TODO - fix this
            //sender.sendNickInUseMessage();
        }
    }

    private void parseConnectionCommand(final List<String> parsedArray) {
        final String command = parsedArray.get(1).toUpperCase();
        IRCUtils.removeFirstElementFromList(parsedArray, 3);

        switch (command) {
            case ServerCommands.NOTICE:
                mServer.postAndStoreEvent(new GenericServerEvent(mServer, parsedArray.get(0)));
                break;
            case ServerCommands.CAP:
                mCapParser.parseCommand(parsedArray);
                break;
        }
    }
}
