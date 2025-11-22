package com.cvetti.server;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    // Set para guardar IPs únicos
    private static final Set<String> activeClients = ConcurrentHashMap.newKeySet();

    /**
     * Registra atividade do cliente.
     * Se o IP não estiver na lista, imprime "Novo cliente conectado".
     */
    public static void registerActivity(String ip) {
        // O método add retorna 'true' se o elemento NÃO existia antes
        if (activeClients.add(ip)) {
            System.out.println("Novo cliente conectado: " + ip);
        }
    }

    /**
     * Remove o cliente da lista e imprime "Saiu".
     */
    public static void removeClient(String ip) {
        if (activeClients.remove(ip)) {
            System.out.println("Cliente " + ip + " saiu");
        }
    }

    /**
     * Imprime a lista de conectados (usado no Heartbeat)
     */
    public static void printConnectedList() {
        if (activeClients.isEmpty()) return;
        
        String lista = String.join(", ", activeClients);
        System.out.println("Clientes ainda conectados:\n" + lista);
    }
}