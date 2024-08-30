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

    public Steam() throws IOException {

        File steamDir = new File(DIRECTORY);
        if (!steamDir.exists()) {
            steamDir.mkdir();
        }

        File downloadsDir = new File(DOWNLOADS_DIRECTORY);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdir();
        }

        codesFile = new RandomAccessFile(CODES_FILENAME, "rw");
        gamesFile = new RandomAccessFile(GAMES_FILENAME, "rw");
        playersFile = new RandomAccessFile(PLAYERS_FILENAME, "rw");

        if (codesFile.length() == 0) {
            codesFile.writeInt(1);
            codesFile.writeInt(1);
            codesFile.writeInt(1);
        }
    }

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

    public void addGame(String titulo, char sistemaOperativo, int edadMinima, double precio, String imagenPath) throws IOException {
        int code = getNextCode("game");
        int contadorDownloads = 0;

        byte[] imagenBytes = imageToBytes(imagenPath);

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

    public void addPlayer(String username, String password, String nombre, Calendar nacimiento, String tipoUsuario) throws IOException {
        int code = getNextCode("user");
        int contadorDownloads = 0;

        byte[] imagenBytes = imageToBytes("img.png");

        long nacimientoTimestamp = nacimiento.getTimeInMillis();

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

    public boolean downloadGame(int gameCode, int clientCode, char sistemaOperativo) throws IOException {

        Game game = getGameByCode(gameCode);
        if (game == null) {
            return false;
        }

        Player player = getPlayerByCode(clientCode);
        if (player == null) {
            return false;
        }

        if (!isCompatible(game.getSistemaOperativo(), sistemaOperativo)) {
            return false;
        }

        if (!isAgeAllowed(player.getNacimiento(), game.getEdadMinima())) {
            return false;
        }

        int downloadCode = getNextCode("download");
        String downloadFilename = DOWNLOADS_DIRECTORY + "/download_" + downloadCode + ".stm";
        File downloadFile = new File(downloadFilename);
        FileWriter fw = new FileWriter(downloadFile);
        BufferedWriter bw = new BufferedWriter(fw);
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String fechaDownload = sdf.format(Calendar.getInstance().getTime());

            bw.write("Fecha de Download: " + fechaDownload + "\n");
            bw.write("Imagen del Juego: [IMAGE DATA]\n");
            bw.write("Download #" + downloadCode + "\n");
            bw.write(player.getNombre() + " ha bajado " + game.getTitulo() + " a un precio de $ " + game.getPrecio() + "\n");
        } finally {
            bw.close();
            fw.close();
        }

        updateGameDownloads(gameCode);
        updatePlayerDownloads(clientCode);

        return true;
    }

    private void updateGameDownloads(int gameCode) throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            if (code == gameCode) {

                gamesFile.readUTF();
                gamesFile.readChar();
                gamesFile.readInt();
                gamesFile.readDouble();
                long pos = gamesFile.getFilePointer();
                int contador = gamesFile.readInt();
                gamesFile.seek(pos);
                gamesFile.writeInt(contador + 1);
                break;
            } else {

                skipGameRecord();
            }
        }
    }

    private void updatePlayerDownloads(int playerCode) throws IOException {
        playersFile.seek(0);
        while (playersFile.getFilePointer() < playersFile.length()) {
            int code = playersFile.readInt();
            if (code == playerCode) {

                playersFile.readUTF();
                playersFile.readUTF();
                playersFile.readUTF();
                playersFile.readLong();
                long pos = playersFile.getFilePointer();
                int contador = playersFile.readInt();
                playersFile.seek(pos);
                playersFile.writeInt(contador + 1);
                break;
            } else {

                skipPlayerRecord();
            }
        }
    }

    public void updatePriceFor(int gameCode, double newPrice) throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            if (code == gameCode) {
                gamesFile.readUTF();
                gamesFile.readChar();
                gamesFile.readInt();
                gamesFile.writeDouble(newPrice);
                break;
            } else {

                skipGameRecord();
            }
        }
    }

    public void reportForClient(int codeClient, String txtFile) throws IOException {
        Player player = getPlayerByCode(codeClient);
        if (player == null) {
            System.out.println("NO SE PUEDE CREAR REPORTE");
            return;
        }

        File report = new File(txtFile);
        FileWriter fw = new FileWriter(report, false);
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

            System.out.println("Código: " + code);
            System.out.println("Título: " + titulo);
            System.out.println("Sistema Operativo: " + so);
            System.out.println("Edad Mínima: " + edadMinima);
            System.out.println("Precio: $" + precio);
            System.out.println("Contador de Downloads: " + contadorDownloads);
            System.out.println("-------------------------------------");
        }
    }

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

    private void skipGameRecord() throws IOException {
        gamesFile.readUTF();
        gamesFile.readChar();
        gamesFile.readInt();
        gamesFile.readDouble();
        gamesFile.readInt();
        int imagenLength = gamesFile.readInt();
        gamesFile.seek(gamesFile.getFilePointer() + imagenLength);
    }

    private void skipPlayerRecord() throws IOException {
        playersFile.readUTF();
        playersFile.readUTF();
        playersFile.readUTF();
        playersFile.readLong();
        playersFile.readInt();
        int imagenLength = playersFile.readInt();
        playersFile.seek(playersFile.getFilePointer() + imagenLength);
        playersFile.readUTF();
    }

    private boolean isCompatible(char gameSO, char clientSO) {

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

    public boolean updateGameDetails(int gameCode, String newTitle, char newSo, int newAge, double newPrice) throws IOException {
        gamesFile.seek(0);
        while (gamesFile.getFilePointer() < gamesFile.length()) {
            int code = gamesFile.readInt();
            if (code == gameCode) {
                gamesFile.writeUTF(newTitle);
                gamesFile.writeChar(newSo);
                gamesFile.writeInt(newAge);
                gamesFile.writeDouble(newPrice);
                return true;
            } else {

                skipGameRecord();
            }
        }
        return false;
    }

    public boolean deleteGame(int gameCode) throws IOException {

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

        File originalFile = new File(GAMES_FILENAME);
        originalFile.delete();
        tempFile.renameTo(originalFile);

        gamesFile = new RandomAccessFile(GAMES_FILENAME, "rw");

        return found;
    }
}
