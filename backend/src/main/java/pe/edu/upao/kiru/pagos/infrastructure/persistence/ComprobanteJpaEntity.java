package pe.edu.upao.kiru.pagos.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.edu.upao.kiru.pagos.domain.TipoComprobante;

@Entity
@Table(name = "comprobante", schema = "public")
class ComprobanteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Integer idComprobante;

    @Column(name = "id_pago", nullable = false, unique = true)
    private Integer idPago;

    @Column(name = "id_empleado_emisor")
    private Integer idEmpleadoEmisor;

    @Column(name = "nro_comprobante", nullable = false, unique = true)
    private String nroComprobante;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_comprobante", nullable = false, columnDefinition = "tipo_comprobante_enum")
    private TipoComprobante tipoComprobante;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    protected ComprobanteJpaEntity() {
    }

    ComprobanteJpaEntity(
            Integer idPago,
            String nroComprobante,
            TipoComprobante tipoComprobante,
            BigDecimal montoTotal,
            LocalDateTime fechaEmision
    ) {
        this.idPago = idPago;
        this.nroComprobante = nroComprobante;
        this.tipoComprobante = tipoComprobante;
        this.montoTotal = montoTotal;
        this.fechaEmision = fechaEmision;
    }

    Integer getIdComprobante() { return idComprobante; }
}
