package com.seb;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    private ServerSocket serverSocket;
    private final Main main;

    public TCPServer(Main main) {
        this.main = main;
        try {
            serverSocket = new ServerSocket(42069);
        } catch (IOException ignored) {}
    }

    public void start () throws IOException {
         while (true) {
             new ClientHandler(serverSocket.accept(), main).run();
         }
    }
}
