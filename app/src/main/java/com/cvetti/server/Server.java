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
            System.out.println("Servidor rodando na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }
    }
    public static void main(String[] args) {
        System.out.println("Iniciando servidor de usuários...");

        // Porta padrão (pode mudar se quiser)
        int port = 8080;

        try {
            Server server = new Server(port);
            server.start();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
