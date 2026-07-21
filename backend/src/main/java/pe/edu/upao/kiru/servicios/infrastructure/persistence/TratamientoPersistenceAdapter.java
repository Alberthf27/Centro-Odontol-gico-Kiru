package pe.edu.upao.kiru.servicios.infrastructure.persistence;

import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.servicios.application.port.TratamientoRepository;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;

@Repository
class TratamientoPersistenceAdapter implements TratamientoRepository {

    private final TratamientoJpaSpringRepository repository;

    TratamientoPersistenceAdapter(TratamientoJpaSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Tratamiento> findById(String idTratamiento) {
        try {
            return repository.findById(Integer.valueOf(idTratamiento)).map(this::toDomain);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Tratamiento> findActivos() {
        return repository.findByEstadoTrueOrderByNombreAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    private Tratamiento toDomain(TratamientoJpaEntity entity) {
        return new Tratamiento(
                entity.getIdTratamiento().toString(),
                entity.getNombre(),
                entity.isMultiplesSesiones(),
                entity.getPrecio(),
                entity.isEstado() ? "ACTIVO" : "INACTIVO"
        );
    }
}
