package pe.edu.upao.kiru.disponibilidades.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;

public class FranjaHoraria {

    private String idFranja;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoFranjaHoraria estado;
    private Odontologo odontologo;

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

    public EstadoFranjaHoraria getEstado() {
        return estado;
    }

    public Odontologo getOdontologo() {
        return odontologo;
    }

    public boolean estaDisponible() {
        return estado == EstadoFranjaHoraria.DISPONIBLE;
    }

    public void ocupar() {
        estado = EstadoFranjaHoraria.OCUPADA;
    }

    public void liberar() {
        estado = EstadoFranjaHoraria.DISPONIBLE;
    }
}
