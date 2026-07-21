package pe.edu.upao.kiru.pagos.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import java.util.Objects;

public class Pago {

    private String idPago;
    private String metodoPago;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private PedidoReserva pedidoReserva;

    protected Pago() {
    }

    public Pago(
            PedidoReserva pedidoReserva,
            String metodoPago,
            BigDecimal monto,
            LocalDateTime fecha
    ) {
        this.pedidoReserva = Objects.requireNonNull(pedidoReserva);
        this.metodoPago = Objects.requireNonNull(metodoPago);
        this.monto = Objects.requireNonNull(monto);
        this.fecha = Objects.requireNonNull(fecha);
    }

    public static Pago reconstituir(
            String idPago,
            PedidoReserva pedidoReserva,
            String metodoPago,
            BigDecimal monto,
            LocalDateTime fecha
    ) {
        Pago pago = new Pago();
        pago.idPago = idPago;
        pago.pedidoReserva = pedidoReserva;
        pago.metodoPago = metodoPago;
        pago.monto = monto;
        pago.fecha = fecha;
        return pago;
    }

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

    public PedidoReserva getPedidoReserva() {
        return pedidoReserva;
    }
}
