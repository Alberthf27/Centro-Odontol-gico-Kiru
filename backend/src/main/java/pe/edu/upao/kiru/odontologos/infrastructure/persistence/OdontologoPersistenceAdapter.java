package pe.edu.upao.kiru.odontologos.infrastructure.persistence;

import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.odontologos.application.port.OdontologoRepository;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;

@Repository
class OdontologoPersistenceAdapter implements OdontologoRepository {

    private final OdontologoJpaSpringRepository repository;

    OdontologoPersistenceAdapter(OdontologoJpaSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Odontologo> findById(String idOdontologo) {
        try {
            return repository.findConEmpleadoById(Integer.valueOf(idOdontologo))
                    .map(this::toDomain);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Odontologo> findActivos() {
        return repository.findActivos().stream().map(this::toDomain).toList();
    }

    private Odontologo toDomain(OdontologoJpaEntity entity) {
        return Odontologo.reconstituir(
                entity.getIdOdontologo().toString(),
                entity.getEspecialidad(),
                entity.getNombreCompleto()
        );
    }
}
