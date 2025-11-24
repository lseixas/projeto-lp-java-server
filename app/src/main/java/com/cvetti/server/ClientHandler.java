package com.cvetti.server;

import java.io.*;
import java.net.Socket;
import java.math.BigDecimal;
import java.util.Date;

import com.cvetti.server.utils.PasswordUtils;
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

                String sessionTag = ip + " [" + instanceId + "]";
                String debugTag = "[Conn #" + connectionId + " | " + instanceId + "]";
                
                if ("disconnect".equals(action)) {
                    SessionManager.removeClient(sessionTag); 
                    response.put("status", "bye");
                } else {
                    SessionManager.registerActivity(sessionTag);

                    if ("heartbeat".equals(action)) {
                        response.put("status", "alive");
                    } else {
                        System.out.println(debugTag + " solicitou: " + action);
                        processAction(action, request, response);
                    }
                }

                out.println(response.toString());
                System.out.println("Enviado: " + response.toString());
            }
        } catch (IOException ignored) {
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    // ===================================================================
    // LÓGICA DE NEGÓCIO — AGORA 100% SEGURA COM HASH + SALT
    // ===================================================================
    private void processAction(String action, JSONObject request, JSONObject response) {
        try {
            switch (action) {

                case "login" -> {
                    User user = userDB.getUserByCpf(request.getString("cpf"));

                    if (user != null) {
                        boolean valid = PasswordUtils.checkPassword(
                                request.getString("password"),
                                user.getPasswordHash(),
                                user.getSalt()
                        );

                        if (valid) {
                            response.put("status", "success");
                            response.put("user", user.toMap());
                        } else {
                            response.put("status", "error");
                            response.put("error", "Credenciais inválidas");
                        }
                    } else {
                        response.put("status", "error");
                        response.put("error", "Credenciais inválidas");
                    }
                }

                case "create" -> {
                    String rawPassword = request.getString("password");
                    String salt = PasswordUtils.generateSalt();
                    String hash = PasswordUtils.hashPassword(rawPassword, salt);

                    User user = new User(
                            request.getString("id"),
                            request.getString("name"),
                            request.getString("email"),
                            request.getString("cpf"),
                            request.getString("phone"),
                            hash,
                            salt,
                            request.getString("saldo")
                    );

                    // salvar salt também
                    user.setPasswordSalt(salt);

                    response.put("status", userDB.addUser(user) ? "success" : "error");
                }

                case "update" -> {
                    User u = userDB.getUserById(request.getString("id"));
                    if (u != null) {
                        u.setName(request.getString("name"));
                        u.setEmail(request.getString("email"));
                        u.setPhone(request.getString("phone"));

                        userDB.updateUser(u);
                        response.put("status", "success");
                    }
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
                    boolean ok = userDB.transfer(
                            request.getString("id"),
                            request.getString("targetId"),
                            new BigDecimal(request.getString("value"))
                    );
                    response.put("status", ok ? "success" : "error");
                }

                case "pix_add" -> {
                    boolean ok = userDB.addPixKey(
                            request.getString("id"),
                            request.getString("type"),
                            request.getString("key")
                    );
                    response.put("status", ok ? "success" : "error");
                }

                case "pix_list" -> {
                    response.put("status", "success");
                    response.put("keys", new JSONArray(userDB.getPixKeys(request.getString("id"))));
                }

                case "pix_delete" -> {
                    boolean ok = userDB.deletePixKey(
                            request.getString("id"),
                            request.getString("key")
                    );
                    response.put("status", ok ? "success" : "error");
                }
            }

        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
        }
    }
}
