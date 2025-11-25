package com.cvetti.client.screens;

import com.cvetti.client.ClientApp;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;

public class PixScreen extends JPanel {
    private ClientApp app;
    private JTabbedPane tabPix;
    
    // --- ABA 1: RECEBER (QR CODE) ---
    private JTextField txtReceiveValue;
    private JButton btnGenerateQR;
    private QRCodePanel pnlQRCode; // Painel customizado

    // --- ABA 2: GERENCIAR CHAVES ---
    private JComboBox<String> cboKeyType;
    private JTextField txtKeyInput;
    private JButton btnAddKey, btnRemoveKey;
    private DefaultListModel<String> listModel;
    private JList<String> listKeys;

    public PixScreen(ClientApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        
        tabPix = new JTabbedPane();
        
        // Constrói as duas abas
        tabPix.addTab("Cobrar (QR)", createReceivePanel());
        tabPix.addTab("Minhas Chaves", createKeysPanel());
        
        add(tabPix, BorderLayout.CENTER);
        
        // Botão voltar no rodapé
        JButton btnBack = new JButton("<< Voltar ao Menu");
        btnBack.addActionListener(e -> app.showScreen("MENU"));
        add(btnBack, BorderLayout.SOUTH);
    }

    // --- PAINEL DE COBRANÇA (QR CODE) ---
    private JPanel createReceivePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Topo: Valor e Botão
        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Valor (R$):"));
        txtReceiveValue = new JTextField(10);
        top.add(txtReceiveValue);
        
        btnGenerateQR = new JButton("Gerar QR Code");
        btnGenerateQR.setBackground(new Color(70, 130, 180));
        btnGenerateQR.setForeground(Color.WHITE);

        btnGenerateQR.setOpaque(true);
        btnGenerateQR.setContentAreaFilled(true);
        btnGenerateQR.setBorderPainted(false);

        top.add(btnGenerateQR);
        
        panel.add(top, BorderLayout.NORTH);

        // Centro: O Desenho do QR Code
        pnlQRCode = new QRCodePanel();
        panel.add(pnlQRCode, BorderLayout.CENTER);

        // Ação
        btnGenerateQR.addActionListener(e -> {
            String val = txtReceiveValue.getText();
            if (val.isEmpty()) return;
            // Gera um visual baseado no texto (simulação)
            pnlQRCode.generateCode("PIX-" + val + "-" + System.currentTimeMillis());
        });

