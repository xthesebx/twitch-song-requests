package com.seb;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPServer {

    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Main main;
    private String channel;

    public TCPServer(Main main) {
        this.main = main;
        try {
            serverSocket = new ServerSocket(42069);
        } catch (IOException e) {}
    }

    public void start () throws IOException {
        socket = serverSocket.accept();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        channel = in.readLine();
        main.twitchChat.joinChannel(channel);
        try {
            if (in.readLine().equals("close")) {
                stop();
                start();
            }
        } catch (SocketException e) {
            stop();
            start();
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        socket.close();
        serverSocket.close();
        main.twitchChat.leaveChannel(channel);
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void sendRequest(String request) throws IOException {
        out.println(request);
    }
}
