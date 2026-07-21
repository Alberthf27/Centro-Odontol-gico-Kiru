package pe.edu.upao.kiru.disponibilidades.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "franja_horaria", schema = "public")
class FranjaHorariaJpaEntity {

    @Id
    @Column(name = "id_franja")
    private Integer idFranja;

    @Column(name = "id_odontologo", nullable = false)
    private Integer idOdontologo;

    @Column(name = "id_cita")
    private Integer idCita;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private boolean disponible;

    protected FranjaHorariaJpaEntity() {
    }

    Integer getIdFranja() { return idFranja; }
    Integer getIdOdontologo() { return idOdontologo; }
    Integer getIdCita() { return idCita; }
    LocalDate getFecha() { return fecha; }
    LocalTime getHoraInicio() { return horaInicio; }
    LocalTime getHoraFin() { return horaFin; }
    boolean isDisponible() { return disponible; }
    void asignarCita(Integer idCita) { this.idCita = idCita; this.disponible = false; }
}
