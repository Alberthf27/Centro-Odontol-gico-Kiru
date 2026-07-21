package pe.edu.upao.kiru.pagos.application.port;

import pe.edu.upao.kiru.pagos.domain.Pago;

public interface PagoRepository {

    Pago save(Pago pago);
}
