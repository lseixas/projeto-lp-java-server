package com.cvetti.server.objects;

import java.util.Date;
import java.util.HashMap;

public class User {

    private String id;
    private String name;
    private String email;
    private String cpf;
    private String passwordHash; // Adicionado para armazenar o hash da senha
    private String saldo; // Considerar usar BigDecimal para valores monetários
    private Date nascimento;

    public User(String id, String name, String email, String cpf, String passwordHash, String saldo, Date nascimento) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.passwordHash = passwordHash; // Inicializar o novo campo
        this.saldo = saldo;
        this.nascimento = nascimento;
    }

    // --- Getters (Necessários para UserDB) ---
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSaldo() {
        return saldo;
    }

    public Date getNascimento() {
        return nascimento;
    }
    // --- Fim Getters ---

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("email", email);
        result.put("cpf", cpf);
        // Não inclua o hash da senha no toMap() por segurança, a menos que necessário
        result.put("saldo", saldo);
        result.put("nascimento", nascimento);
        return result;
    }

    // --- Setters (Opcional, mas útil para updates) ---
     public void setName(String name) { this.name = name; }
     public void setEmail(String email) { this.email = email; }
     public void setCpf(String cpf) { this.cpf = cpf; }
     public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
     public void setSaldo(String saldo) { this.saldo = saldo; }
     public void setNascimento(Date nascimento) { this.nascimento = nascimento; }
     // Não adicione setter para ID se for imutável após criação
}