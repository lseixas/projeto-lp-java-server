package com.cvetti.client.screens;

import com.cvetti.client.ClientApp;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JPanel {
    private ClientApp app;
    private JTextField txtCpf;
    private JPasswordField txtPass;
    private JButton btnLogin, btnGoToRegister;
    private JLabel lblCpf, lblPass;

    public LoginScreen(ClientApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        initComponents();
        updateTexts();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblCpf = new JLabel();
        txtCpf = new JTextField(15);
        
        lblPass = new JLabel();
        txtPass = new JPasswordField(15);
        
        btnLogin = new JButton();
        btnGoToRegister = new JButton();

        gbc.gridx = 0; gbc.gridy = 0; add(lblCpf, gbc);
        gbc.gridx = 1; add(txtCpf, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(lblPass, gbc);
        gbc.gridx = 1; add(txtPass, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(btnLogin, gbc);
        
        gbc.gridy = 3;
        add(btnGoToRegister, gbc);

        btnLogin.addActionListener(e -> doLogin());
        btnGoToRegister.addActionListener(e -> app.showScreen("REGISTER"));
    }

    private boolean validateFields() {
        // Valida CPF
        String cpfClean = txtCpf.getText().replaceAll("\\D", "");
        if (cpfClean.length() != 11) {
            JOptionPane.showMessageDialog(this, "CPF Inválido!\nO CPF deve conter 11 dígitos numéricos.");
            return false;
        }

        // Valida Senha
        if (new String(txtPass.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Senha Inválida!\nA senha não pode estar vazia.");
            return false;
        }
        return true;
    }

    private void doLogin() {
        if (!validateFields()) return;

        String cpfText = txtCpf.getText();

        JSONObject json = new JSONObject();
        json.put("action", "login");
        json.put("cpf", cpfText);
        json.put("password", new String(txtPass.getPassword()));

        app.sendRequest(json, response -> {
            if ("success".equals(response.optString("status"))) {
                app.setLoggedUser(response.getJSONObject("user"));
                app.showScreen("MENU");
            } else {
                JOptionPane.showMessageDialog(this, app.getMessages().getString("msg.login_fail"));
            }
        });
    }

    public void updateTexts() {
        lblCpf.setText(app.getMessages().getString("lbl.cpf"));
        lblPass.setText(app.getMessages().getString("lbl.password"));
        btnLogin.setText(app.getMessages().getString("btn.login"));
        btnGoToRegister.setText(app.getMessages().getString("btn.create"));
    }

    public void clearFields() {
        txtCpf.setText("");
        txtPass.setText("");
    }
}