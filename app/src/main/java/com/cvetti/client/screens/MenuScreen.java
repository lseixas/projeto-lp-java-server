package com.cvetti.client.screens;

import com.cvetti.client.ClientApp;
import javax.swing.*;
import java.awt.*;

public class MenuScreen extends JPanel {
    private ClientApp app;
    private JLabel lblWelcome;
    // Adicionado o botão btnPix
    private JButton btnDeposit, btnWithdraw, btnTransfer, btnPix, btnEdit, btnLogout;

    public MenuScreen(ClientApp app) {
        this.app = app;
        // Aumentei para 7 linhas para caber todos os botões
        setLayout(new GridLayout(7, 1, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        
        lblWelcome = new JLabel("Bem-vindo", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblWelcome);

        btnDeposit = createMenuButton("tab.deposit");
        btnWithdraw = createMenuButton("tab.withdraw");
        btnTransfer = createMenuButton("tab.transfer");
        
        // --- CORREÇÃO: Inicializando o botão PIX ---
        btnPix = createMenuButton("tab.pix");
        // ------------------------------------------

        btnEdit = createMenuButton("tab.edit");
        
        btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.LIGHT_GRAY);
        
        // Ações de Navegação
        btnDeposit.addActionListener(e -> openTransaction("deposit"));
        btnWithdraw.addActionListener(e -> openTransaction("withdraw"));
        btnTransfer.addActionListener(e -> openTransaction("transfer"));
        
        // Ação do Botão Pix
        btnPix.addActionListener(e -> {
            // Carrega as chaves e mostra a tela
            app.getPixScreen().loadKeys();
            app.showScreen("PIX");
        });
        
        btnEdit.addActionListener(e -> {
            app.getEditUserScreen().loadUserData(); 
            app.showScreen("EDIT");
        });

        btnLogout.addActionListener(e -> app.logout());
        add(btnLogout);

        this.updateTexts();
    }

    private JButton createMenuButton(String key) {
        JButton btn = new JButton();
        btn.putClientProperty("key", key); // Guarda a chave para tradução

        btn.setForeground(Color.BLACK); // Força o texto a ser preto
        btn.setBackground(Color.WHITE); // Garante fundo branco para contraste
        btn.setFocusPainted(false);     // (Opcional) Remove a borda de foco feia ao clicar
        add(btn);
        return btn;
    }
    
    private void openTransaction(String type) {
        app.getTransactionScreen().configure(type);
        app.showScreen("TRANSACTION");
    }

    public void updateUserName(String name) {
        lblWelcome.setText("Olá, " + name);
    }

    public void updateTexts() {
        btnDeposit.setText(app.getMessages().getString("tab.deposit"));
        btnWithdraw.setText(app.getMessages().getString("tab.withdraw"));
        btnTransfer.setText(app.getMessages().getString("tab.transfer"));
        // Atualiza texto do Pix também
        btnPix.setText(app.getMessages().getString("tab.pix"));
        btnEdit.setText(app.getMessages().getString("tab.edit"));
    }
}