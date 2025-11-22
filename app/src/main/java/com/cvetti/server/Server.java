package com.cvetti.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // REMOVI O SYSTEM.OUT DAQUI. 
                // Deixe o ClientHandler e o SessionManager cuidarem dos logs.
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Server(8080).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}