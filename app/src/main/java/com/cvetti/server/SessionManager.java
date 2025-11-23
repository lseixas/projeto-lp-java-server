package com.cvetti.server;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    // Set para guardar as Tags dos clientes (IP + ID)
    private static final Set<String> activeClients = ConcurrentHashMap.newKeySet();

    /**
     * Registra atividade. Se for um cliente novo, avisa e mostra a lista.
     */
    public static void registerActivity(String clientTag) {
        // O método .add retorna true se o item NÃO existia antes (ou seja, é novo)
        if (activeClients.add(clientTag)) {
            System.out.println("Novo cliente conectado: " + clientTag);
            printConnectedList(); // Imprime a lista apenas na entrada
        }
    }

    /**
     * Remove o cliente e mostra a lista atualizada.
     */
    public static void removeClient(String clientTag) {
        if (activeClients.remove(clientTag)) {
            System.out.println(clientTag + " saiu.");
            printConnectedList(); // Imprime a lista apenas na saída
        }
    }

    /**
     * Imprime a lista de quem está online agora.
     */
    public static void printConnectedList() {
        if (activeClients.isEmpty()) {
            System.out.println("Nenhum cliente conectado.");
        } else {
            String lista = String.join(", ", activeClients);
            System.out.println(">> Clientes Online: " + lista);
        }
    }
}