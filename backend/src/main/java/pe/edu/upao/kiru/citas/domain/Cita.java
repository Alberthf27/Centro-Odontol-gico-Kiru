package pe.edu.upao.kiru.citas.domain;

import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;

public class Cita {

    private String idCita;
    private EstadoCita estado;
    private Cliente cliente;
    private Odontologo odontologo;
    private Tratamiento tratamiento;
    private FranjaHoraria franjaHoraria;
    private String notaSesion;
    private boolean asistenciaOdontologo;
    private boolean asistenciaPaciente;

    public String getIdCita() {
        return idCita;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Odontologo getOdontologo() {
        return odontologo;
    }

    public Tratamiento getTratamiento() {
        return tratamiento;
    }

    public FranjaHoraria getFranjaHoraria() {
        return franjaHoraria;
    }

    public String getNotaSesion() {
        return notaSesion;
    }

    public boolean isAsistenciaOdontologo() {
        return asistenciaOdontologo;
    }

    public boolean isAsistenciaPaciente() {
        return asistenciaPaciente;
    }

    public boolean esTratamiento() {
        return tratamiento != null;
    }

    public void cancelar() {
        estado = EstadoCita.CANCELADA;
    }

    public void reprogramar(FranjaHoraria nuevaFranja) {
        franjaHoraria = nuevaFranja;
        odontologo = nuevaFranja.getOdontologo();
        estado = EstadoCita.REPROGRAMADA;
    }
}
