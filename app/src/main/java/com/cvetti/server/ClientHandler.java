package com.cvetti.server;

import java.io.*;
import java.net.Socket;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.cvetti.server.usecase.UserDB;
import com.cvetti.server.objects.User;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int connectionId; 
    private final UserDB userDB = new UserDB();

    public ClientHandler(Socket socket, int connectionId) {
        this.clientSocket = socket;
        this.connectionId = connectionId;
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
                
                String ip = clientSocket.getInetAddress().getHostAddress();
                String action = request.optString("action", "");
                String instanceId = request.optString("instanceId", "???");
                
                // Tag Única (IP + ID da Janela)
                String sessionTag = ip + " [" + instanceId + "]";
                
                // Tag Técnica para Debug (mostra ID da thread)
                String debugTag = "[Conn #" + connectionId + " | " + instanceId + "]";
                
                if ("disconnect".equals(action)) {
                    SessionManager.removeClient(sessionTag); 
                    response.put("status", "bye");
                    // O log de saída já é feito dentro do removeClient
                } else {
                    // Registra que este cliente está vivo
                    // Se for novo, o SessionManager vai imprimir "Novo cliente..."
                    SessionManager.registerActivity(sessionTag);
                    
                    if ("heartbeat".equals(action)) {
                        // --- CORREÇÃO AQUI ---
                        // Removemos o printConnectedList() daqui para parar o spam.
                        // O heartbeat agora é silencioso no console.
                        response.put("status", "alive");
                    } else {
                        // Ações reais continuam gerando log
                        System.out.println(debugTag + " solicitou: " + action);
                        processAction(action, request, response);
                    }
                }

                out.println(response.toString());
            }
        } catch (IOException e) {
            // Ignora
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    // --- Lógica de Negócio (Mantenha igual ao anterior) ---
    private void processAction(String action, JSONObject request, JSONObject response) {
        try {
            switch (action) {
                case "login" -> {
                     User user = userDB.getUserByCpf(request.getString("cpf"));
                     if (user != null && user.getPasswordHash().equals(request.getString("password"))) {
                         response.put("status", "success");
                         response.put("user", user.toMap());
                     } else {
                         response.put("status", "error");
                         response.put("error", "Credenciais inválidas");
                     }
                }
                case "create" -> {
                     User user = new User(request.getString("id"), request.getString("name"), request.getString("email"), request.getString("cpf"), request.getString("phone"), request.getString("passwordHash"), request.getString("saldo"), new Date());
                     response.put("status", userDB.addUser(user) ? "success" : "error");
                }
                case "update" -> {
                      User u = userDB.getUserById(request.getString("id"));
                      if(u!=null) { u.setName(request.getString("name")); u.setEmail(request.getString("email")); u.setPhone(request.getString("phone")); userDB.updateUser(u); response.put("status", "success"); }
                }
                case "deposit" -> {
                     userDB.deposit(request.getString("id"), new BigDecimal(request.getString("value")));
                     response.put("status", "success");
                }
                case "withdraw" -> {
                     boolean ok = userDB.withdraw(request.getString("id"), new BigDecimal(request.getString("value")));
                     response.put("status", ok ? "success" : "error");
                }
                case "transfer" -> {
                     boolean ok = userDB.transfer(request.getString("id"), request.getString("targetId"), new BigDecimal(request.getString("value")));
                     response.put("status", ok ? "success" : "error");
                }
                case "pix_add" -> {
                     boolean ok = userDB.addPixKey(request.getString("id"), request.getString("type"), request.getString("key"));
                     response.put("status", ok ? "success" : "error");
                }
                case "pix_list" -> {
                     response.put("status", "success");
                     response.put("keys", new JSONArray(userDB.getPixKeys(request.getString("id"))));
                }
                case "pix_delete" -> {
                     boolean ok = userDB.deletePixKey(request.getString("id"), request.getString("key"));
                     response.put("status", ok ? "success" : "error");
                }
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
    }
}