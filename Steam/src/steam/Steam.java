package steam;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Steam {

    private RandomAccessFile codesFile;
    private RandomAccessFile gamesFile;
    private RandomAccessFile playersFile;
    private final String DIRECTORY = "steam";
    private final String DOWNLOADS_DIRECTORY = "steam/downloads";
    private final String CODES_FILENAME = "steam/codes.stm";
    private final String GAMES_FILENAME = "steam/games.stm";
    private final String PLAYERS_FILENAME = "steam/player.stm";

    // Constructor
    public Steam() throws IOException {
        // Asegurar que el directorio 'steam' y 'downloads' existan
        File steamDir = new File(DIRECTORY);
        if (!steamDir.exists()) {
            steamDir.mkdir();
        }

        File downloadsDir = new File(DOWNLOADS_DIRECTORY);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdir();
        }

        // Inicializar los archivos RandomAccessFile
        // "rw" permite lectura y escritura
        codesFile = new RandomAccessFile(CODES_FILENAME, "rw");
        gamesFile = new RandomAccessFile(GAMES_FILENAME, "rw");
        playersFile = new RandomAccessFile(PLAYERS_FILENAME, "rw");

        // Inicializar el archivo de códigos si es nuevo
        if (codesFile.length() == 0) {
            codesFile.writeInt(1); // Código para nuevos juegos
            codesFile.writeInt(1); // Código para nuevos clientes
            codesFile.writeInt(1); // Código para nuevos downloads
        }
    }

    // Método para obtener el próximo código disponible
    public synchronized int getNextCode(String type) throws IOException {
        codesFile.seek(0);
        int gameCode = codesFile.readInt();
        int userCode = codesFile.readInt();
        int downloadCode = codesFile.readInt();

        int nextCode = 0;
        codesFile.seek(0);
        switch (type.toLowerCase()) {
            case "game":
                nextCode = gameCode;
                codesFile.writeInt(gameCode + 1);
                break;
            case "user":
                nextCode = userCode;
                codesFile.writeInt(userCode + 1);
                break;
            case "download":
                nextCode = downloadCode;
                codesFile.writeInt(downloadCode + 1);
                break;
            default:
                throw new IllegalArgumentException("Tipo de código inválido");
        }
        return nextCode;
    }

    // Método para agregar un nuevo juego
    public void addGame(String titulo, char sistemaOperativo, int edadMinima, double precio, String imagenPath) throws IOException {
        int code = getNextCode("game");
        int contadorDownloads = 0;

        // Convertir la imagen seleccionada a bytes
        byte[] imagenBytes = imageToBytes(imagenPath);

        // Moverse al final del archivo para agregar el nuevo registro
        gamesFile.seek(gamesFile.length());
        gamesFile.writeInt(code);
        gamesFile.writeUTF(titulo);
        gamesFile.writeChar(sistemaOperativo);
        gamesFile.writeInt(edadMinima);
        gamesFile.writeDouble(precio);
        gamesFile.writeInt(contadorDownloads);
        gamesFile.writeInt(imagenBytes.length);
        gamesFile.write(imagenBytes);
    }

