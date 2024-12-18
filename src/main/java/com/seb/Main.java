package com.seb;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.hawolt.logger.Logger;

import java.io.*;
import java.util.HashMap;

public class Main {

    HashMap<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) {
        new Main();
    }

    public TwitchChat twitchChat;
    public Main() {
        String original = read(new File("tokens.env"));
        String access = original.substring(0, original.indexOf("\n"));
        String refresh = original.substring(original.indexOf("\n") + 1).substring(0, original.indexOf("\n"));
        String clid = original.substring(original.indexOf("\n") + 1).substring(original.indexOf("\n") + 1);

        OAuth2Credential oAuth2Credential = new OAuth2Credential("twitch", access);
        twitchChat = TwitchChatBuilder.builder().withChatAccount(oAuth2Credential).withDefaultEventHandler(SimpleEventHandler.class).build();
        twitchChat.getChannels().forEach(channel -> {twitchChat.leaveChannel(channel);});
        EventManager eventManager = twitchChat.getEventManager();
        TCPServer tcpServer = new TCPServer(this);
        new Thread(() -> {
            tcpServer.start();
        }).start();
        eventManager.onEvent(ChannelMessageEvent.class, e -> {
            if (e.getMessage().startsWith("!request")) {
                String songname = e.getMessage().substring(e.getMessage().indexOf(" ") + 1);
                if (clients.get(e.getChannel().getName()).isConnected()) {
                    try {
                        clients.get(e.getChannel().getName()).sendRequest(songname);
                    } catch (IOException ex) {
                        Logger.error(ex);
                    }
                } else {
                    twitchChat.sendMessage(e.getChannel().getName(), "Request not available right now");
                }
            } else if (e.getMessage().equals("!song")) {
                if (clients.get(e.getChannel().getName()).isConnected()) {
                    try {
                        clients.get(e.getChannel().getName()).sendRequest("song?");
                    } catch (IOException ex) {
                        Logger.error(ex);
                    }
                }
            } else if (e.getMessage().equals("!playlist")) {
                twitchChat.sendMessage(e.getChannel().getName(), "https://playlist.sebgameservers.de");
            }
        });
    }

    public static String read (File file) {
        if (!file.exists()) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
                writer.write("{}");
                writer.close();
            } catch (IOException ignored) {
            }
        }
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String temp;
            temp = reader.readLine();
            text.append(temp);
            while (true) {
                temp = reader.readLine();
                if (temp == null) break;
                text.append("\n").append(temp);
            }
            reader.close();
        } catch (IOException ignored) {
        }
        return text.toString();
    }
}