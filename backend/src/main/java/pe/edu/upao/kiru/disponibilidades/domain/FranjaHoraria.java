package pe.edu.upao.kiru.disponibilidades.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;

public class FranjaHoraria {

    private String idFranja;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean disponible;
    private Odontologo odontologo;
    private Cita cita;

    protected FranjaHoraria() {
    }

    public FranjaHoraria(
            String idFranja,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            boolean disponible,
            Odontologo odontologo
    ) {
        this.idFranja = idFranja;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.disponible = disponible;
        this.odontologo = odontologo;
    }

    public String getIdFranja() {
        return idFranja;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public Odontologo getOdontologo() {
        return odontologo;
    }

    public Cita getCita() {
        return cita;
    }

    public boolean estaDisponible() {
        return disponible;
    }

    public void ocupar() {
        disponible = false;
    }

    public void liberar() {
        disponible = true;
        cita = null;
    }

    public void asignarCita(Cita cita) {
        this.cita = cita;
        disponible = false;
    }
}
