package pe.edu.upao.kiru.pagos.application;

import java.util.UUID;
import org.springframework.stereotype.Component;
import pe.edu.upao.kiru.pagos.application.port.CodigoComprobanteGenerator;

/** Genera un número visible y único para el comprobante de pago web. */
@Component
public class UuidCodigoComprobanteGenerator implements CodigoComprobanteGenerator {

    @Override
    public String siguienteBoleta() {
        return "BOL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
