package pe.edu.upao.kiru.clientes.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Cliente {

    private String idCliente;
    private String dni;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String celular;
    private String domicilio;
    private String estado;

    protected Cliente() {
    }

    public Cliente(
            String dni,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            String celular,
            String domicilio
    ) {
        this.dni = Objects.requireNonNull(dni);
        this.nombres = Objects.requireNonNull(nombres);
        this.apellidos = Objects.requireNonNull(apellidos);
        this.fechaNacimiento = fechaNacimiento;
        this.celular = celular;
        this.domicilio = domicilio;
        this.estado = "ACTIVO";
    }

    public static Cliente reconstituir(
            String idCliente,
            String dni,
            String nombres,
            String apellidos,
            LocalDate fechaNacimiento,
            String celular,
            String domicilio,
            boolean activo
    ) {
        Cliente cliente = new Cliente();
        cliente.idCliente = idCliente;
        cliente.dni = dni;
        cliente.nombres = nombres;
        cliente.apellidos = apellidos;
        cliente.fechaNacimiento = fechaNacimiento;
        cliente.celular = celular;
        cliente.domicilio = domicilio;
        cliente.estado = activo ? "ACTIVO" : "INACTIVO";
        return cliente;
    }

    public String getIdCliente() {
        return idCliente;
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

    public String getEstado() {
        return estado;
    }
}
