package com.cvetti.server;

public class App {
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
