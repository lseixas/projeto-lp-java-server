package com.cvetti.server;

import java.net.InetAddress;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final int port;
    private final AtomicInteger clientCounter = new AtomicInteger(0);
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    
    // LISTA THREAD-SAFE: Guarda o histórico de tudo que foi printado
    private static final List<String> sessionLog = Collections.synchronizedList(new ArrayList<>());

    public Server(int port) {
        this.port = port;
    }

    // --- MÉTODO ESTÁTICO PARA USAR NO LUGAR DE System.out.println ---
    // Use Server.log("mensagem") aqui e no ClientHandler
    public static void log(String msg) {
        System.out.println(msg); // Mostra no console
        sessionLog.add(msg);     // Guarda na memória para salvar depois
    }

    public void start() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveLogToFile));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            String serverIp = InetAddress.getLocalHost().getHostAddress();

            log("=========================================");
            log("   SERVIDOR INICIADO");
            log("   ENDEREÇO: " + serverIp + ":" + port); // Mostra IP:Porta
            log("   (Pressione Ctrl+C para parar e salvar o log)");
            log("=========================================");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCounter.incrementAndGet();
                threadPool.execute(new ClientHandler(clientSocket, clientId));
            }
        }
    }

    private void saveLogToFile() {
        System.out.println("\nEncerrando servidor e salvando log...");
        
        // Salva tudo o que acumulou na lista sessionLog
        try (PrintWriter writer = new PrintWriter(new FileWriter("log.txt", true))) {
            writer.println("--------- SESSÃO INICIADA ---------");
            for (String line : sessionLog) {
                writer.println(line);
            }
            writer.println("--------- SESSÃO FINALIZADA ---------\n");
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo de log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        try {
            new Server(port).start();
        } catch (BindException e) {
            System.err.println("\n[ERRO CRÍTICO] Porta " + port + " em uso.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}