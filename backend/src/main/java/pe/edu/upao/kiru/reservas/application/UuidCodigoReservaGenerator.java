package pe.edu.upao.kiru.reservas.application;

import java.util.UUID;
import org.springframework.stereotype.Component;
import pe.edu.upao.kiru.reservas.application.port.CodigoReservaGenerator;

@Component
public class UuidCodigoReservaGenerator implements CodigoReservaGenerator {

    @Override
    public String siguientePedido() {
        return siguienteCodigo("PED");
    }

    @Override
    public String siguienteCita() {
        return siguienteCodigo("CITA");
    }

    private String siguienteCodigo(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}
