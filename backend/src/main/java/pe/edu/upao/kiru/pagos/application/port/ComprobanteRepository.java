package pe.edu.upao.kiru.pagos.application.port;

import pe.edu.upao.kiru.pagos.domain.Comprobante;

public interface ComprobanteRepository {

    Comprobante save(Comprobante comprobante);
}
