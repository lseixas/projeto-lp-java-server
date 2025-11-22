package com.cvetti.server;

import java.io.*;
import java.net.Socket;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List; // Importante para o Pix
import com.cvetti.server.usecase.UserDB;
import com.cvetti.server.objects.User;
import org.json.JSONArray; // Importante para enviar a lista de chaves
import org.json.JSONObject;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final UserDB userDB = new UserDB();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line = in.readLine();
            if (line != null) {
                JSONObject request = new JSONObject(line);
                JSONObject response = new JSONObject();
                
                // Identificação do Cliente
                String ip = clientSocket.getInetAddress().getHostAddress();
                String action = request.optString("action", "");

                // --- LÓGICA DE MONITORAMENTO E LOGS ---
                
                if ("disconnect".equals(action)) {
                    SessionManager.removeClient(ip);
                    response.put("status", "bye");
                } else {
                    // Registra atividade (se for novo, imprime no console)
                    SessionManager.registerActivity(ip);
                    
                    if ("heartbeat".equals(action)) {
                        SessionManager.printConnectedList();
                        response.put("status", "alive");
                    } else {
                        // Processa a ação real (Login, Pix, etc)
                        processAction(action, request, response);
                    }
                }

                out.println(response.toString());
            }
        } catch (IOException e) {
            // Ignora conexões vazias ou erros de rede
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    // --- LÓGICA DE NEGÓCIO ---
    private void processAction(String action, JSONObject request, JSONObject response) {
        try {
            switch (action) {
                // --- AUTENTICAÇÃO E CADASTRO ---
                case "login" -> {
                    // Login agora é por CPF
                    User user = userDB.getUserByCpf(request.getString("cpf"));
                    if (user != null && user.getPasswordHash().equals(request.getString("password"))) {
                        response.put("status", "success");
                        response.put("user", user.toMap());
                    } else {
                        response.put("status", "error");
                        response.put("error", "CPF ou Senha incorretos");
                    }
                }
                case "create" -> {
                    User user = new User(
                        request.getString("id"), request.getString("name"),
                        request.getString("email"), request.getString("cpf"),
                        request.getString("phone"), // <--- Novo
                        request.getString("passwordHash"), request.getString("saldo"), new Date()
                    );
                    boolean ok = userDB.addUser(user);
                    response.put("status", ok ? "success" : "error");
                    if(!ok) response.put("error", "Erro ao criar conta (CPF ou Email já existem?)");
                }
                case "update" -> {
                    User u = userDB.getUserById(request.getString("id"));
                    if (u != null) {
                        u.setName(request.getString("name"));
                        u.setEmail(request.getString("email"));
                        userDB.updateUser(u);
                        response.put("status", "success");
                    } else response.put("status", "error");
                }

                // --- TRANSAÇÕES FINANCEIRAS ---
                case "deposit" -> {
                    userDB.deposit(request.getString("id"), new BigDecimal(request.getString("value")));
                    response.put("status", "success");
                }
                case "withdraw" -> {
                    boolean ok = userDB.withdraw(request.getString("id"), new BigDecimal(request.getString("value")));
                    if(ok) response.put("status", "success"); 
                    else response.put("error", "Saldo insuficiente");
                }
                case "transfer" -> {
                    boolean ok = userDB.transfer(request.getString("id"), request.getString("targetId"), new BigDecimal(request.getString("value")));
                    if(ok) response.put("status", "success"); 
                    else response.put("error", "Erro na transferência (Verifique ID e Saldo)");
                }

                // --- MÓDULO PIX (NOVO) ---
                case "pix_add" -> {
                    boolean ok = userDB.addPixKey(request.getString("id"), request.getString("type"), request.getString("key"));
                    response.put("status", ok ? "success" : "error");
                }
                case "pix_list" -> {
                    List<String> keys = userDB.getPixKeys(request.getString("id"));
                    response.put("status", "success");
                    // Converte a lista Java para Array JSON
                    response.put("keys", new JSONArray(keys));
                }
                case "pix_delete" -> {
                    boolean ok = userDB.deletePixKey(request.getString("id"), request.getString("key"));
                    response.put("status", ok ? "success" : "error");
                }
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", "Erro interno: " + e.getMessage());
            e.printStackTrace(); // Ajuda a debugar no console do servidor
        }
    }
}