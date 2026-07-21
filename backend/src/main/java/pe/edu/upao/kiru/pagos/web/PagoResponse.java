package pe.edu.upao.kiru.pagos.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.upao.kiru.pagos.application.dto.PagoRegistrado;
import pe.edu.upao.kiru.pagos.domain.Pago;
import pe.edu.upao.kiru.pagos.domain.TipoComprobante;

public record PagoResponse(
        String idPago,
        String idPedido,
        String metodoPago,
        BigDecimal monto,
        LocalDateTime fecha,
        String nroComprobante,
        TipoComprobante tipoComprobante
) {
    public static PagoResponse from(PagoRegistrado pagoRegistrado) {
        Pago pago = pagoRegistrado.pago();
        return new PagoResponse(
                pago.getIdPago(),
                pago.getPedidoReserva().getIdPedido(),
                pago.getMetodoPago(),
                pago.getMonto(),
                pago.getFecha(),
                pagoRegistrado.comprobante().getNroComprobante(),
                pagoRegistrado.comprobante().getTipoComprobante()
        );
    }
}
