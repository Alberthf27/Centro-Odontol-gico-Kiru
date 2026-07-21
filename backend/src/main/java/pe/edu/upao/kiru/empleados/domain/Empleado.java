package pe.edu.upao.kiru.empleados.domain;

public class Empleado {

    private String idEmpleado;
    private String nombre;
    private String apellido;
    private String dni;
    private String celular;
    private String cargo;
    private String estado;
    private String direccion;

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
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

    public String getEstado() {
        return estado;
    }

    public String getDireccion() {
        return direccion;
    }
}
