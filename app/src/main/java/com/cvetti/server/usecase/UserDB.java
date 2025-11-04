package com.cvetti.server.usecase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.cvetti.server.objects.User;

public class UserDB  {

    // --- Detalhes da Conexão JDBC (Ajuste conforme seu ambiente) ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/banco_cvetti_users"; // Altere 'seu_banco'
    private static final String DB_USER = "root"; // Altere seu usuário
    private static final String DB_PASSWORD = "root1234"; // Altere sua senha

    // --- Método para obter conexão ---
    private Connection getConnection() throws SQLException {
        // Opcional: Carregar o driver (geralmente não necessário com JDBC 4+)
        // try {
        //     Class.forName("com.mysql.cj.jdbc.Driver");
        // } catch (ClassNotFoundException e) {
        //     System.err.println("Driver MySQL JDBC não encontrado.");
        //     e.printStackTrace();
        //     throw new SQLException("Driver não encontrado", e);
        // }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // --- CREATE ---
    public boolean addUser(User user) {
        String sql = "INSERT INTO usuario (id, name, email, cpf, passwordHash, saldo, nascimento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getCpf());
            pstmt.setString(5, user.getPasswordHash()); // Salva o hash
             // Converta saldo para o tipo apropriado se necessário (ex: BigDecimal)
            pstmt.setBigDecimal(6, new java.math.BigDecimal(user.getSaldo()));
             // Converte java.util.Date para java.sql.Date
            if (user.getNascimento() != null) {
                pstmt.setDate(7, new java.sql.Date(user.getNascimento().getTime()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao adicionar usuário: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- READ (By ID) ---
    public User getUserById(String id) {
        String sql = "SELECT id, name, email, cpf, passwordHash, saldo, nascimento FROM usuario WHERE id = ?";
        User user = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("cpf"),
                        rs.getString("passwordHash"),
                        rs.getBigDecimal("saldo").toString(), // Converte de volta para String
                        rs.getDate("nascimento") // java.sql.Date é compatível com java.util.Date
                );
            }
            rs.close(); // Fechar ResultSet explicitamente dentro do try-with-resources do PreparedStatement
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return user; // Retorna null se não encontrar
    }

    // --- READ (All) ---
    public List<User> getAllUsers() {
        String sql = "SELECT id, name, email, cpf, passwordHash, saldo, nascimento FROM usuario";
        List<User> users = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("cpf"),
                        rs.getString("passwordHash"),
                        rs.getBigDecimal("saldo").toString(),
                        rs.getDate("nascimento")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os usuários: " + e.getMessage());
            e.printStackTrace();
            // Retorna lista vazia em caso de erro
        }
        return users;
    }

    // --- UPDATE ---
    public boolean updateUser(User user) {
        // Note: Não atualizamos o ID (chave primária) nem o passwordHash aqui
        // A atualização de senha deve ser um processo separado e seguro
        String sql = "UPDATE usuario SET name = ?, email = ?, cpf = ?, saldo = ?, nascimento = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getCpf());
            pstmt.setBigDecimal(4, new java.math.BigDecimal(user.getSaldo()));
            if (user.getNascimento() != null) {
                pstmt.setDate(5, new java.sql.Date(user.getNascimento().getTime()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            pstmt.setString(6, user.getId()); // Condição WHERE

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- DELETE ---
    public boolean deleteUser(String id) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao deletar usuário: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- Método main para teste rápido (opcional) ---
    public static void main(String[] args) {
        UserDB userDB = new UserDB();

        java.util.Date birthDate = new java.util.Date(); // Use uma data real
        User newUser = new User("user1", "Test User", "test@example.com", "12345678900", "hashed_password", "100.50", birthDate);
        if (userDB.addUser(newUser)) {
            System.out.println("Usuário adicionado com sucesso.");
        }

        User foundUser = userDB.getUserById("user1");
        if (foundUser != null) {
            System.out.println("Usuário encontrado: " + foundUser.getName());
        } else {
            System.out.println("Usuário não encontrado.");
        }

        List<User> allUsers = userDB.getAllUsers();
        System.out.println("Total de usuários: " + allUsers.size());
        for (User u : allUsers) {
            System.out.println("- " + u.getName());
        }

         User userToUpdate = userDB.getUserById("user1");
         if (userToUpdate != null) {
             userToUpdate.setName("Updated Test User");
             userToUpdate.setSaldo("150.75");
             if(userDB.updateUser(userToUpdate)) {
                 System.out.println("Usuário atualizado.");
             }
         }
    }
}