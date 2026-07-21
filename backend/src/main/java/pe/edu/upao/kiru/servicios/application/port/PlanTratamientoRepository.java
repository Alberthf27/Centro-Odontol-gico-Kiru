package pe.edu.upao.kiru.servicios.application.port;

import java.util.List;
import java.util.Optional;
import pe.edu.upao.kiru.servicios.domain.PlanTratamiento;

public interface PlanTratamientoRepository {

    List<PlanTratamiento> findActivosConSesionesPendientes(String idCliente);

    Optional<PlanTratamiento> findActivoConSesionPendienteParaActualizar(
            String idCliente,
            String idSesion
    );

    void liberarSesionDeCita(String nroCita);
}
