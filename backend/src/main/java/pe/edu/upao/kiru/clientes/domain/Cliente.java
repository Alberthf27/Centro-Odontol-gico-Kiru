package pe.edu.upao.kiru.clientes.domain;

import java.time.LocalDate;

public class Cliente {

    private String idCliente;
    private String authUserId;
    private String dni;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String celular;
    private String domicilio;
    private boolean activo;

    public String getIdCliente() {
        return idCliente;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public String getDni() {
        return dni;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getCelular() {
        return celular;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public boolean isActivo() {
        return activo;
    }
}
