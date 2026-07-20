package pe.edu.upao.kiru.servicios.domain;

import java.math.BigDecimal;

public class Tratamiento {

    private String idTratamiento;
    private String nombre;
    private String modalidad;
    private BigDecimal precio;
    private boolean activo;

    public String getIdTratamiento() {
        return idTratamiento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getModalidad() {
        return modalidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isActivo() {
        return activo;
    }
}
