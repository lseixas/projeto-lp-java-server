package com.cvetti.client;

import com.cvetti.client.screens.*;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public class ClientApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private SocketClient socketClient;
    private ResourceBundle messages;
    private JSONObject loggedUser;

    // --- IDENTIFICADOR ÚNICO DA INSTÂNCIA (JANELA) ---
    // Gera um código curto (ex: "A1B2C") ao abrir o app para o servidor saber quem é quem
    private final String instanceId = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

    // Componentes Visuais Globais
    private JLabel lblConnectionStatus;

    // Telas (MVC)
    private LoginScreen loginScreen;
    private RegisterScreen registerScreen;
    private MenuScreen menuScreen;
    private TransactionScreen transactionScreen;
    private EditUserScreen editUserScreen;
    private PixScreen pixScreen; // Tela do Pix

    public ClientApp() {
        // 1. Configurações Iniciais
        socketClient = new SocketClient("localhost", 8080);
        loadBundle(Locale.forLanguageTag("pt-BR"));

        initializeUI();
        
        // 2. Inicia o monitoramento de conexão (Heartbeat)
        startConnectionMonitor(); 
        
        // 3. Hook para avisar o servidor ao fechar a janela
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendDisconnectSignal();
                super.windowClosing(e);
            }
        });

        // 4. Mostra a tela inicial
        showScreen("LOGIN");
    }

    private void initializeUI() {
        this.setTitle("Banco Cvetti - Cliente [" + instanceId + "]"); // Mostra ID no título também
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1650, 900); // Altura ajustada para caber tudo
        this.setLocationRelativeTo(null);

        // --- Painel Central (CardLayout para trocar telas) ---
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Inicializa as telas passando 'this' (Controlador)
        loginScreen = new LoginScreen(this);
        registerScreen = new RegisterScreen(this);
        menuScreen = new MenuScreen(this);
        transactionScreen = new TransactionScreen(this);
        editUserScreen = new EditUserScreen(this);
        pixScreen = new PixScreen(this);

        // Adiciona telas ao painel principal
        mainPanel.add(loginScreen, "LOGIN");
        mainPanel.add(registerScreen, "REGISTER");
        mainPanel.add(menuScreen, "MENU");
        mainPanel.add(transactionScreen, "TRANSACTION");
        mainPanel.add(editUserScreen, "EDIT");
        mainPanel.add(pixScreen, "PIX");

        this.add(mainPanel, BorderLayout.CENTER);
        
        // --- Barra Inferior (Status + Idioma) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Label de Status
        lblConnectionStatus = new JLabel("Verificando...");
        lblConnectionStatus.setOpaque(true);
        lblConnectionStatus.setFont(new Font("Arial", Font.BOLD, 12));
        bottomPanel.add(lblConnectionStatus, BorderLayout.WEST);

        // Botões de Idioma
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton btnPt = new JButton("PT");
        btnPt.addActionListener(e -> updateLanguage(Locale.forLanguageTag("pt-BR")));
        JButton btnEn = new JButton("EN");
        btnEn.addActionListener(e -> updateLanguage(Locale.forLanguageTag("en-US")));
        JButton btnEs = new JButton("ES");
        btnEs.addActionListener(e -> updateLanguage(Locale.forLanguageTag("es-ES")));
        JButton btnFr = new JButton("FR");
        btnFr.addActionListener(e -> updateLanguage(Locale.forLanguageTag("fr-FR")));
        JButton btnDe = new JButton("DE");
        btnDe.addActionListener(e -> updateLanguage(Locale.forLanguageTag("de-DE")));
        JButton btnIt = new JButton("IT");
        btnIt.addActionListener(e -> updateLanguage(Locale.forLanguageTag("it-IT")));
        JButton btnJa = new JButton("JP");
        btnJa.addActionListener(e -> updateLanguage(Locale.forLanguageTag("ja-JP")));

        langPanel.add(btnPt);
        langPanel.add(btnEn);
        langPanel.add(btnEs);
        langPanel.add(btnFr);
        langPanel.add(btnDe);
        langPanel.add(btnIt);
        langPanel.add(btnJa);

        bottomPanel.add(langPanel, BorderLayout.EAST);

        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- LÓGICA DE CONEXÃO (Heartbeat com ID) ---
    private void startConnectionMonitor() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    // Cria pacote de "estou vivo" com o ID da janela
                    JSONObject json = new JSONObject();
                    json.put("action", "heartbeat");
                    json.put("instanceId", instanceId);
                    
                    // Envia e espera resposta rápida
                    String response = socketClient.send(json.toString());
                    boolean isOnline = (response != null);

                    // Atualiza UI na Thread correta
                    SwingUtilities.invokeLater(() -> updateConnectionUI(isOnline));

                    Thread.sleep(3000); // Verifica a cada 3 segundos
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void updateConnectionUI(boolean isOnline) {
        if (isOnline) {
            lblConnectionStatus.setText(" ● Online ");
            lblConnectionStatus.setForeground(new Color(0, 150, 0)); // Verde
        } else {
            lblConnectionStatus.setText(" ● Offline (Servidor Indisponível) ");
            lblConnectionStatus.setForeground(Color.RED);
        }
    }

    private void sendDisconnectSignal() {
        // Envia aviso de saída em thread separada
        Thread t = new Thread(() -> {
            JSONObject json = new JSONObject();
            json.put("action", "disconnect");
            json.put("instanceId", instanceId); // Avisa quem está saindo
            socketClient.send(json.toString());
        });
        t.start();
        try { t.join(500); } catch (InterruptedException ignored) {} 
    }

    // --- MÉTODOS DE COMUNICAÇÃO ---

    public void sendRequest(JSONObject json, ResponseHandler handler) {
        // Validação prévia de conexão
        if (!socketClient.isConnected()) {
             JOptionPane.showMessageDialog(this, messages.getString("msg.connection_error"));
             return;
        }
        
        // Garante que toda requisição envie o ID da instância (útil para logs no server)
        json.put("instanceId", instanceId);

        new Thread(() -> {
            String responseStr = socketClient.send(json.toString());
            SwingUtilities.invokeLater(() -> {
                if (responseStr == null) {
                    JOptionPane.showMessageDialog(this, messages.getString("msg.connection_error"));
                } else {
                    handler.handle(new JSONObject(responseStr));
                }
            });
        }).start();
    }

    // --- NAVEGAÇÃO E ESTADO ---

    public void showScreen(String screenName) {
        cardLayout.show(mainPanel, screenName);
    }

    public void logout() {
        this.loggedUser = null;
        loginScreen.clearFields();
        showScreen("LOGIN");
    }

    public void setLoggedUser(JSONObject user) {
        this.loggedUser = user;
        menuScreen.updateUserName(user.optString("name"));
    }

    // --- GETTERS E UTILITÁRIOS ---

    public JSONObject getLoggedUser() { return loggedUser; }
    public ResourceBundle getMessages() { return messages; }
    
    // Getters para as telas acessarem umas às outras
    public TransactionScreen getTransactionScreen() { return transactionScreen; }
    public EditUserScreen getEditUserScreen() { return editUserScreen; }
    public PixScreen getPixScreen() { return pixScreen; }

    private void updateLanguage(Locale locale) {
        loadBundle(locale);
        // Propaga a mudança para todas as telas
        loginScreen.updateTexts();
        registerScreen.updateTexts();
        menuScreen.updateTexts();
        transactionScreen.updateTexts();
        editUserScreen.updateTexts();
        pixScreen.updateTexts();
    }

    private void loadBundle(Locale locale) {
        messages = ResourceBundle.getBundle("messages", locale);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new ClientApp().setVisible(true));
    }

    // Interface para callback
    public interface ResponseHandler {
        void handle(JSONObject response);
    }
}