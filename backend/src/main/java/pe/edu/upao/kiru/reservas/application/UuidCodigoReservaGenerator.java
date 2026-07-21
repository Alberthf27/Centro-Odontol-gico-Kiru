package pe.edu.upao.kiru.reservas.application;

import java.util.UUID;
import org.springframework.stereotype.Component;
import pe.edu.upao.kiru.reservas.application.port.CodigoReservaGenerator;

@Component
public class UuidCodigoReservaGenerator implements CodigoReservaGenerator {

    @Override
    public String siguientePedido() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String siguienteCita() {
        return UUID.randomUUID().toString();
    }
}
