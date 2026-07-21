package pe.edu.upao.kiru.servicios.domain;

public class Sesion {

    private final String idSesion;
    private final String nombreTipo;
    private final int orden;
    private final String descripcion;

    public Sesion(String idSesion, String nombreTipo, int orden, String descripcion) {
        this.idSesion = idSesion;
        this.nombreTipo = nombreTipo;
        this.orden = orden;
        this.descripcion = descripcion;
    }

    public String getIdSesion() {
        return idSesion;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public int getOrden() {
        return orden;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
