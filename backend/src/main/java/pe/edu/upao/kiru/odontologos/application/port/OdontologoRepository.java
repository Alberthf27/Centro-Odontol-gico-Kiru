package pe.edu.upao.kiru.odontologos.application.port;

import java.util.Optional;
import java.util.List;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;

public interface OdontologoRepository {

    Optional<Odontologo> findById(String idOdontologo);

    List<Odontologo> findActivos();
}
