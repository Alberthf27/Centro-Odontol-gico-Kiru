package pe.edu.upao.kiru.servicios.domain;

import java.math.BigDecimal;

public class Tratamiento {

    private String idTratamiento;
    private String nombre;
    private boolean multiplesSesiones;
    private BigDecimal precio;
    private String estado;

    protected Tratamiento() {
    }

    public Tratamiento(
            String idTratamiento,
            String nombre,
            boolean multiplesSesiones,
            BigDecimal precio,
            String estado
    ) {
        this.idTratamiento = idTratamiento;
        this.nombre = nombre;
        this.multiplesSesiones = multiplesSesiones;
        this.precio = precio;
        this.estado = estado;
    }

    public String getIdTratamiento() {
        return idTratamiento;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isMultiplesSesiones() {
        return multiplesSesiones;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public String getEstado() {
        return estado;
    }

    public boolean estaActivo() {
        return "ACTIVO".equals(estado);
    }
}
