package pe.edu.upao.kiru.citas.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.edu.upao.kiru.citas.domain.EstadoCita;

@Entity
@Table(name = "cita", schema = "public")
public class CitaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer idCita;

    @Column(name = "id_pedido", nullable = false)
    private Integer idPedido;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "id_odontologo", nullable = false)
    private Integer idOdontologo;

    @Column(name = "id_tratamiento")
    private Integer idTratamiento;

    @Column(name = "nro_cita", nullable = false, unique = true)
    private String nroCita;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_cita_enum")
    private EstadoCita estado;

    @Column(name = "nota_sesion")
    private String notaSesion;

    @Column(name = "asistencia_cliente")
    private Boolean asistenciaCliente;

    @Column(name = "asistencia_odontologo")
    private Boolean asistenciaOdontologo;

    protected CitaJpaEntity() {
    }

    public CitaJpaEntity(
            Integer idPedido,
            Integer idCliente,
            Integer idOdontologo,
            Integer idTratamiento,
            String nroCita,
            LocalDate fechaProgramada,
            LocalTime horaInicio,
            LocalTime horaFin,
            EstadoCita estado
    ) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.idOdontologo = idOdontologo;
        this.idTratamiento = idTratamiento;
        this.nroCita = nroCita;
        this.fechaProgramada = fechaProgramada;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
    }

    public Integer getIdCita() { return idCita; }
    public Integer getIdPedido() { return idPedido; }
    public Integer getIdCliente() { return idCliente; }
    public Integer getIdOdontologo() { return idOdontologo; }
    public Integer getIdTratamiento() { return idTratamiento; }
    public String getNroCita() { return nroCita; }
    public LocalDate getFechaProgramada() { return fechaProgramada; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public EstadoCita getEstado() { return estado; }
    public String getNotaSesion() { return notaSesion; }
    public Boolean getAsistenciaCliente() { return asistenciaCliente; }
    public Boolean getAsistenciaOdontologo() { return asistenciaOdontologo; }
    public void actualizar(
            EstadoCita estado,
            Integer idOdontologo,
            Integer idTratamiento,
            LocalDate fechaProgramada,
            LocalTime horaInicio,
            LocalTime horaFin,
            String notaSesion,
            boolean asistenciaCliente,
            boolean asistenciaOdontologo
    ) {
        this.estado = estado;
        this.idOdontologo = idOdontologo;
        this.idTratamiento = idTratamiento;
        this.fechaProgramada = fechaProgramada;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.notaSesion = notaSesion;
        this.asistenciaCliente = asistenciaCliente;
        this.asistenciaOdontologo = asistenciaOdontologo;
    }
}
