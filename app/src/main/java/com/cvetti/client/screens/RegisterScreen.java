package com.cvetti.client.screens;

import com.cvetti.client.ClientApp;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterScreen extends JPanel {
    private ClientApp app;
    private JTextField txtName, txtEmail, txtCpf, txtPhone, txtBalance;
    private JPasswordField txtPass;
    private JButton btnRegister, btnBack;
    private JLabel lblImage, lblName, lblEmail, lblCpf, lblPhone, lblPass, lblBal;

    // --- REGEX DE VALIDAÇÃO ---
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+\\d{2}\\(\\d{2}\\)\\d{9}$");
    private static final Pattern BALANCE_PATTERN = Pattern.compile("^\\d+,\\d{2}$");

    public RegisterScreen(ClientApp app) {
        this.app = app;
        // Layout principal com mais espaçamento entre os componentes
        setLayout(new BorderLayout(20, 20));
        // Margem externa geral
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); 
        initComponents();
        updateTexts();
    }

    private void initComponents() {
        // =================================================================
        // 1. PAINEL DA DIREITA (IMAGEM MAIOR)
        // =================================================================
        JPanel pnlRightImage = new JPanel(new GridBagLayout()); // GridBag centraliza bem a imagem
        try {
            URL imgUrl = getClass().getResource("/img/Revenue-amico.png");
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                
                // --- AUMENTANDO O TAMANHO ---
                // Escala para 350px de LARGURA, altura automática (-1) para manter proporção.
                Image img = originalIcon.getImage().getScaledInstance(350, -1, Image.SCALE_SMOOTH);
                
                lblImage = new JLabel(new ImageIcon(img));
                pnlRightImage.add(lblImage);
                // Adiciona uma margem à esquerda da imagem para não grudar no formulário
                pnlRightImage.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            } else {
                System.err.println("Imagem não encontrada: /img/Revenue-amico.png");
                pnlRightImage.add(new JLabel("[Imagem não encontrada]"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Adiciona o painel da imagem na DIREITA (EAST) do layout principal
        add(pnlRightImage, BorderLayout.EAST);


        // =================================================================
        // 2. PAINEL DA ESQUERDA (FORMULÁRIO + BOTÕES)
        // =================================================================
        // Criamos um container para agrupar o formulário e os botões no lado esquerdo
        JPanel pnlLeftContainer = new JPanel(new BorderLayout(10, 20));

        // --- 2a. Formulário (Centro do Container Esquerdo) ---
        JPanel pnlForm = new JPanel(new GridLayout(6, 2, 10, 15)); // Mais espaçamento vertical
        
        lblName = new JLabel(); pnlForm.add(lblName);
        txtName = new JTextField(); pnlForm.add(txtName);

        lblEmail = new JLabel(); pnlForm.add(lblEmail);
        txtEmail = new JTextField(); pnlForm.add(txtEmail);

        lblCpf = new JLabel(); pnlForm.add(lblCpf);
        txtCpf = new JTextField(); 
        txtCpf.setToolTipText("Ex: 123.456.789-00"); 
        pnlForm.add(txtCpf);

        lblPhone = new JLabel(); pnlForm.add(lblPhone);
        txtPhone = new JTextField(); 
        txtPhone.setToolTipText("Ex: +55(11)971775742"); 
        pnlForm.add(txtPhone);

        lblPass = new JLabel(); pnlForm.add(lblPass);
        txtPass = new JPasswordField(); pnlForm.add(txtPass);

        lblBal = new JLabel(); pnlForm.add(lblBal);
        txtBalance = new JTextField();
        txtBalance.setToolTipText("Formato obrigatório: 00,00 (Ex: 100,50)"); 
        pnlForm.add(txtBalance);

        pnlLeftContainer.add(pnlForm, BorderLayout.CENTER);

        // --- 2b. Botões (Base do Container Esquerdo) ---
        JPanel pnlButtons = new JPanel(new GridLayout(1, 2, 15, 0)); // Mais espaçamento entre botões
        // Botões mais altos
        btnBack = new JButton();
        btnBack.setPreferredSize(new Dimension(0, 40));
        btnBack.addActionListener(e -> app.showScreen("LOGIN"));
        pnlButtons.add(btnBack);

        btnRegister = new JButton();
        btnRegister.setPreferredSize(new Dimension(0, 40));
        // Cor de destaque para o botão principal (opcional, fica bonito com a imagem)
        btnRegister.setBackground(new Color(70, 130, 180)); 
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegister.addActionListener(e -> doRegister());
        pnlButtons.add(btnRegister);

        pnlLeftContainer.add(pnlButtons, BorderLayout.SOUTH);

        // Adiciona o container esquerdo ao CENTRO do layout principal
        add(pnlLeftContainer, BorderLayout.CENTER);
    }

    // ... (O restante dos métodos validateFields, doRegister, clearFields, updateTexts
    // permanecem EXATAMENTE iguais ao código anterior, não precisa alterar nada lá) ...
    
    private boolean validateFields() {
        if (txtName.getText().trim().length() < 3) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.name")); return false;
        }
        if (!EMAIL_PATTERN.matcher(txtEmail.getText()).matches()) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.email")); return false;
        }
        if (!CPF_PATTERN.matcher(txtCpf.getText()).matches()) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.cpf_fmt")); return false;
        }
        if (!PHONE_PATTERN.matcher(txtPhone.getText()).matches()) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.phone")); return false;
        }
        if (new String(txtPass.getPassword()).length() < 6) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.pass")); return false;
        }
        String balText = txtBalance.getText().trim();
        if (!BALANCE_PATTERN.matcher(balText).matches()) {
            JOptionPane.showMessageDialog(this, "Formato de saldo inválido!\nUse vírgula e dois centavos.\nExemplo correto: 50,00"); return false;
        }
        try {
            String valStr = balText.replace(",", ".");
            if (new BigDecimal(valStr).compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, app.getMessages().getString("val.balance")); return false;
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
        json.put("password", new String(txtPass.getPassword()));
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
        txtName.setText(""); txtEmail.setText(""); txtCpf.setText(""); 
        txtPhone.setText(""); txtPass.setText(""); txtBalance.setText("");
    }

    public void updateTexts() {
        lblName.setText(app.getMessages().getString("lbl.name"));
        lblEmail.setText(app.getMessages().getString("lbl.email"));
        lblCpf.setText(app.getMessages().getString("lbl.cpf"));
        lblPhone.setText(app.getMessages().getString("lbl.phone"));
        lblPass.setText(app.getMessages().getString("lbl.password"));
        lblBal.setText(app.getMessages().getString("lbl.balance"));
        btnRegister.setText(app.getMessages().getString("btn.create"));
        btnBack.setText(app.getMessages().getString("btn.back"));
    }
}