package pe.edu.upao.kiru.reservas.application.port;

import java.util.Optional;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;

public interface PedidoReservaRepository {

    PedidoReserva save(PedidoReserva pedidoReserva);

    Optional<PedidoReserva> findById(String idPedido);
}
