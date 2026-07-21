package pe.edu.upao.kiru.servicios.application.port;

import java.util.Optional;
import java.util.List;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;

public interface TratamientoRepository {

    Optional<Tratamiento> findById(String idTratamiento);

    List<Tratamiento> findActivos();
}
