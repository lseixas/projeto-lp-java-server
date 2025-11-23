package com.cvetti.server;

import java.io.IOException;
import java.net.BindException; // Importante para detectar o erro de porta
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final int port;
    private final AtomicInteger clientCounter = new AtomicInteger(0);
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        // Tenta abrir a porta. Se falhar aqui, joga o erro pro main.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("=========================================");
            System.out.println("   SERVIDOR INICIADO NA PORTA " + port);
            System.out.println("   (Pressione Ctrl+C para parar)");
            System.out.println("=========================================");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCounter.incrementAndGet();
                threadPool.execute(new ClientHandler(clientSocket, clientId));
            }
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        try {
            new Server(port).start();
        } catch (BindException e) {
            // --- AQUI ESTÁ A PROTEÇÃO ---
            System.err.println("\n[ERRO CRÍTICO] O servidor já está em andamento!");
            System.err.println("Motivo: A porta " + port + " já está sendo usada.");
            System.err.println("Solução: Feche a outra janela do servidor antes de abrir esta.\n");
            System.exit(1); // Fecha o programa com código de erro
        } catch (IOException e) {
            System.err.println("Erro inesperado no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}