package com.cvetti.server;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;
import java.util.ResourceBundle;

public class ClientApp extends JFrame {

    private ResourceBundle messages;
    private SocketClient socketClient;

    // Componentes de UI
    private JTabbedPane tabbedPane;
    private JTextField txtId, txtName, txtEmail, txtCpf, txtBalance, txtSearchId;
    private JPasswordField txtPassword;
    private JTextArea txtResult;
    private JButton btnCreate, btnSearch, btnClear;
    private JLabel lblStatus;
    private JComboBox<String> langSelector;
    
    // Labels para atualização dinâmica
    private JLabel lId, lName, lEmail, lCpf, lPass, lBal, lSearchId, lLang;

    public ClientApp() {
        // Carrega locale padrão (pode forçar new Locale("pt", "BR") se quiser)
        loadBundle(Locale.getDefault());
        
        // Inicializa conexão
        socketClient = new SocketClient("localhost", 8080);
        
        initComponents();
        setupLayout();
        setupListeners();
        updateTexts();
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500, 450);
        this.setLocationRelativeTo(null);
    }

    private void loadBundle(Locale locale) {
        // Certifique-se de que os arquivos .properties estão no classpath
        messages = ResourceBundle.getBundle("messages", locale);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Campos Cadastro
        txtId = new JTextField(20);
        txtName = new JTextField(20);
        txtEmail = new JTextField(20);
        txtCpf = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtBalance = new JTextField(20);
        btnCreate = new JButton();
        btnClear = new JButton();

        // Campos Busca
        txtSearchId = new JTextField(20);
        btnSearch = new JButton();
        txtResult = new JTextArea(10, 30);
        txtResult.setEditable(false);

        // Labels (inicializados vazios, preenchidos por updateTexts)
        lId = new JLabel(); lName = new JLabel(); lEmail = new JLabel();
        lCpf = new JLabel(); lPass = new JLabel(); lBal = new JLabel();
        lSearchId = new JLabel(); lLang = new JLabel();

        lblStatus = new JLabel("Status: ...");
        
        // Seletor de Idioma
        langSelector = new JComboBox<>(new String[]{"Português", "English"});
        if (Locale.getDefault().getLanguage().equals("en")) {
            langSelector.setSelectedIndex(1);
        }
    }

    private void setupLayout() {
        // Painel de Cadastro
        JPanel pnlRegister = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        addFormRow(pnlRegister, lId, txtId, gbc, 0);
        addFormRow(pnlRegister, lName, txtName, gbc, 1);
        addFormRow(pnlRegister, lEmail, txtEmail, gbc, 2);
        addFormRow(pnlRegister, lCpf, txtCpf, gbc, 3);
        addFormRow(pnlRegister, lPass, txtPassword, gbc, 4);
        addFormRow(pnlRegister, lBal, txtBalance, gbc, 5);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnCreate);
        btnPanel.add(btnClear);
        pnlRegister.add(btnPanel, gbc);

        // Painel de Busca
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 10));
        JPanel topSearch = new JPanel();
        topSearch.add(lSearchId);
        topSearch.add(txtSearchId);
        topSearch.add(btnSearch);
        pnlSearch.add(topSearch, BorderLayout.NORTH);
        pnlSearch.add(new JScrollPane(txtResult), BorderLayout.CENTER);

        // Adiciona abas
        tabbedPane.addTab("Register", pnlRegister);
        tabbedPane.addTab("Search", pnlSearch);

        // Barra inferior (Status + Idioma)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel langPanel = new JPanel();
        langPanel.add(lLang);
        langPanel.add(langSelector);
        bottomPanel.add(lblStatus, BorderLayout.WEST);
        bottomPanel.add(langPanel, BorderLayout.EAST);

        this.setLayout(new BorderLayout());
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel p, JLabel l, JComponent c, GridBagConstraints gbc, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        p.add(l, gbc);
        gbc.gridx = 1;
        p.add(c, gbc);
    }

    private void updateTexts() {
        this.setTitle(messages.getString("app.title"));
        
        lId.setText(messages.getString("lbl.id"));
        lName.setText(messages.getString("lbl.name"));
        lEmail.setText(messages.getString("lbl.email"));
        lCpf.setText(messages.getString("lbl.cpf"));
        lPass.setText(messages.getString("lbl.password"));
        lBal.setText(messages.getString("lbl.balance"));
        lSearchId.setText(messages.getString("lbl.id"));
        lLang.setText(messages.getString("lang.select"));

        btnCreate.setText(messages.getString("btn.create"));
        btnClear.setText(messages.getString("btn.clear"));
        btnSearch.setText(messages.getString("btn.search"));

        tabbedPane.setTitleAt(0, messages.getString("tab.register"));
        tabbedPane.setTitleAt(1, messages.getString("tab.search"));
    }

    private void setupListeners() {
        // Ação Criar
        btnCreate.addActionListener(e -> {
            JSONObject json = new JSONObject();
            json.put("action", "create");
            json.put("id", txtId.getText());
            json.put("name", txtName.getText());
            json.put("email", txtEmail.getText());
            json.put("cpf", txtCpf.getText());
            json.put("passwordHash", new String(txtPassword.getPassword())); // Envia cru, server espera hash mas sua app atual aceita string
            json.put("saldo", txtBalance.getText());

            sendRequest(json);
        });

        // Ação Buscar
        btnSearch.addActionListener(e -> {
            JSONObject json = new JSONObject();
            json.put("action", "get");
            json.put("id", txtSearchId.getText());

            sendRequest(json);
        });

        // Limpar
        btnClear.addActionListener(e -> {
            txtId.setText(""); txtName.setText(""); txtEmail.setText("");
            txtCpf.setText(""); txtPassword.setText(""); txtBalance.setText("");
            txtResult.setText("");
        });

        // Troca de Idioma
        langSelector.addActionListener(e -> {
            String selected = (String) langSelector.getSelectedItem();
            if ("English".equals(selected)) {
                loadBundle(new Locale("en", "US"));
            } else {
                loadBundle(new Locale("pt", "BR"));
            }
            updateTexts();
        });
    }

    private void sendRequest(JSONObject json) {
        new Thread(() -> {
            try {
                String responseStr = socketClient.send(json.toString());
                if (responseStr == null) {
                    SwingUtilities.invokeLater(() -> txtResult.setText("Erro de conexão com o servidor."));
                    return;
                }
                
                JSONObject response = new JSONObject(responseStr);
                
                SwingUtilities.invokeLater(() -> {
                    if (response.has("error")) {
                         JOptionPane.showMessageDialog(this, 
                             java.text.MessageFormat.format(messages.getString("msg.error"), response.getString("error")));
                    } else if ("success".equals(response.optString("status"))) {
                        if (response.has("user")) {
                            // É uma resposta de busca
                            txtResult.setText(response.getJSONObject("user").toString(4));
                        } else {
                            // É uma resposta de criação
                            JOptionPane.showMessageDialog(this, messages.getString("msg.success"));
                        }
                    } else if ("not_found".equals(response.optString("status"))) {
                        JOptionPane.showMessageDialog(this, messages.getString("msg.not_found"));
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        // Look and Feel nativo
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> {
            new ClientApp().setVisible(true);
        });
    }
}

// Classe auxiliar simples para Socket (pode ser arquivo separado)
class SocketClient {
    private String host;
    private int port;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Abre, envia, recebe, fecha (Simples e Stateless para evitar timeouts de idle)
    public synchronized String send(String data) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println(data);
            return in.readLine();
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}