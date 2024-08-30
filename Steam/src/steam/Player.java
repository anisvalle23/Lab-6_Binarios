package steam;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Player {

    private int code;
    private String username;
    private String password;
    private String nombre;
    private Calendar nacimiento;
    private int contadorDownloads;
    private byte[] imagen;
    private String tipoUsuario;

    public Player(int code, String username, String password, String nombre, Calendar nacimiento, int contadorDownloads, byte[] imagen, String tipoUsuario) {
        this.code = code;
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.nacimiento = nacimiento;
        this.contadorDownloads = contadorDownloads;
        this.imagen = imagen;
        this.tipoUsuario = tipoUsuario;
    }

    public int getCode() {
        return code;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNombre() {
        return nombre;
    }

    public Calendar getNacimiento() {
        return nacimiento;
    }

    public int getContadorDownloads() {
        return contadorDownloads;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setNacimiento(Calendar nacimiento) {
        this.nacimiento = nacimiento;
    }

    public void incrementarDescargas() {
        this.contadorDownloads++;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getFormattedNacimiento() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(this.nacimiento.getTime());
    }

    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
}
