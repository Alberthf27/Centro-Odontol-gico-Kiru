package pe.edu.upao.kiru.empleados.domain;

public class Empleado {

    private String idEmpleado;
    private String nombres;
    private String apellidos;
    private String dni;
    private String celular;
    private String cargo;
    private boolean activo;

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getDni() {
        return dni;
    }

    public String getCelular() {
        return celular;
    }

    public String getCargo() {
        return cargo;
    }

    public boolean isActivo() {
        return activo;
    }
}
