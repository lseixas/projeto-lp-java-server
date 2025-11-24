package com.cvetti.server.objects;

import java.util.HashMap;

public class User {

    private String id;
    private String name;
    private String email;
    private String cpf;
    private String passwordHash;
    private String passwordSalt;
    private String saldo;
    private String phone;

    public User(String id, String name, String email, String cpf, String phone,
                String passwordHash, String passwordSalt, String saldo) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.saldo = saldo;
    }

    // --- Getters ---
    public String getId() { return id; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getCpf() { return cpf; }

    public String getPasswordHash() { return passwordHash; }

    public String getSalt() { return passwordSalt; }

    public String getSaldo() { return saldo; }

    public String getPhone() { return phone; }

    // --- toMap ---
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("email", email);
        result.put("cpf", cpf);
        result.put("saldo", saldo);
        result.put("phone", phone);
        return result;
    }

    // --- Setters ---
    public void setName(String name) { this.name = name; }

    public void setEmail(String email) { this.email = email; }

    public void setCpf(String cpf) { this.cpf = cpf; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public void setSaldo(String saldo) { this.saldo = saldo; }

    public void setPhone(String phone) { this.phone = phone; }
}
