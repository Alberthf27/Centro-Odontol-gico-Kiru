package pe.edu.upao.kiru.servicios.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.edu.upao.kiru.servicios.domain.EstadoPlanTratamiento;

@Entity
@Table(name = "plan_tratamiento", schema = "public")
class PlanTratamientoJpaEntity {

    @Id
    @Column(name = "id_plan")
    private Integer idPlan;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "id_odontologo", nullable = false)
    private Integer idOdontologo;

    @Column(name = "id_tratamiento", nullable = false)
    private Integer idTratamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tratamiento", insertable = false, updatable = false)
    private TratamientoJpaEntity tratamiento;

    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_plan_tratamiento_enum")
    private EstadoPlanTratamiento estado;

    @Column(nullable = false)
    private BigDecimal progreso;

    protected PlanTratamientoJpaEntity() {
    }

    Integer getIdPlan() { return idPlan; }
    Integer getIdCliente() { return idCliente; }
    Integer getIdOdontologo() { return idOdontologo; }
    TratamientoJpaEntity getTratamiento() { return tratamiento; }
    String getDescripcion() { return descripcion; }
    LocalDate getFechaInicio() { return fechaInicio; }
    EstadoPlanTratamiento getEstado() { return estado; }
    BigDecimal getProgreso() { return progreso; }
}
