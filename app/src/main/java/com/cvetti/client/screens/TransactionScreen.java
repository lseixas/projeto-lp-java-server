package com.cvetti.client.screens;

import com.cvetti.client.ClientApp;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL; // Necessário para carregar o arquivo

public class TransactionScreen extends JPanel {
    private ClientApp app;
    private String currentType; // "deposit", "withdraw", "transfer"

    private JLabel lblImage, lblTitle, lblValue, lblDestiny;
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

        // --- 1. IMAGEM (ATM) ---
        // Tenta carregar a imagem da pasta resources
        try {
            // O caminho começa com / porque está na raiz do resources
            URL imgUrl = getClass().getResource("/img/atm_image.jpg");
            
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                
                // Redimensiona a imagem para 100x100 (ajuste conforme quiser)
                Image img = originalIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                
                lblImage = new JLabel(new ImageIcon(img));
                // Centraliza a imagem
                lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                
                gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
                add(lblImage, gbc);
            } else {
                System.err.println("Imagem não encontrada em /img/atm_image.jpg");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- 2. TÍTULO ---
        lblTitle = new JLabel("Transação");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER); // Centraliza texto
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; // Agora é linha 1
        add(lblTitle, gbc);

        // --- 3. DESTINO (Só para transferência) ---
        lblDestiny = new JLabel();
        txtDestiny = new JTextField(15);
        
        gbc.gridy = 2; gbc.gridwidth = 1; // Agora é linha 2
        gbc.gridx = 0; add(lblDestiny, gbc);
        gbc.gridx = 1; add(txtDestiny, gbc);

        // --- 4. VALOR ---
        lblValue = new JLabel();
        txtValue = new JTextField(15);
        
        gbc.gridy = 3; // Agora é linha 3
        gbc.gridx = 0; add(lblValue, gbc);
        gbc.gridx = 1; add(txtValue, gbc);

        // --- 5. BOTÕES ---
        btnCancel = new JButton("Cancelar");
        btnExecute = new JButton("Confirmar");
        
        gbc.gridy = 4; // Agora é linha 4
        gbc.gridx = 0; add(btnCancel, gbc);
        gbc.gridx = 1; add(btnExecute, gbc);

        // Ações
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

    private boolean validateFields() {
        String valueStr = txtValue.getText().trim();

        if (valueStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, digite um valor.");
            return false;
        }

        if ("transfer".equals(currentType) && txtDestiny.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o ID de destino.");
            return false;
        }

        try {
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
        if (!validateFields()) return; 

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