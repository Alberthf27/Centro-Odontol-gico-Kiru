package pe.edu.upao.kiru.reservas.infrastructure.persistence;

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
import pe.edu.upao.kiru.pagos.domain.EstadoPago;

@Entity
@Table(name = "pedido_reserva", schema = "public")
class PedidoReservaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "nro_pedido", nullable = false, unique = true)
    private String nroPedido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado_pago", nullable = false, columnDefinition = "pedido_reserva_estado")
    private EstadoPago estadoPago;

    protected PedidoReservaJpaEntity() {
    }

    PedidoReservaJpaEntity(Integer idCliente, String nroPedido, LocalDateTime fechaCreacion, BigDecimal montoTotal, EstadoPago estadoPago) {
        this.idCliente = idCliente;
        this.nroPedido = nroPedido;
        this.fechaCreacion = fechaCreacion;
        this.montoTotal = montoTotal;
        this.estadoPago = estadoPago;
    }

    Integer getIdPedido() { return idPedido; }
    Integer getIdCliente() { return idCliente; }
    String getNroPedido() { return nroPedido; }
    LocalDateTime getFechaCreacion() { return fechaCreacion; }
    BigDecimal getMontoTotal() { return montoTotal; }
    EstadoPago getEstadoPago() { return estadoPago; }
    void actualizar(BigDecimal montoTotal, EstadoPago estadoPago) { this.montoTotal = montoTotal; this.estadoPago = estadoPago; }
}
