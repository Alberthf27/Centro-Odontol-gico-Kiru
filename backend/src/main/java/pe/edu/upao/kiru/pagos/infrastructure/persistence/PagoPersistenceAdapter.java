package pe.edu.upao.kiru.pagos.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.pagos.application.port.PagoRepository;
import pe.edu.upao.kiru.pagos.domain.Pago;

@Repository
class PagoPersistenceAdapter implements PagoRepository {

    private final PagoJpaSpringRepository repository;

    PagoPersistenceAdapter(PagoJpaSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pago save(Pago pago) {
        PagoJpaEntity saved = repository.save(new PagoJpaEntity(
                Integer.valueOf(pago.getPedidoReserva().getIdPedido()),
                pago.getMetodoPago(),
                pago.getMonto(),
                pago.getFecha()
        ));
        return Pago.reconstituir(
                saved.getIdPago().toString(),
                pago.getPedidoReserva(),
                pago.getMetodoPago(),
                pago.getMonto(),
                pago.getFecha()
        );
    }
}
