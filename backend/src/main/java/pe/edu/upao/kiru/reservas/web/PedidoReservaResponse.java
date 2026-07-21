package pe.edu.upao.kiru.reservas.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import pe.edu.upao.kiru.citas.web.CitaResponse;
import pe.edu.upao.kiru.pagos.domain.EstadoPago;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;

public record PedidoReservaResponse(
        String idPedido,
        String nroPedido,
        LocalDateTime fecha,
        BigDecimal montoTotal,
        EstadoPago estadoPago,
        List<CitaResponse> citas
) {
    public static PedidoReservaResponse from(PedidoReserva pedido) {
        return new PedidoReservaResponse(
                pedido.getIdPedido(),
                pedido.getNroPedido(),
                pedido.getFecha(),
                pedido.getMontoTotal(),
                pedido.getEstadoPago(),
                pedido.getCitas().stream().map(CitaResponse::from).toList()
        );
    }
}
