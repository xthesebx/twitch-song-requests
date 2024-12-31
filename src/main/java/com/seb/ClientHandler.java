package com.seb;


import com.hawolt.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String channel;
    private Main main;

    ClientHandler(Socket socket, Main main) throws IOException {
        if (!socket.getInetAddress().isLoopbackAddress()) {
            socket.close();
            return;
        }
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        channel = in.readLine();
        this.main = main;
        main.clients.put(channel, this);
        Logger.debug("Client connected: " + channel);
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
            if (e instanceof SocketException) {
                return;
            }
        }
    }

    public void stop() throws IOException {
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