//    // Método auxiliar para convertir una imagen a un arreglo de bytes
//    private byte[] imageToBytes(String imagePath) throws IOException {
//        File file = new File(imagePath);
//        byte[] bytesArray = new byte[(int) file.length()];
//        FileInputStream fis = new FileInputStream(file);
//        try {
//            fis.read(bytesArray);
//        } finally {
//            fis.close();
//        }
//        return bytesArray;
//    }
    // Método para agregar un nuevo jugador
    public void addPlayer(String username, String password, String nombre, Calendar nacimiento, String tipoUsuario) throws IOException {
        int code = getNextCode("user");
        int contadorDownloads = 0;

        // Cargar la imagen desde el classpath
        byte[] imagenBytes = imageToBytes("img.png");

        // Convertir Calendar a long (timestamp)
        long nacimientoTimestamp = nacimiento.getTimeInMillis();

        // Moverse al final del archivo para agregar el nuevo registro
        playersFile.seek(playersFile.length());
        playersFile.writeInt(code);
        playersFile.writeUTF(username);
        playersFile.writeUTF(password);
        playersFile.writeUTF(nombre);
        playersFile.writeLong(nacimientoTimestamp);
        playersFile.writeInt(contadorDownloads);
        playersFile.writeInt(imagenBytes.length);
        playersFile.write(imagenBytes);
        playersFile.writeUTF(tipoUsuario);
    }

    // Método auxiliar para convertir una imagen a un arreglo de bytes
    private byte[] imageToBytes(String imagePath) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(imagePath);
        if (is == null) {
            throw new FileNotFoundException("No se encontró el archivo: " + imagePath);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    // Método para cerrar los archivos al finalizar
    public void close() {
        try {
            if (codesFile != null) {
                codesFile.close();
            }
            if (gamesFile != null) {
                gamesFile.close();
            }
            if (playersFile != null) {
                playersFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para descargar un juego
    public boolean downloadGame(int gameCode, int clientCode, char sistemaOperativo) throws IOException {
        // Verificar si el juego existe y obtener sus datos
        Game game = getGameByCode(gameCode);
        if (game == null) {
            return false;
        }

        // Verificar si el cliente existe y obtener sus datos
        Player player = getPlayerByCode(clientCode);
        if (player == null) {
            return false;
        }

        // Verificar si el sistema operativo es compatible
        if (!isCompatible(game.getSistemaOperativo(), sistemaOperativo)) {
            return false;
        }

        // Verificar si el cliente cumple con la edad mínima
        if (!isAgeAllowed(player.getNacimiento(), game.getEdadMinima())) {
            return false;
        }

        // Crear el archivo de descarga
        int downloadCode = getNextCode("download");
        String downloadFilename = DOWNLOADS_DIRECTORY + "/download_" + downloadCode + ".stm";
        File downloadFile = new File(downloadFilename);
        FileWriter fw = new FileWriter(downloadFile);
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            // Formatear la fecha
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String fechaDownload = sdf.format(Calendar.getInstance().getTime());

            // Escribir en el archivo de descarga
            bw.write("Fecha de Download: " + fechaDownload + "\n");
            bw.write("Imagen del Juego: [IMAGE DATA]\n"); // Placeholder
            bw.write("Download #" + downloadCode + "\n");
            bw.write(player.getNombre() + " ha bajado " + game.getTitulo() + " a un precio de $ " + game.getPrecio() + "\n");
        } finally {
            bw.close();
            fw.close();
        }

        // Actualizar los contadores de downloads
        updateGameDownloads(gameCode);
        updatePlayerDownloads(clientCode);

        return true;
    }

    // Método para actualizar el contador de downloads de un juego
    private void updateGameDownloads(int gameCode) throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            if (code == gameCode) {
                // Saltar a la posición del contador de downloads
                gamesFile.readUTF(); // Titulo
                gamesFile.readChar(); // SO
                gamesFile.readInt(); // Edad mínima
                gamesFile.readDouble(); // Precio
                long pos = gamesFile.getFilePointer();
                int contador = gamesFile.readInt();
                gamesFile.seek(pos);
                gamesFile.writeInt(contador + 1);
                break;
            } else {
                // Saltar al siguiente registro
                skipGameRecord();
            }
        }
    }

    // Método para actualizar el contador de downloads de un jugador
    private void updatePlayerDownloads(int playerCode) throws IOException {
        playersFile.seek(0);
        while (playersFile.getFilePointer() < playersFile.length()) {
            int code = playersFile.readInt();
            if (code == playerCode) {
                // Saltar a la posición del contador de downloads
                playersFile.readUTF(); // Username
                playersFile.readUTF(); // Password
                playersFile.readUTF(); // Nombre
                playersFile.readLong(); // Nacimiento
                long pos = playersFile.getFilePointer();
                int contador = playersFile.readInt();
                playersFile.seek(pos);
                playersFile.writeInt(contador + 1);
                break;
            } else {
                // Saltar al siguiente registro
                skipPlayerRecord();
            }
        }
    }

    // Método para actualizar el precio de un juego
    public void updatePriceFor(int gameCode, double newPrice) throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            if (code == gameCode) {
                gamesFile.readUTF(); // Titulo
                gamesFile.readChar(); // SO
                gamesFile.readInt(); // Edad mínima
                gamesFile.writeDouble(newPrice);
                break;
            } else {
                // Saltar al siguiente registro
                skipGameRecord();
            }
        }
    }

    // Método para generar un reporte para un cliente
    public void reportForClient(int codeClient, String txtFile) throws IOException {
        Player player = getPlayerByCode(codeClient);
        if (player == null) {
            System.out.println("NO SE PUEDE CREAR REPORTE");
            return;
        }

        File report = new File(txtFile);
        FileWriter fw = new FileWriter(report, false); // Overwrite
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            bw.write("Código: " + player.getCode() + "\n");
            bw.write("Username: " + player.getUsername() + "\n");
            bw.write("Nombre: " + player.getNombre() + "\n");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String fechaNacimiento = sdf.format(player.getNacimiento());
            bw.write("Fecha de Nacimiento: " + fechaNacimiento + "\n");
            bw.write("Contador de Downloads: " + player.getContadorDownloads() + "\n");
            bw.write("Tipo de Usuario: " + player.getTipoUsuario() + "\n");
            System.out.println("REPORTE CREADO");
        } catch (IOException e) {
            System.out.println("NO SE PUEDE CREAR REPORTE");
        } finally {
            bw.close();
            fw.close();
        }
    }

    // Método para imprimir todos los juegos disponibles
    public void printGames() throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            String titulo = gamesFile.readUTF();
            char so = gamesFile.readChar();
            int edadMinima = gamesFile.readInt();
            double precio = gamesFile.readDouble();
            int contadorDownloads = gamesFile.readInt();
            int imagenLength = gamesFile.readInt();
            byte[] imagen = new byte[imagenLength];
            gamesFile.readFully(imagen);
            // Mostrar los datos
            System.out.println("Código: " + code);
            System.out.println("Título: " + titulo);
            System.out.println("Sistema Operativo: " + so);
            System.out.println("Edad Mínima: " + edadMinima);
            System.out.println("Precio: $" + precio);
            System.out.println("Contador de Downloads: " + contadorDownloads);
            System.out.println("-------------------------------------");
        }
    }

    // Métodos auxiliares para obtener un juego o jugador por código
    private Game getGameByCode(int gameCode) throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            String titulo = gamesFile.readUTF();
            char so = gamesFile.readChar();
            int edadMinima = gamesFile.readInt();
            double precio = gamesFile.readDouble();
            int contadorDownloads = gamesFile.readInt();
            int imagenLength = gamesFile.readInt();
            byte[] imagen = new byte[imagenLength];
            gamesFile.readFully(imagen);
            if (code == gameCode) {
                return new Game(code, titulo, so, edadMinima, precio, contadorDownloads, imagen);
            }
        }
        return null;
    }

    private steam.Player getPlayerByCode(int playerCode) throws IOException {
        playersFile.seek(0);
        while (playersFile.getFilePointer() < playersFile.length()) {
            int code = playersFile.readInt();
            String username = playersFile.readUTF();
            String password = playersFile.readUTF();
            String nombre = playersFile.readUTF();
            long nacimientoTimestamp = playersFile.readLong();
            int contadorDownloads = playersFile.readInt();
            int imagenLength = playersFile.readInt();
            byte[] imagen = new byte[imagenLength];
            playersFile.readFully(imagen);
            String tipoUsuario = playersFile.readUTF();
            if (code == playerCode) {
                Calendar nacimiento = Calendar.getInstance();
                nacimiento.setTimeInMillis(nacimientoTimestamp);
                return new Player(code, username, password, nombre, nacimiento, contadorDownloads, imagen, tipoUsuario);
            }
        }
        return null;
    }

    // Métodos para saltar registros en archivos
    private void skipGameRecord() throws IOException {
        gamesFile.readUTF(); // Titulo
        gamesFile.readChar(); // SO
        gamesFile.readInt(); // Edad mínima
        gamesFile.readDouble(); // Precio
        gamesFile.readInt(); // Contador downloads
        int imagenLength = gamesFile.readInt();
        gamesFile.seek(gamesFile.getFilePointer() + imagenLength);
    }

    private void skipPlayerRecord() throws IOException {
        playersFile.readUTF(); // Username
        playersFile.readUTF(); // Password
        playersFile.readUTF(); // Nombre
        playersFile.readLong(); // Nacimiento
        playersFile.readInt(); // Contador downloads
        int imagenLength = playersFile.readInt();
        playersFile.seek(playersFile.getFilePointer() + imagenLength);
        playersFile.readUTF(); // Tipo de usuario
    }

    // Métodos de verificación
    private boolean isCompatible(char gameSO, char clientSO) {
        // Asumiendo que 'W' = Windows, 'M' = Mac, 'L' = Linux
        return gameSO == clientSO;
    }

    private boolean isAgeAllowed(Calendar nacimiento, int edadMinima) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age >= edadMinima;
    }

    // Clases internas para representar Game y Player
    private class Game {

        private int code;
        private String titulo;
        private char sistemaOperativo;
        private int edadMinima;
        private double precio;
        private int contadorDownloads;
        private byte[] imagen;

        public Game(int code, String titulo, char sistemaOperativo, int edadMinima, double precio, int contadorDownloads, byte[] imagen) {
            this.code = code;
            this.titulo = titulo;
            this.sistemaOperativo = sistemaOperativo;
            this.edadMinima = edadMinima;
            this.precio = precio;
            this.contadorDownloads = contadorDownloads;
            this.imagen = imagen;
        }

        public int getCode() {
            return code;
        }

        public String getTitulo() {
            return titulo;
        }

        public char getSistemaOperativo() {
            return sistemaOperativo;
        }

        public int getEdadMinima() {
            return edadMinima;
        }

        public double getPrecio() {
            return precio;
        }
    }

    // Método para obtener un jugador por su nombre de usuario
    public steam.Player getPlayerByUsername(String username) throws IOException {
        playersFile.seek(0);
        while (playersFile.getFilePointer() < playersFile.length()) {
            int code = playersFile.readInt();
            String user = playersFile.readUTF();
            String password = playersFile.readUTF();
            String nombre = playersFile.readUTF();
            long nacimientoTimestamp = playersFile.readLong();
            int contadorDownloads = playersFile.readInt();
            int imagenLength = playersFile.readInt();
            byte[] imagen = new byte[imagenLength];
            playersFile.readFully(imagen);
            String tipoUsuario = playersFile.readUTF();
            if (user.equals(username)) {
                Calendar nacimiento = Calendar.getInstance();
                nacimiento.setTimeInMillis(nacimientoTimestamp);
                return new Player(code, user, password, nombre, nacimiento, contadorDownloads, imagen, tipoUsuario);
            }
        }
        return null;
    }

    // Método para actualizar detalles de un juego existente
    public boolean updateGameDetails(int gameCode, String newTitle, char newSo, int newAge, double newPrice) throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            if (code == gameCode) {
                gamesFile.writeUTF(newTitle); // Actualizar el título
                gamesFile.writeChar(newSo); // Actualizar el sistema operativo
                gamesFile.writeInt(newAge); // Actualizar la edad mínima
                gamesFile.writeDouble(newPrice); // Actualizar el precio
                return true;
            } else {
                // Saltar al siguiente registro
                skipGameRecord();
            }
        }
        return false; // Si no se encuentra el juego con el código proporcionado
    }

    // Método para eliminar un juego por su código
    public boolean deleteGame(int gameCode) throws IOException {
        // Crear un archivo temporal para almacenar los registros restantes
        File tempFile = new File("steam/temp_games.stm");
        RandomAccessFile tempGamesFile = new RandomAccessFile(tempFile, "rw");

        gamesFile.seek(0);
        boolean found = false;
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            String titulo = gamesFile.readUTF();
            char so = gamesFile.readChar();
            int edadMinima = gamesFile.readInt();
            double precio = gamesFile.readDouble();
            int contadorDownloads = gamesFile.readInt();
            int imagenLength = gamesFile.readInt();
            byte[] imagen = new byte[imagenLength];
            gamesFile.readFully(imagen);

            if (code != gameCode) {
                // Copiar los registros que no coinciden con el código al archivo temporal
                tempGamesFile.writeInt(code);
                tempGamesFile.writeUTF(titulo);
                tempGamesFile.writeChar(so);
                tempGamesFile.writeInt(edadMinima);
                tempGamesFile.writeDouble(precio);
                tempGamesFile.writeInt(contadorDownloads);
                tempGamesFile.writeInt(imagenLength);
                tempGamesFile.write(imagen);
            } else {
                found = true;
            }
        }

        gamesFile.close();
        tempGamesFile.close();

        // Reemplazar el archivo original por el temporal
        File originalFile = new File(GAMES_FILENAME);
        originalFile.delete();
        tempFile.renameTo(originalFile);

        // Reabrir el archivo original
        gamesFile = new RandomAccessFile(GAMES_FILENAME, "rw");

        return found; // Retorna true si el juego fue encontrado y eliminado
    }
}
