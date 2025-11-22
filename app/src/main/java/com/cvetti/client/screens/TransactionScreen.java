package com.cvetti.client.screens;

import com.cvetti.client.ClientApp;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class TransactionScreen extends JPanel {
    private ClientApp app;
    private String currentType; // "deposit", "withdraw", "transfer"

    private JLabel lblTitle, lblValue, lblDestiny;
    private JTextField txtValue, txtDestiny;
    private JButton btnExecute, btnCancel;

    public TransactionScreen(ClientApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblTitle = new JLabel("Transação");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        // Destino (Só para transferência)
        lblDestiny = new JLabel();
        txtDestiny = new JTextField(15);
        gbc.gridy = 1; gbc.gridwidth = 1;
        add(lblDestiny, gbc);
        gbc.gridx = 1; add(txtDestiny, gbc);

        // Valor
        lblValue = new JLabel();
        txtValue = new JTextField(15);
        gbc.gridx = 0; gbc.gridy = 2; add(lblValue, gbc);
        gbc.gridx = 1; add(txtValue, gbc);

        // Botões
        btnCancel = new JButton("Cancelar");
        btnExecute = new JButton("Confirmar");
        
        gbc.gridx = 0; gbc.gridy = 3; add(btnCancel, gbc);
        gbc.gridx = 1; add(btnExecute, gbc);

        btnCancel.addActionListener(e -> app.showScreen("MENU"));
        btnExecute.addActionListener(e -> doTransaction());
    }

    public void configure(String type) {
        this.currentType = type;
        txtValue.setText("");
        txtDestiny.setText("");
        
        updateTexts(); 

        boolean isTransfer = "transfer".equals(type);
        lblDestiny.setVisible(isTransfer);
        txtDestiny.setVisible(isTransfer);
    }

    // --- NOVA VALIDAÇÃO ---
    private boolean validateFields() {
        String valueStr = txtValue.getText().trim();

        // 1. Valida se está vazio
        if (valueStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, digite um valor.");
            return false;
        }

        // 2. Valida Destino (apenas se for transferência)
        if ("transfer".equals(currentType) && txtDestiny.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o ID de destino.");
            return false;
        }

        // 3. Valida Formato do Valor e se é Positivo
        try {
            // Troca vírgula por ponto para o Java entender (ex: "10,50" -> "10.50")
            String cleanValue = valueStr.replace(",", ".");
            BigDecimal val = new BigDecimal(cleanValue);

            if (val.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "O valor deve ser maior que zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido! Use números e vírgula (ex: 50,00).");
            return false;
        }

        return true;
    }

    private void doTransaction() {
        if (!validateFields()) return; // Para se a validação falhar

        // Prepara o valor formatado para o servidor (com ponto)
        String formattedValue = txtValue.getText().replace(",", ".");

        JSONObject json = new JSONObject();
        json.put("action", currentType);
        json.put("id", app.getLoggedUser().getString("id"));
        json.put("value", formattedValue);

        if ("transfer".equals(currentType)) {
            json.put("targetId", txtDestiny.getText().trim());
        }

        app.sendRequest(json, response -> {
            if ("success".equals(response.optString("status"))) {
                JOptionPane.showMessageDialog(this, app.getMessages().getString("msg.success"));
                app.showScreen("MENU");
            } else {
                String errorMsg = java.text.MessageFormat.format(app.getMessages().getString("msg.error"), response.optString("error"));
                JOptionPane.showMessageDialog(this, errorMsg);
            }
        });
    }

    public void updateTexts() {
        lblValue.setText(app.getMessages().getString("lbl.value"));
        lblDestiny.setText(app.getMessages().getString("lbl.destiny"));
        btnCancel.setText(app.getMessages().getString("btn.cancel"));
        btnExecute.setText(app.getMessages().getString("btn.confirm"));
        
        String key = "tab." + currentType;
        if(currentType != null) lblTitle.setText(app.getMessages().getString(key));
    }
}