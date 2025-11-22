package com.cvetti.client.screens;

import com.cvetti.client.ClientApp;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterScreen extends JPanel {
    private ClientApp app;
    private JTextField txtName, txtEmail, txtCpf, txtPhone, txtBalance;
    private JPasswordField txtPass;
    private JButton btnRegister, btnBack;
    private JLabel lblName, lblEmail, lblCpf, lblPhone, lblPass, lblBal;

    // --- REGEX DE VALIDAÇÃO ---
    // Email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    // CPF: 000.000.000-00
    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$");
    // Telefone: +55(11)971775742
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+\\d{2}\\(\\d{2}\\)\\d{9}$");

    public RegisterScreen(ClientApp app) {
        this.app = app;
        // Aumentei linhas para caber o telefone
        setLayout(new GridLayout(8, 2, 10, 10)); 
        setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        initComponents();
        updateTexts();
    }

    private void initComponents() {
        lblName = new JLabel(); add(lblName);
        txtName = new JTextField(); add(txtName);

        lblEmail = new JLabel(); add(lblEmail);
        txtEmail = new JTextField(); add(txtEmail);

        lblCpf = new JLabel(); add(lblCpf);
        txtCpf = new JTextField(); 
        txtCpf.setToolTipText("Ex: 123.456.789-00"); // Dica ao passar o mouse
        add(txtCpf);

        lblPhone = new JLabel(); add(lblPhone);
        txtPhone = new JTextField(); 
        txtPhone.setToolTipText("Ex: +55(11)971775742"); // Dica
        add(txtPhone);

        lblPass = new JLabel(); add(lblPass);
        txtPass = new JPasswordField(); add(txtPass);

        lblBal = new JLabel(); add(lblBal);
        txtBalance = new JTextField(); add(txtBalance);

        btnBack = new JButton();
        btnBack.addActionListener(e -> app.showScreen("LOGIN"));
        add(btnBack);

        btnRegister = new JButton();
        btnRegister.addActionListener(e -> doRegister());
        add(btnRegister);
    }

    private boolean validateFields() {
        // Valida Nome
        if (txtName.getText().trim().length() < 3) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.name"));
            return false;
        }

        // Valida Email
        if (!EMAIL_PATTERN.matcher(txtEmail.getText()).matches()) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.email"));
            return false;
        }

        // Valida CPF (Formato Estrito)
        if (!CPF_PATTERN.matcher(txtCpf.getText()).matches()) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.cpf_fmt") + "\nEx: 123.456.789-00");
            return false;
        }

        // Valida Telefone (Formato Estrito)
        if (!PHONE_PATTERN.matcher(txtPhone.getText()).matches()) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.phone") + "\nEx: +55(11)999999999");
            return false;
        }

        // Valida Senha
        if (new String(txtPass.getPassword()).length() < 6) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.pass"));
            return false;
        }

        // Valida Saldo
        try {
            String valStr = txtBalance.getText().replace(",", ".");
            if (new BigDecimal(valStr).compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.balance"));
            return false;
        }

        return true;
    }

    private void doRegister() {
        if (!validateFields()) return;

        String randomId = UUID.randomUUID().toString();
        
        JSONObject json = new JSONObject();
        json.put("action", "create");
        json.put("id", randomId);
        json.put("name", txtName.getText().trim());
        json.put("email", txtEmail.getText().trim());
        json.put("cpf", txtCpf.getText().trim()); // Envia com pontuação
        json.put("phone", txtPhone.getText().trim()); // Envia com formatação
        json.put("passwordHash", new String(txtPass.getPassword()));
        json.put("saldo", txtBalance.getText().replace(",", "."));

        app.sendRequest(json, response -> {
            if ("success".equals(response.optString("status"))) {
                JOptionPane.showMessageDialog(this, app.getMessages().getString("msg.success"));
                clearFields();
                app.showScreen("LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Erro: " + response.optString("error"));
            }
        });
    }

    private void clearFields() {
        txtName.setText(""); txtEmail.setText(""); 
        txtCpf.setText(""); txtPhone.setText("");
        txtPass.setText(""); txtBalance.setText("");
    }

    public void updateTexts() {
        lblName.setText(app.getMessages().getString("lbl.name"));
        lblEmail.setText(app.getMessages().getString("lbl.email"));
        lblCpf.setText(app.getMessages().getString("lbl.cpf"));
        lblPhone.setText(app.getMessages().getString("lbl.phone")); // Novo
        lblPass.setText(app.getMessages().getString("lbl.password"));
        lblBal.setText(app.getMessages().getString("lbl.balance"));
        btnRegister.setText(app.getMessages().getString("btn.create"));
        btnBack.setText(app.getMessages().getString("btn.back"));
    }
}