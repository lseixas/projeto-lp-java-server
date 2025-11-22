package com.cvetti.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient {

    private String host;
    private int port;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized String send(String data) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(data);
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    // --- NOVO MÉTODO: Checa se o servidor está ouvindo ---
    public boolean isConnected() {
        try (Socket socket = new Socket()) {
            // Tenta conectar com timeout de 1 segundo para não travar a thread
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (IOException e) {
            return false; 
        }
    }
}