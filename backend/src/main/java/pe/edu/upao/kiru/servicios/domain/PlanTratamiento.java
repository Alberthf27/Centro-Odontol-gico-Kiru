package pe.edu.upao.kiru.servicios.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;

public class PlanTratamiento {

    private final String idPlan;
    private final String descripcion;
    private final LocalDate fechaInicio;
    private final EstadoPlanTratamiento estado;
    private final BigDecimal progreso;
    private final Cliente cliente;
    private final Odontologo odontologo;
    private final Tratamiento tratamiento;
    private final List<Sesion> sesionesPendientes;

    public PlanTratamiento(
            String idPlan,
            String descripcion,
            LocalDate fechaInicio,
            EstadoPlanTratamiento estado,
            BigDecimal progreso,
            Cliente cliente,
            Odontologo odontologo,
            Tratamiento tratamiento,
            List<Sesion> sesionesPendientes
    ) {
        this.idPlan = idPlan;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
        this.progreso = progreso;
        this.cliente = cliente;
        this.odontologo = odontologo;
        this.tratamiento = tratamiento;
        this.sesionesPendientes = List.copyOf(sesionesPendientes);
    }

    public String getIdPlan() { return idPlan; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public EstadoPlanTratamiento getEstado() { return estado; }
    public BigDecimal getProgreso() { return progreso; }
    public Cliente getCliente() { return cliente; }
    public Odontologo getOdontologo() { return odontologo; }
    public Tratamiento getTratamiento() { return tratamiento; }
    public List<Sesion> getSesionesPendientes() { return sesionesPendientes; }

    public boolean contieneSesion(String idSesion) {
        return sesionesPendientes.stream().anyMatch(sesion -> sesion.getIdSesion().equals(idSesion));
    }

    public Sesion obtenerSesion(String idSesion) {
        return sesionesPendientes.stream()
                .filter(sesion -> sesion.getIdSesion().equals(idSesion))
                .findFirst()
                .orElse(null);
    }
}
