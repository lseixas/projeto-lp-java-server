package com.cvetti.server;

import java.io.*;
import java.net.Socket;
import com.cvetti.server.usecase.UserDB;
import com.cvetti.server.objects.User;
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
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Recebido: " + line);

                JSONObject request = new JSONObject(line);
                JSONObject response = new JSONObject();

                switch (request.getString("action")) {
                    case "create" -> {
                        User user = new User(
                            request.getString("id"),
                            request.getString("name"),
                            request.getString("email"),
                            request.getString("cpf"),
                            request.getString("passwordHash"),
                            request.getString("saldo"),
                            new java.util.Date()
                        );
                        boolean ok = userDB.addUser(user);
                        response.put("status", ok ? "success" : "error");
                    }
                    case "get" -> {
                        User user = userDB.getUserById(request.getString("id"));
                        if (user != null) {
                            response.put("status", "success");
                            response.put("user", user.toMap());
                        } else {
                            response.put("status", "not_found");
                        }
                    }
                    default -> response.put("error", "Ação inválida");
                }

                out.println(response.toString());
            }

        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }
}
