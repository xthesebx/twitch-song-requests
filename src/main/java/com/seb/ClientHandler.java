package com.seb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String channel;
    private final Main main;

    ClientHandler(Socket socket, Main main) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        channel = in.readLine();
        this.main = main;
        main.clients.put(channel, this);
    }

    @Override
    public void run() {
        main.twitchChat.joinChannel(channel);
        String s;
        try {
            while ((s = in.readLine()) != null) {
                try {
                    if (s.equals("close")) {
                        stop();
                    } else {
                        main.twitchChat.sendMessage(channel, s);
                    }
                } catch (SocketException e) {
                    stop();
                }
            }
        } catch (IOException e) {

        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        socket.close();
        main.twitchChat.leaveChannel(channel);
        main.clients.remove(channel);
    }

    public void sendRequest(String request) throws IOException {
        out.println(request);
    }

    public Boolean isConnected() {
        return socket.isConnected();
    }
}
