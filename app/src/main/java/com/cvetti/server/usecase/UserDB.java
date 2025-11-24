package com.cvetti.server.usecase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import com.cvetti.server.objects.User;

public class UserDB {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/banco_cvetti_users";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root1234";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ==========================================================
    // CREATE USER
    // ==========================================================
    public boolean addUser(User user) {
        String sql =
            "INSERT INTO users (id, name, email, cpf, phone, passwordHash, passwordSalt, saldo) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getCpf());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getPasswordHash());
            pstmt.setString(7, user.getSalt());
            pstmt.setBigDecimal(8, new BigDecimal(user.getSaldo()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao adicionar usuário: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // GET BY CPF (login)
    // ==========================================================
    public User getUserByCpf(String cpf) {
        String sql = "SELECT * FROM users WHERE cpf = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cpf);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por CPF: " + e.getMessage());
        }
        return null;
    }

    // ==========================================================
    // GET BY ID
    // ==========================================================
    public User getUserById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
        }
        return null;
    }

    // ==========================================================
    // GET ALL
    // ==========================================================
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) users.add(mapResultSetToUser(rs));

        } catch (SQLException e) { e.printStackTrace(); }

        return users;
    }

    // ==========================================================
    // MAPPING RESULTSET → USER
    // ==========================================================
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("cpf"),
            rs.getString("phone"),
            rs.getString("passwordHash"),
            rs.getString("passwordSalt"),
            rs.getBigDecimal("saldo").toString()
        );
    }

    // ==========================================================
    // UPDATE USER (name, email, phone)
    // ==========================================================
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ==========================================================
    // DELETE
    // ==========================================================
    public boolean deleteUser(String id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) { e.printStackTrace(); return false; }
    }


    // ==========================================================
    // DEPOSIT
    // ==========================================================
    public boolean deposit(String id, BigDecimal value) {
        String sql = "UPDATE users SET saldo = saldo + ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, value);
            pstmt.setString(2, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro no depósito: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // WITHDRAW
    // ==========================================================
    public boolean withdraw(String id, BigDecimal value) {
        User u = getUserById(id);
        if (u == null) return false;

        BigDecimal current = new BigDecimal(u.getSaldo());
        if (current.compareTo(value) < 0) return false;

        String sql = "UPDATE users SET saldo = saldo - ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, value);
            pstmt.setString(2, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro no saque: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // TRANSFER
    // ==========================================================
    public boolean transfer(String fromId, String toId, BigDecimal value) {
        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            User sender = getUserById(fromId);
            if (sender == null || new BigDecimal(sender.getSaldo()).compareTo(value) < 0) {
                throw new SQLException("Saldo insuficiente.");
            }

            String sqlWithdraw = "UPDATE users SET saldo = saldo - ? WHERE id = ?";
            String sqlDeposit = "UPDATE users SET saldo = saldo + ? WHERE id = ?";

            try (PreparedStatement w = conn.prepareStatement(sqlWithdraw);
                 PreparedStatement d = conn.prepareStatement(sqlDeposit)) {

                w.setBigDecimal(1, value);
                w.setString(2, fromId);
                w.executeUpdate();

                d.setBigDecimal(1, value);
                d.setString(2, toId);
                d.executeUpdate();

                conn.commit();
                return true;
            }

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            return false;

        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }


    // ==========================================================
    // PIX METHODS
    // ==========================================================
    public boolean addPixKey(String userId, String type, String value) {
        String sql = "INSERT INTO pix_keys (user_id, key_type, key_value) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, value);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) { return false; }
    }

    public List<String> getPixKeys(String userId) {
        List<String> keys = new ArrayList<>();
        String sql = "SELECT key_type, key_value FROM pix_keys WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                keys.add(rs.getString("key_type") + ": " + rs.getString("key_value"));
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return keys;
    }

    public boolean deletePixKey(String userId, String keyValue) {
        String sql = "DELETE FROM pix_keys WHERE user_id = ? AND key_value = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, keyValue);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) { return false; }
    }
}
