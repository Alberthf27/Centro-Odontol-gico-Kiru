package pe.edu.upao.kiru.pagos.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.pagos.application.port.ComprobanteRepository;
import pe.edu.upao.kiru.pagos.domain.Comprobante;

@Repository
class ComprobantePersistenceAdapter implements ComprobanteRepository {

    private final ComprobanteJpaSpringRepository repository;

    ComprobantePersistenceAdapter(ComprobanteJpaSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public Comprobante save(Comprobante comprobante) {
        ComprobanteJpaEntity saved = repository.save(new ComprobanteJpaEntity(
                Integer.valueOf(comprobante.getPago().getIdPago()),
                comprobante.getNroComprobante(),
                comprobante.getTipoComprobante(),
                comprobante.getMontoTotal(),
                comprobante.getFechaEmision()
        ));
        return Comprobante.reconstituir(
                saved.getIdComprobante().toString(),
                comprobante.getNroComprobante(),
                comprobante.getTipoComprobante(),
                comprobante.getMontoTotal(),
                comprobante.getFechaEmision(),
                comprobante.getPago()
        );
    }
}
