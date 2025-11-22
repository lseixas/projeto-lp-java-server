package com.cvetti.client.screens;

import com.cvetti.client.ClientApp;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class EditUserScreen extends JPanel {
    private ClientApp app;
    private JTextField txtName, txtEmail, txtPhone; // Adicionado Phone
    private JButton btnSave, btnCancel;
    private JLabel lblName, lblEmail, lblPhone;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+\\d{2}\\(\\d{2}\\)\\d{9}$");

    public EditUserScreen(ClientApp app) {
        this.app = app;
        setLayout(new GridLayout(5, 2, 10, 10)); // Aumentado
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        lblName = new JLabel(); add(lblName);
        txtName = new JTextField(); add(txtName);
        
        lblEmail = new JLabel(); add(lblEmail);
        txtEmail = new JTextField(); add(txtEmail);

        lblPhone = new JLabel(); add(lblPhone); // Novo Campo
        txtPhone = new JTextField(); add(txtPhone);
        
        btnCancel = new JButton();
        btnCancel.addActionListener(e -> app.showScreen("MENU"));
        add(btnCancel);
        
        btnSave = new JButton();
        btnSave.addActionListener(e -> doUpdate());
        add(btnSave);
    }
    
    public void loadUserData() {
        JSONObject user = app.getLoggedUser();
        if (user != null) {
            txtName.setText(user.optString("name"));
            txtEmail.setText(user.optString("email"));
            txtPhone.setText(user.optString("phone")); // Carrega o telefone
        }
    }

    private void doUpdate() {
        // Validação simples
        if (txtName.getText().length() < 3) {
            JOptionPane.showMessageDialog(this, "Nome curto demais."); return;
        }
        if (!PHONE_PATTERN.matcher(txtPhone.getText()).matches()) {
            JOptionPane.showMessageDialog(this, "Telefone inválido! Formato: +55(11)971775742"); return;
        }

        JSONObject json = new JSONObject();
        json.put("action", "update");
        json.put("id", app.getLoggedUser().getString("id"));
        json.put("name", txtName.getText());
        json.put("email", txtEmail.getText());
        json.put("phone", txtPhone.getText()); // Envia novo telefone

        app.sendRequest(json, response -> {
            if ("success".equals(response.optString("status"))) {
                JOptionPane.showMessageDialog(this, "Atualizado!");
                // Atualiza cache local
                app.getLoggedUser().put("name", txtName.getText());
                app.getLoggedUser().put("email", txtEmail.getText());
                app.getLoggedUser().put("phone", txtPhone.getText());
                
                app.setLoggedUser(app.getLoggedUser());
                app.showScreen("MENU");
            } else {
                JOptionPane.showMessageDialog(this, "Erro.");
            }
        });
    }

    public void updateTexts() {
        lblName.setText(app.getMessages().getString("lbl.new_name"));
        lblEmail.setText(app.getMessages().getString("lbl.new_email"));
        lblPhone.setText(app.getMessages().getString("lbl.phone")); // Novo
        btnSave.setText(app.getMessages().getString("btn.save"));
        btnCancel.setText(app.getMessages().getString("btn.cancel"));
    }
}