        return panel;
    }

    // --- PAINEL DE CHAVES ---
    private JPanel createKeysPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Entrada de dados
        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        cboKeyType = new JComboBox<>(new String[]{"CPF", "EMAIL", "TELEFONE", "ALEATORIA"});
        txtKeyInput = new JTextField();
        btnAddKey = new JButton("Cadastrar Chave");
        
        inputPanel.add(new JLabel("Tipo de Chave:"));
        inputPanel.add(cboKeyType);
        inputPanel.add(txtKeyInput);
        
        // Lógica visual: Se escolher CPF, trava o campo e usa o do usuário
        cboKeyType.addActionListener(e -> {
            String type = (String) cboKeyType.getSelectedItem();
            if ("CPF".equals(type)) {
                txtKeyInput.setText(app.getLoggedUser().optString("cpf"));
                txtKeyInput.setEditable(false);
            } else if ("ALEATORIA".equals(type)) {
                txtKeyInput.setText(UUID.randomUUID().toString());
                txtKeyInput.setEditable(false);
            } else {
                txtKeyInput.setText("");
                txtKeyInput.setEditable(true);
            }
        });

        // Lista de Chaves
        listModel = new DefaultListModel<>();
        listKeys = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(listKeys);

        // Botões de Ação
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnAddKey);
        btnRemoveKey = new JButton("Remover Selecionada");
        btnPanel.add(btnRemoveKey);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        btnAddKey.addActionListener(e -> registerKey());
        btnRemoveKey.addActionListener(e -> removeKey());

        return panel;
    }

    // --- LÓGICA BACKEND ---

    public void loadKeys() {
        listModel.clear();
        JSONObject json = new JSONObject();
        json.put("action", "pix_list");
        json.put("id", app.getLoggedUser().getString("id"));

        app.sendRequest(json, response -> {
            if (response.has("keys")) {
                JSONArray arr = response.getJSONArray("keys");
                for (int i = 0; i < arr.length(); i++) {
                    listModel.addElement(arr.getString(i));
                }
            }
        });
    }

    private void registerKey() {
        JSONObject json = new JSONObject();
        json.put("action", "pix_add");
        json.put("id", app.getLoggedUser().getString("id"));
        json.put("type", cboKeyType.getSelectedItem());
        json.put("key", txtKeyInput.getText());

        app.sendRequest(json, response -> {
            if ("success".equals(response.optString("status"))) {
                JOptionPane.showMessageDialog(this, "Chave cadastrada!");
                loadKeys(); // Recarrega a lista
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar.");
            }
        });
    }

    private void removeKey() {
        String selected = listKeys.getSelectedValue();
        if (selected == null) return;

        // O formato é "TIPO: VALOR". Vamos pegar só o valor (parte depois dos dois pontos)
        String keyValue = selected.split(": ")[1];

        JSONObject json = new JSONObject();
        json.put("action", "pix_delete");
        json.put("id", app.getLoggedUser().getString("id"));
        json.put("key", keyValue);

        app.sendRequest(json, response -> {
            if ("success".equals(response.optString("status"))) {
                loadKeys(); // Recarrega
            }
        });
    }
    
    public void updateTexts() {
        // Atualize textos do properties aqui se desejar
    }

    // --- COMPONENTE VISUAL FAKE QR CODE ---
    // Desenha quadrados aleatórios para parecer um QR Code real sem precisar de lib externa
    class QRCodePanel extends JPanel {
        private BufferedImage qrImage;

        public QRCodePanel() {
            setPreferredSize(new Dimension(200, 200));
            setBackground(Color.WHITE);
        }

        public void generateCode(String seed) {
            int size = 200;
            int modules = 25; // Quantidade de quadradinhos
            int cellSize = size / modules;
            
            qrImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = qrImage.createGraphics();
            
            // Fundo Branco
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, size, size);
            
            // Gera padrão baseado na seed (para ser sempre igual para o mesmo valor)
            Random rand = new Random(seed.hashCode());
            g2d.setColor(Color.BLACK);

            for (int row = 0; row < modules; row++) {
                for (int col = 0; col < modules; col++) {
                    // Desenha quadrados pretos aleatórios, mas deixa borda branca
                    if (row > 1 && row < modules-2 && col > 1 && col < modules-2) {
                         if (rand.nextBoolean()) {
                             g2d.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                         }
                    }
                }
            }
            
            // Desenha os "Olhos" do QR Code (Quadrados grandes nos cantos)
            drawFinderPattern(g2d, 2, 2, cellSize);
            drawFinderPattern(g2d, modules - 9, 2, cellSize);
            drawFinderPattern(g2d, 2, modules - 9, cellSize);

            g2d.dispose();
            repaint();
        }

        private void drawFinderPattern(Graphics2D g, int x, int y, int cell) {
            g.setColor(Color.BLACK);
            g.fillRect(x * cell, y * cell, 7 * cell, 7 * cell);
            g.setColor(Color.WHITE);
            g.fillRect((x + 1) * cell, (y + 1) * cell, 5 * cell, 5 * cell);
            g.setColor(Color.BLACK);
            g.fillRect((x + 2) * cell, (y + 2) * cell, 3 * cell, 3 * cell);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (qrImage != null) {
                // Centraliza a imagem
                int x = (getWidth() - qrImage.getWidth()) / 2;
                int y = (getHeight() - qrImage.getHeight()) / 2;
                g.drawImage(qrImage, x, y, this);
                
                g.setColor(Color.BLACK);
                g.drawString("Escaneie para pagar", x + 40, y + 215);
            }
        }
    }
}