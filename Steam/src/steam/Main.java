package steam;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Calendar;

public class Main {

    private Steam steam;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JPanel loginPanel;
    private JPanel adminPanel;
    private JPanel userPanel;

    private JTextField usernameField;
    private JPasswordField passwordField;

    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin";

    public Main() {
        try {
            steam = new Steam();
            initializeUI();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error inicializando el sistema de Steam.");
            System.exit(1);
        }
    }

    private void initializeUI() {
        mainFrame = new JFrame("Steam");
        mainFrame.setSize(800, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new CardLayout()); // Asegurarse de que esté utilizando CardLayout

        mainPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton registerButton = new JButton("Registrar Usuario");
        JButton loginButton = new JButton("Iniciar Sesión");
        JButton exitButton = new JButton("Salir");

        mainPanel.add(registerButton);
        mainPanel.add(loginButton);
        mainPanel.add(exitButton);

        registerButton.addActionListener(e -> showRegisterPanel());
        loginButton.addActionListener(e -> showLoginPanel());
        exitButton.addActionListener(e -> System.exit(0));

        loginPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        JButton loginSubmitButton = new JButton("Login");
        JButton loginBackButton = new JButton("Back");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel());
        loginPanel.add(loginSubmitButton);
        loginPanel.add(loginBackButton);

        loginSubmitButton.addActionListener(e -> handleLogin());
        loginBackButton.addActionListener(e -> showMainPanel());

        adminPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton addGameButton = new JButton("Agregar Juego");
        JButton modifyGameButton = new JButton("Modificar Juego");
        JButton deleteGameButton = new JButton("Eliminar Juego");
        JButton generateReportButton = new JButton("Generar Reporte");

        adminPanel.add(addGameButton);
        adminPanel.add(modifyGameButton);
        adminPanel.add(deleteGameButton);
        adminPanel.add(generateReportButton);

        addGameButton.addActionListener(e -> addGame());
        modifyGameButton.addActionListener(e -> modifyGame());
        deleteGameButton.addActionListener(e -> deleteGame());
        generateReportButton.addActionListener(e -> generateReport());

        userPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton viewCatalogButton = new JButton("Ver Catálogo de Juegos");
        JButton downloadGameButton = new JButton("Descargar Juego");
        JButton viewProfileButton = new JButton("Ver Perfil");

        userPanel.add(viewCatalogButton);
        userPanel.add(downloadGameButton);
        userPanel.add(viewProfileButton);

        viewCatalogButton.addActionListener(e -> viewCatalog());
        downloadGameButton.addActionListener(e -> downloadGame());
        viewProfileButton.addActionListener(e -> viewProfile());

        mainFrame.add(mainPanel, "mainPanel");
        mainFrame.add(loginPanel, "loginPanel");
        mainFrame.add(adminPanel, "adminPanel");
        mainFrame.add(userPanel, "userPanel");

