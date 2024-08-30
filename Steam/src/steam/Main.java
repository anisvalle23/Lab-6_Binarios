package steam;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    
    private Steam steam;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JPanel loginPanel;
    private JPanel adminPanel;
    private JPanel userPanel;
    
    private JTextField usernameText;
    private JPasswordField passText;
    
    private final String usernameDefault = "admin";
    private final String passDefault = "admin";
    
    public Main() {
        try {
            steam = new Steam();
            initializeUI();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "ERROR!");
            System.exit(1);
        }
    }
    
    private void initializeUI() {
        mainFrame = new JFrame("STEAM");
        mainFrame.setSize(300, 300);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new CardLayout());
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null);
        
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton registerButton = new JButton("Registrar Usuario");
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(registerButton, gbc);
        
        JButton loginButton = new JButton("Iniciar Sesión");
        gbc.gridy = 1;
        mainPanel.add(loginButton, gbc);
        
        JButton exitButton = new JButton("Salir");
        gbc.gridy = 2;
        mainPanel.add(exitButton, gbc);
        
        registerButton.addActionListener(e -> showRegisterPanel());
        loginButton.addActionListener(e -> showLoginPanel());
        exitButton.addActionListener(e -> System.exit(0));
        
        loginPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(userLabel, gbc);
        
        usernameText = new JTextField(15);
        gbc.gridx = 1;
        loginPanel.add(usernameText, gbc);
        
        JLabel passLabel = new JLabel("Password:");
        gbc.gridy = 1;
        gbc.gridx = 0;
        loginPanel.add(passLabel, gbc);
        
        passText = new JPasswordField(15);
        gbc.gridx = 1;
        loginPanel.add(passText, gbc);
        
        JButton loginSubmitButton = new JButton("Login");
        gbc.gridy = 2;
        gbc.gridx = 0;
        loginPanel.add(loginSubmitButton, gbc);
        
        JButton loginBackButton = new JButton("Volver");
        gbc.gridx = 1;
        loginPanel.add(loginBackButton, gbc);
        
        loginSubmitButton.addActionListener(e -> {
            try {
                handleLogin();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        loginBackButton.addActionListener(e -> showMainPanel());
        
        adminPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton addGameButton = new JButton("Agregar Juego");
        gbc.gridx = 0;
        gbc.gridy = 0;
        adminPanel.add(addGameButton, gbc);
        
        JButton modifyGameButton = new JButton("Moificar Juego");
        gbc.gridy = 1;
        adminPanel.add(modifyGameButton, gbc);
        
        JButton deleteGameButton = new JButton("Eliminar Juego");
        gbc.gridy = 2;
        adminPanel.add(deleteGameButton, gbc);
        
        JButton generateReportButton = new JButton("Generar Reporte");
        gbc.gridy = 3;
        adminPanel.add(generateReportButton, gbc);
        
        JButton logoutButtonAdmin = new JButton("Cerrar Sesion");
        gbc.gridy = 4;
        adminPanel.add(logoutButtonAdmin, gbc);
        
        addGameButton.addActionListener(e -> addGame());
        modifyGameButton.addActionListener(e -> modifyGame());
        deleteGameButton.addActionListener(e -> deleteGame());
        generateReportButton.addActionListener(e -> generateReport());
        logoutButtonAdmin.addActionListener(e -> logout());
        
        userPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton viewCatalogButton = new JButton("Ver Catalogo de Juegos");
        gbc.gridx = 0;
        gbc.gridy = 0;
        userPanel.add(viewCatalogButton, gbc);
        
        JButton downloadGameButton = new JButton("Descargar Juego");
        gbc.gridy = 1;
        userPanel.add(downloadGameButton, gbc);

        JButton viewProfileButton = new JButton("Ver Perfil");
        gbc.gridy = 2;
        userPanel.add(viewProfileButton, gbc);
        
        JButton logoutButtonUser = new JButton("Cerrar Sesion");
        gbc.gridy = 3;
        userPanel.add(logoutButtonUser, gbc);

        viewCatalogButton.addActionListener(e -> viewCatalog());
        downloadGameButton.addActionListener(e -> downloadGame());
        viewProfileButton.addActionListener(e -> viewProfile());
        logoutButtonUser.addActionListener(e -> logout());
        
        mainFrame.add(mainPanel, "mainPanel");
        mainFrame.add(loginPanel, "loginPanel");
        mainFrame.add(adminPanel, "adminPanel");
        mainFrame.add(userPanel, "userPanel");

        showMainPanel();
        mainFrame.setVisible(true);
    }

    private void logout() {
        usernameText.setText("");
        passText.setText("");
        
        showMainPanel();
    }
    
    private void showMainPanel() {
        CardLayout cl = (CardLayout) mainFrame.getContentPane().getLayout();
        cl.show(mainFrame.getContentPane(), "mainPanel");
    }
    
    private void showLoginPanel() {
        CardLayout cl = (CardLayout) mainFrame.getContentPane().getLayout();
        cl.show(mainFrame.getContentPane(), "loginPanel");
    }
    
    private void handleLogin() throws IOException {
        String username = usernameText.getText();
        String password = new String(passText.getPassword());
        
        if (isDefaultAdmin(username, password) || isRegisteredAdmin(username, password)) {
            showAdminPanel();
        } else {
            try {
                Player player = steam.getPlayerByUsername(username);
                if (player != null && player.checkPassword(password)) {
                    showUserPanel();
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Usuario/Contraseña incorrectos");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "ERROR!");
            }
        }
    }
    
    private boolean isDefaultAdmin(String username, String password) {
        return username.equals(usernameDefault) && password.equals(passDefault);
    }
    
    private boolean isRegisteredAdmin(String username, String password) throws IOException {
        Player player = steam.getPlayerByUsername(username);
        return player != null && player.checkPassword(password) && player.getTipoUsuario().equals("admin");
    }
    
    private void showAdminPanel() {
        CardLayout cl = (CardLayout) mainFrame.getContentPane().getLayout();
        cl.show(mainFrame.getContentPane(), "adminPanel");
    }
    
    private void showUserPanel() {
        CardLayout cl = (CardLayout) mainFrame.getContentPane().getLayout();
        cl.show(mainFrame.getContentPane(), "userPanel");
    }
    
    private void showRegisterPanel() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField nombreField = new JTextField();
        JTextField tipoUsuarioField = new JTextField();
        
        SpinnerModel model = new SpinnerDateModel();
        JSpinner dateOfBirthSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateOfBirthSpinner, "dd/MM/yyyy");
        dateOfBirthSpinner.setEditor(editor);
        
        JPanel registerPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        registerPanel.add(new JLabel("Username:"));
        registerPanel.add(usernameField);
        registerPanel.add(new JLabel("Password:"));
        registerPanel.add(passwordField);
        registerPanel.add(new JLabel("Nombre:"));
        registerPanel.add(nombreField);
        registerPanel.add(new JLabel("Tipo de usuario (normal/admin):"));
        registerPanel.add(tipoUsuarioField);
        registerPanel.add(new JLabel("Fecha de Nacimiento:"));
        registerPanel.add(dateOfBirthSpinner);
        
        int result = JOptionPane.showConfirmDialog(mainFrame, registerPanel, "Registrar Usuario", 
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String nombre = nombreField.getText();
            String tipoUsuario = tipoUsuarioField.getText().toLowerCase();
            Date dateOfBirth = (Date) dateOfBirthSpinner.getValue();
            Calendar nacimiento = Calendar.getInstance();
            nacimiento.setTime(dateOfBirth);
            
            if (!username.isEmpty() && !password.isEmpty() && !nombre.isEmpty() 
                    && (tipoUsuario.equals("normal") || tipoUsuario.equals("admin"))) {
                try {
                    steam.addPlayer(username, password, nombre, nacimiento, tipoUsuario);
                    JOptionPane.showMessageDialog(mainFrame, "Usuario registrado exitosamente!");
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Error al registrar el usuario");
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Datos no validos. Ingresar de nuevo");
            }
        }
    }
    
    private void addGame() {
        JTextField titleField = new JTextField();
        JTextField soField = new JTextField();
        JTextField edadMinimaField = new JTextField();
        JTextField precioField = new JTextField();

        JPanel addGamePanel = new JPanel(new GridLayout(6, 2, 10, 10));
        addGamePanel.add(new JLabel("Titulo:"));
        addGamePanel.add(titleField);
        addGamePanel.add(new JLabel("Sistema operativo (W/M/L):"));
        addGamePanel.add(soField);
        addGamePanel.add(new JLabel("Edad minima:"));
        addGamePanel.add(edadMinimaField);
        addGamePanel.add(new JLabel("Precio:"));
        addGamePanel.add(precioField);
        
        JButton selectImageButton = new JButton("Seleccionar imagen");
        JLabel selectedImageLabel = new JLabel("No ha seleccionado ninguna imagen");
        addGamePanel.add(selectImageButton);
        addGamePanel.add(selectedImageLabel);
        
        final String[] imagePath = {null};
        selectImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(mainFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                imagePath[0] = fileChooser.getSelectedFile().getAbsolutePath();
                selectedImageLabel.setText(fileChooser.getSelectedFile().getName());
            }
        });
        
        int result = JOptionPane.showConfirmDialog(mainFrame, addGamePanel, "Agregar Juego", 
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String titulo = titleField.getText();
            char so = soField.getText().toUpperCase().charAt(0);
            int edadMinima = Integer.parseInt(edadMinimaField.getText());
            double precio = Double.parseDouble(precioField.getText());
            
            if (!titulo.isEmpty() && (so == 'W' || so == 'M' || so == 'L') && edadMinima > 0 
                    && precio > 0 && imagePath[0] != null) {
                try {
                    steam.addGame(titulo, so, edadMinima, precio, imagePath[0]);
                    JOptionPane.showMessageDialog(mainFrame, "Juego agregado exitosamente!");
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Error al agregar juego");
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Datos no validos. Por favor, ingresar de nuevo");
            }
        }
    }
    
    private void modifyGame() {
        JTextField gameCodeField = new JTextField();
        JTextField newTitleField = new JTextField();
        JTextField newSoField = new JTextField();
        JTextField newAgeField = new JTextField();
        JTextField newPriceField = new JTextField();
        
        JPanel modifyGamePanel = new JPanel(new GridLayout(6, 2, 10, 10));
        modifyGamePanel.add(new JLabel("Codigo del juego:"));
        modifyGamePanel.add(gameCodeField);
        modifyGamePanel.add(new JLabel("Nuevo titulo:"));
        modifyGamePanel.add(newTitleField);
        modifyGamePanel.add(new JLabel("Nuevo sistema operativo (W/M/L):"));
        modifyGamePanel.add(newSoField);
        modifyGamePanel.add(new JLabel("Nueva edad minima:"));
        modifyGamePanel.add(newAgeField);
        modifyGamePanel.add(new JLabel("Nuevo precio:"));
        modifyGamePanel.add(newPriceField);
        
        int result = JOptionPane.showConfirmDialog(mainFrame, modifyGamePanel, "Modificar Juego", 
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int gameCode = Integer.parseInt(gameCodeField.getText());
                String newTitle = newTitleField.getText();
                char newSo = newSoField.getText().toUpperCase().charAt(0);
                int newAge = Integer.parseInt(newAgeField.getText());
                double newPrice = Double.parseDouble(newPriceField.getText());

                if (steam.updateGameDetails(gameCode, newTitle, newSo, newAge, newPrice)) {
                    JOptionPane.showMessageDialog(mainFrame, "Juego modificado exitosamente!");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "El juego no fue encontrado");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Error al modificar el juego");
            }
        }
    }
    
    private void deleteGame() {
        JTextField gameCodeField = new JTextField();
        
        JPanel deleteGamePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        deleteGamePanel.add(new JLabel("Codigo del juego:"));
        deleteGamePanel.add(gameCodeField);
        
        int result = JOptionPane.showConfirmDialog(mainFrame, deleteGamePanel, "Eliminar Juego", 
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int gameCode = Integer.parseInt(gameCodeField.getText());

                if (steam.deleteGame(gameCode)) {
                    JOptionPane.showMessageDialog(mainFrame, "Juego eliminado exitosamente!");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "El juego no fue encontrado");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Error al eliminar el juego");
            }
        }
    }
    
    private void generateReport() {
        JTextField clientCodeField = new JTextField();
        JTextField fileNameField = new JTextField();
        
        JPanel reportPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        reportPanel.add(new JLabel("Codigo del cliente:"));
        reportPanel.add(clientCodeField);
        reportPanel.add(new JLabel("Nombre del archivo de reporte:"));
        reportPanel.add(fileNameField);
        
        int result = JOptionPane.showConfirmDialog(mainFrame, reportPanel, "Generar Reporte", 
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int clientCode = Integer.parseInt(clientCodeField.getText());
            String fileName = fileNameField.getText();
            
            if (!fileName.isEmpty()) {
                try {
                    steam.reportForClient(clientCode, fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Error al generar el reporte");
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Nombre de archivo no valido");
            }
        }
    }
    
    private void viewCatalog() {
        try {
            steam.printGames(mainFrame);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error al mostrar el catálogo.");
        }
    }
    
    private void downloadGame() {
        JTextField gameCodeField = new JTextField();
        JTextField clientCodeField = new JTextField();
        JTextField soField = new JTextField();
        
        JPanel downloadPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        downloadPanel.add(new JLabel("Codigo del juego:"));
        downloadPanel.add(gameCodeField);
        downloadPanel.add(new JLabel("Codigo del cliente:"));
        downloadPanel.add(clientCodeField);
        downloadPanel.add(new JLabel("Sistema operativo (W/M/L):"));
        downloadPanel.add(soField);
        
        int result = JOptionPane.showConfirmDialog(mainFrame, downloadPanel, "Descargar Juego", 
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int gameCode = Integer.parseInt(gameCodeField.getText());
            int clientCode = Integer.parseInt(clientCodeField.getText());
            char so = soField.getText().toUpperCase().charAt(0);
            
            try {
                boolean success = steam.downloadGame(gameCode, clientCode, so);
                if (success) {
                    JOptionPane.showMessageDialog(mainFrame, "Juego descargado exitosamente!");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "No se pudo descargar, verificar datos");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Error al descargar el juego");
            }
        }
    }
    
    private void viewProfile() {
        try {
            Player player = steam.getPlayerByUsername(usernameText.getText());
            if (player != null) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Codigo: " + player.getCode() + "\n"
                        + "Username: " + player.getUsername() + "\n"
                        + "Nombre: " + player.getNombre() + "\n"
                        + "Fecha de nacimiento: " + player.getFormattedNacimiento() + "\n"
                        + "Descargas: " + player.getContadorDownloads() + "\n"
                        + "Tipo de usuario: " + player.getTipoUsuario());
            } else {
                JOptionPane.showMessageDialog(mainFrame, "El perfil no fue encontrado");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error al mostrar el perfil");
        }
    }
    
    public static void main(String[] args) {
        new Main();
    }
}
