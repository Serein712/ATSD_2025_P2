package todolist.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

// Data Transfer Object para la clase Usuario
public class UsuarioData {

    private Long id;
    private String email;
    private String nombre;
    private String password;
    private Date fechaNacimiento;
    private Boolean admin;
    private Boolean ban;

    public UsuarioData() {
        this.admin = false;
    }
    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPassword(String password) { this.password = password; }

    public String getPassword() { return password; }

    public Date getFechaNacimiento() {
        // Type: Date
        // Format: YYYY-MM-DD
        // If null -> error
        return fechaNacimiento;
    }

    public String getFechaNacimientoFormateada() {
        // Type: String
        // Format: DD/MM/YYYY
        // If null -> Date not specified

        if (this.fechaNacimiento == null) {
            return "Not specified";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(this.fechaNacimiento);
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    // Sobreescribimos equals y hashCode para que dos usuarios sean iguales
    // si tienen el mismo ID (ignoramos el resto de atributos)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioData)) return false;
        UsuarioData that = (UsuarioData) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
    public Boolean getBan() {
        return ban;
    }

    public void setBan(Boolean ban) {
        this.ban = ban;
    }
}