        showMainPanel();
        mainFrame.setVisible(true);
    }

    private void showMainPanel() {
        CardLayout cl = (CardLayout) mainFrame.getContentPane().getLayout();
        cl.show(mainFrame.getContentPane(), "mainPanel");
    }

    private void showLoginPanel() {
        CardLayout cl = (CardLayout) mainFrame.getContentPane().getLayout();
        cl.show(mainFrame.getContentPane(), "loginPanel");
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            showAdminPanel();
        } else {
            try {
                Player player = steam.getPlayerByUsername(username);
                if (player != null && player.checkPassword(password)) {
                    showUserPanel();
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Usuario o contraseña incorrectos.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Error en la autenticación.");
            }
        }
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

        JPanel registerPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        registerPanel.add(new JLabel("Username:"));
        registerPanel.add(usernameField);
        registerPanel.add(new JLabel("Password:"));
        registerPanel.add(passwordField);
        registerPanel.add(new JLabel("Nombre:"));
        registerPanel.add(nombreField);
        registerPanel.add(new JLabel("Tipo de Usuario (normal/admin):"));
        registerPanel.add(tipoUsuarioField);

        int result = JOptionPane.showConfirmDialog(mainFrame, registerPanel, "Registrar Usuario", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String nombre = nombreField.getText();
            String tipoUsuario = tipoUsuarioField.getText().toLowerCase();

            if (!username.isEmpty() && !password.isEmpty() && !nombre.isEmpty() && (tipoUsuario.equals("normal") || tipoUsuario.equals("admin"))) {
                try {
                    Calendar nacimiento = Calendar.getInstance();
                    steam.addPlayer(username, password, nombre, nacimiento, tipoUsuario);
                    JOptionPane.showMessageDialog(mainFrame, "Usuario registrado exitosamente.");
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Error al registrar el usuario.");
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Datos inválidos. Por favor, complete todos los campos.");
            }
        }
    }

    private void addGame() {
        JTextField titleField = new JTextField();
        JTextField soField = new JTextField();
        JTextField edadMinimaField = new JTextField();
        JTextField precioField = new JTextField();

        JPanel addGamePanel = new JPanel(new GridLayout(6, 2, 10, 10));
        addGamePanel.add(new JLabel("Título:"));
        addGamePanel.add(titleField);
        addGamePanel.add(new JLabel("Sistema Operativo (W/M/L):"));
        addGamePanel.add(soField);
        addGamePanel.add(new JLabel("Edad Mínima:"));
        addGamePanel.add(edadMinimaField);
        addGamePanel.add(new JLabel("Precio:"));
        addGamePanel.add(precioField);

        // Agregar un botón para seleccionar la imagen
        JButton selectImageButton = new JButton("Seleccionar Imagen");
        JLabel selectedImageLabel = new JLabel("No se ha seleccionado ninguna imagen");
        addGamePanel.add(selectImageButton);
        addGamePanel.add(selectedImageLabel);

        // Selector de archivos para la imagen
        final String[] imagePath = {null};
        selectImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(mainFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                imagePath[0] = fileChooser.getSelectedFile().getAbsolutePath();
                selectedImageLabel.setText(fileChooser.getSelectedFile().getName());
            }
        });

        int result = JOptionPane.showConfirmDialog(mainFrame, addGamePanel, "Agregar Juego", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String titulo = titleField.getText();
            char so = soField.getText().toUpperCase().charAt(0);
            int edadMinima = Integer.parseInt(edadMinimaField.getText());
            double precio = Double.parseDouble(precioField.getText());

            if (!titulo.isEmpty() && (so == 'W' || so == 'M' || so == 'L') && edadMinima > 0 && precio > 0 && imagePath[0] != null) {
                try {
                    steam.addGame(titulo, so, edadMinima, precio, imagePath[0]);
                    JOptionPane.showMessageDialog(mainFrame, "Juego agregado exitosamente.");
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Error al agregar el juego.");
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Datos inválidos. Por favor, complete todos los campos.");
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
        modifyGamePanel.add(new JLabel("Código del Juego:"));
        modifyGamePanel.add(gameCodeField);
        modifyGamePanel.add(new JLabel("Nuevo Título:"));
        modifyGamePanel.add(newTitleField);
        modifyGamePanel.add(new JLabel("Nuevo Sistema Operativo (W/M/L):"));
        modifyGamePanel.add(newSoField);
        modifyGamePanel.add(new JLabel("Nueva Edad Mínima:"));
        modifyGamePanel.add(newAgeField);
        modifyGamePanel.add(new JLabel("Nuevo Precio:"));
        modifyGamePanel.add(newPriceField);

        int result = JOptionPane.showConfirmDialog(mainFrame, modifyGamePanel, "Modificar Juego", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int gameCode = Integer.parseInt(gameCodeField.getText());
                String newTitle = newTitleField.getText();
                char newSo = newSoField.getText().toUpperCase().charAt(0);
                int newAge = Integer.parseInt(newAgeField.getText());
                double newPrice = Double.parseDouble(newPriceField.getText());

                if (steam.updateGameDetails(gameCode, newTitle, newSo, newAge, newPrice)) {
                    JOptionPane.showMessageDialog(mainFrame, "Juego modificado exitosamente.");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Juego no encontrado.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Error al modificar el juego.");
            }
        }
    }

    private void deleteGame() {
        JTextField gameCodeField = new JTextField();

        JPanel deleteGamePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        deleteGamePanel.add(new JLabel("Código del Juego:"));
        deleteGamePanel.add(gameCodeField);

        int result = JOptionPane.showConfirmDialog(mainFrame, deleteGamePanel, "Eliminar Juego", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int gameCode = Integer.parseInt(gameCodeField.getText());

                if (steam.deleteGame(gameCode)) {
                    JOptionPane.showMessageDialog(mainFrame, "Juego eliminado exitosamente.");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Juego no encontrado.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Error al eliminar el juego.");
            }
        }
    }

    private void generateReport() {
        JTextField clientCodeField = new JTextField();
        JTextField fileNameField = new JTextField();

        JPanel reportPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        reportPanel.add(new JLabel("Código del Cliente:"));
        reportPanel.add(clientCodeField);
        reportPanel.add(new JLabel("Nombre del Archivo de Reporte:"));
        reportPanel.add(fileNameField);

        int result = JOptionPane.showConfirmDialog(mainFrame, reportPanel, "Generar Reporte", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int clientCode = Integer.parseInt(clientCodeField.getText());
            String fileName = fileNameField.getText();

            if (!fileName.isEmpty()) {
                try {
                    steam.reportForClient(clientCode, fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Error al generar el reporte.");
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Nombre de archivo inválido.");
            }
        }
    }

    private void viewCatalog() {
        try {
            steam.printGames();
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
        downloadPanel.add(new JLabel("Código del Juego:"));
        downloadPanel.add(gameCodeField);
        downloadPanel.add(new JLabel("Código del Cliente:"));
        downloadPanel.add(clientCodeField);
        downloadPanel.add(new JLabel("Sistema Operativo (W/M/L):"));
        downloadPanel.add(soField);

        int result = JOptionPane.showConfirmDialog(mainFrame, downloadPanel, "Descargar Juego", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int gameCode = Integer.parseInt(gameCodeField.getText());
            int clientCode = Integer.parseInt(clientCodeField.getText());
            char so = soField.getText().toUpperCase().charAt(0);

            try {
                boolean success = steam.downloadGame(gameCode, clientCode, so);
                if (success) {
                    JOptionPane.showMessageDialog(mainFrame, "Juego descargado exitosamente.");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "No se pudo descargar el juego. Verifique los datos.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Error al descargar el juego.");
            }
        }
    }

    private void viewProfile() {
        try {
            Player player = steam.getPlayerByUsername(usernameField.getText());
            if (player != null) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Código: " + player.getCode() + "\n"
                        + "Username: " + player.getUsername() + "\n"
                        + "Nombre: " + player.getNombre() + "\n"
                        + "Fecha de Nacimiento: " + player.getFormattedNacimiento() + "\n"
                        + "Descargas: " + player.getContadorDownloads() + "\n"
                        + "Tipo de Usuario: " + player.getTipoUsuario());
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Perfil no encontrado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error al mostrar el perfil.");
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
