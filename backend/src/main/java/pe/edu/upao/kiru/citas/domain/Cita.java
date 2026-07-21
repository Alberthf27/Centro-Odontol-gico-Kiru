package pe.edu.upao.kiru.citas.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;
import pe.edu.upao.kiru.servicios.domain.Sesion;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;

public class Cita {

    /**
     * Numero visible de la cita. El diagrama lo denomina nroCita; se conserva
     * getIdCita() como alias temporal para los contratos de reserva existentes.
     */
    private String nroCita;
    private EstadoCita estado;
    private Tratamiento tratamiento;
    private Sesion sesion;
    private Cliente cliente;
    private Odontologo odontologo;
    private String notaSesion;
    private boolean asistenciaOdontologo;
    private boolean asistenciaPaciente;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private FranjaHoraria franjaHoraria;

    protected Cita() {
    }

    public Cita(
            String nroCita,
            Cliente cliente,
            Odontologo odontologo,
            Tratamiento tratamiento,
            FranjaHoraria franjaHoraria
    ) {
        this(nroCita, cliente, odontologo, tratamiento, null, franjaHoraria);
    }

    public Cita(
            String nroCita,
            Cliente cliente,
            Odontologo odontologo,
            Tratamiento tratamiento,
            Sesion sesion,
            FranjaHoraria franjaHoraria
    ) {
        this.nroCita = Objects.requireNonNull(nroCita);
        this.cliente = Objects.requireNonNull(cliente);
        this.odontologo = Objects.requireNonNull(odontologo);
        this.tratamiento = tratamiento;
        this.sesion = sesion;
        this.franjaHoraria = Objects.requireNonNull(franjaHoraria);
        this.fecha = franjaHoraria.getFecha();
        this.horaInicio = franjaHoraria.getHoraInicio();
        this.horaFin = franjaHoraria.getHoraFin();
        this.estado = EstadoCita.PENDIENTE;
    }

    public static Cita reconstituir(
            String nroCita,
            EstadoCita estado,
            Cliente cliente,
            Odontologo odontologo,
            Tratamiento tratamiento,
            Sesion sesion,
            FranjaHoraria franjaHoraria,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            String notaSesion,
            Boolean asistenciaOdontologo,
            Boolean asistenciaPaciente
    ) {
        Cita cita = new Cita();
        cita.nroCita = nroCita;
        cita.estado = estado;
        cita.cliente = cliente;
        cita.odontologo = odontologo;
        cita.tratamiento = tratamiento;
        cita.sesion = sesion;
        cita.franjaHoraria = franjaHoraria;
        cita.notaSesion = notaSesion;
        cita.asistenciaOdontologo = Boolean.TRUE.equals(asistenciaOdontologo);
        cita.asistenciaPaciente = Boolean.TRUE.equals(asistenciaPaciente);
        cita.fecha = fecha != null ? fecha : Objects.requireNonNull(franjaHoraria).getFecha();
        cita.horaInicio = horaInicio != null ? horaInicio : Objects.requireNonNull(franjaHoraria).getHoraInicio();
        cita.horaFin = horaFin != null ? horaFin : Objects.requireNonNull(franjaHoraria).getHoraFin();
        return cita;
    }

    public String getNroCita() {
        return nroCita;
    }

    /**
     * Alias de compatibilidad para los comandos internos que todavía reciben
     * el identificador como idCita.
     */
    public String getIdCita() {
        return nroCita;
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

    public Sesion getSesion() {
        return sesion;
    }

    public TipoCita getTipoCita() {
        return esTratamiento() ? TipoCita.TRATAMIENTO : TipoCita.CONSULTA;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public boolean esTratamiento() {
        return tratamiento != null;
    }

    public boolean perteneceA(Cliente cliente) {
        return this.cliente == cliente
                || (this.cliente.getIdCliente() != null
                && Objects.equals(this.cliente.getIdCliente(), cliente.getIdCliente()));
    }

    public boolean permiteCancelacionOReprogramacion(LocalDateTime ahora) {
        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        return ChronoUnit.HOURS.between(ahora, inicio) >= 24
                && estado != EstadoCita.CANCELADA
                && estado != EstadoCita.ATENDIDA;
    }

    public void cancelar() {
        estado = EstadoCita.CANCELADA;
    }

    public void iniciarCita() {
        estado = EstadoCita.EN_ATENCION;
    }

    public void finalizarCita() {
        estado = EstadoCita.ATENDIDA;
    }

    public void actualizarAsistencia(boolean asistenciaPaciente, boolean asistenciaOdontologo) {
        this.asistenciaPaciente = asistenciaPaciente;
        this.asistenciaOdontologo = asistenciaOdontologo;
    }

    public void reprogramar(FranjaHoraria nuevaFranja) {
        franjaHoraria = nuevaFranja;
        odontologo = nuevaFranja.getOdontologo();
        fecha = nuevaFranja.getFecha();
        horaInicio = nuevaFranja.getHoraInicio();
        horaFin = nuevaFranja.getHoraFin();
        estado = EstadoCita.REPROGRAMADA;
    }
}
