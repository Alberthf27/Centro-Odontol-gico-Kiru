package pe.edu.upao.kiru.pagos.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;

public class Pago {

    private String idPago;
    private String metodoPago;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private EstadoPago estado;
    private PedidoReserva pedidoReserva;

    public String getIdPago() {
        return idPago;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public PedidoReserva getPedidoReserva() {
        return pedidoReserva;
    }
}
