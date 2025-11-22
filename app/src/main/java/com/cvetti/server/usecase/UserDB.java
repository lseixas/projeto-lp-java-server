package com.cvetti.server.usecase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; 
import com.cvetti.server.objects.User;

public class UserDB {

    // --- Detalhes da Conexão JDBC ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/banco_cvetti_users";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root1234";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // --- CREATE ---
    public boolean addUser(User user) {
        // CORREÇÃO: Adicionado campo 'phone'
        String sql = "INSERT INTO usuario (id, name, email, cpf, phone, passwordHash, saldo, nascimento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getCpf());
            pstmt.setString(5, user.getPhone()); // <--- Novo campo
            pstmt.setString(6, user.getPasswordHash());
            pstmt.setBigDecimal(7, new BigDecimal(user.getSaldo()));
            
            if (user.getNascimento() != null) {
                pstmt.setDate(8, new java.sql.Date(user.getNascimento().getTime()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao adicionar usuário: " + e.getMessage());
            return false;
        }
    }

    // --- READ (By CPF) - Usado no Login ---
    public User getUserByCpf(String cpf) {
        String sql = "SELECT * FROM usuario WHERE cpf = ?";
        User user = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cpf);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por CPF: " + e.getMessage());
        }
        return user;
    }

    // --- READ (By ID) ---
    public User getUserById(String id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        User user = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
        }
        return user;
    }

    // --- READ (All) ---
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM usuario";
        List<User> users = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Método auxiliar para evitar repetição de código na leitura do ResultSet
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("cpf"),
            rs.getString("phone"), // <--- Leitura do novo campo
            rs.getString("passwordHash"),
            rs.getBigDecimal("saldo").toString(),
            rs.getDate("nascimento")
        );
    }

    // --- UPDATE (Dados Cadastrais) ---
    public boolean updateUser(User user) {
        // CORREÇÃO: Atualiza também o telefone
        String sql = "UPDATE usuario SET name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone()); // <--- Novo campo
            pstmt.setString(4, user.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========================================================================
    // --- MÉTODOS FINANCEIROS ---
    // ========================================================================

    public boolean deposit(String id, BigDecimal value) {
        String sql = "UPDATE usuario SET saldo = saldo + ? WHERE id = ?";
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

    public boolean withdraw(String id, BigDecimal value) {
        // Verifica saldo antes
        User u = getUserById(id);
        if (u == null) return false;
        BigDecimal currentBalance = new BigDecimal(u.getSaldo());
        if (currentBalance.compareTo(value) < 0) {
            return false;
        }

        String sql = "UPDATE usuario SET saldo = saldo - ? WHERE id = ?";
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

    public boolean transfer(String fromId, String toId, BigDecimal value) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Inicia Transação

            // Valida saldo origem
            User sender = getUserById(fromId); 
            if (sender == null || new BigDecimal(sender.getSaldo()).compareTo(value) < 0) {
                throw new SQLException("Saldo insuficiente ou remetente inválido.");
            }

            String sqlWithdraw = "UPDATE usuario SET saldo = saldo - ? WHERE id = ?";
            String sqlDeposit = "UPDATE usuario SET saldo = saldo + ? WHERE id = ?";

            try (PreparedStatement withdrawStmt = conn.prepareStatement(sqlWithdraw);
                 PreparedStatement depositStmt = conn.prepareStatement(sqlDeposit)) {

                // Executa Saque
                withdrawStmt.setBigDecimal(1, value);
                withdrawStmt.setString(2, fromId);
                int wRows = withdrawStmt.executeUpdate();

                // Executa Depósito
                depositStmt.setBigDecimal(1, value);
                depositStmt.setString(2, toId);
                int dRows = depositStmt.executeUpdate();

                if (wRows > 0 && dRows > 0) {
                    conn.commit(); // Sucesso
                    return true;
                } else {
                    conn.rollback(); // Falha no destino
                    return false;
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true);
                    conn.close(); 
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ========================================================================
    // --- MÉTODOS PIX ---
    // ========================================================================

    public boolean addPixKey(String userId, String type, String value) {
        String sql = "INSERT INTO pix_keys (user_id, key_type, key_value) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, value);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keys;
    }

    public boolean deletePixKey(String userId, String keyValue) {
        String sql = "DELETE FROM pix_keys WHERE user_id = ? AND key_value = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, keyValue);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}