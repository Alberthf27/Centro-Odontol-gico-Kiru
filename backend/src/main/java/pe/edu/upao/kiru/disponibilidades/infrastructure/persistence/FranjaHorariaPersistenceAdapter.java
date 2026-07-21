package pe.edu.upao.kiru.disponibilidades.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.disponibilidades.application.port.FranjaHorariaRepository;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;

@Repository
class FranjaHorariaPersistenceAdapter implements FranjaHorariaRepository {

    private final FranjaHorariaJpaSpringRepository repository;

    FranjaHorariaPersistenceAdapter(FranjaHorariaJpaSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<FranjaHoraria> findPorOdontologoYFecha(String idOdontologo, LocalDate fecha) {
        try {
            return repository.findByIdOdontologoAndFechaOrderByHoraInicioAsc(Integer.valueOf(idOdontologo), fecha)
                    .stream().map(this::toDomain).toList();
        } catch (NumberFormatException exception) {
            return List.of();
        }
    }

    @Override
    public Optional<FranjaHoraria> findById(String idFranja) {
        try {
            return repository.findById(Integer.valueOf(idFranja)).map(this::toDomain);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FranjaHoraria> findByCitaId(String idCita) {
        try {
            return repository.findByIdCita(Integer.valueOf(idCita)).map(this::toDomain);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public boolean ocuparSiDisponible(String idFranja) {
        try {
            return repository.ocuparSiDisponible(Integer.valueOf(idFranja)) == 1;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    @Override
    public void liberar(String idFranja) {
        try {
            repository.liberar(Integer.valueOf(idFranja));
        } catch (NumberFormatException ignored) {
            // El servicio ya valida la existencia de la franja antes de liberarla.
        }
    }

    private FranjaHoraria toDomain(FranjaHorariaJpaEntity entity) {
        Odontologo odontologo = Odontologo.reconstituir(entity.getIdOdontologo().toString(), null);
        return new FranjaHoraria(
                entity.getIdFranja().toString(),
                entity.getFecha(),
                entity.getHoraInicio(),
                entity.getHoraFin(),
                entity.isDisponible(),
                odontologo
        );
    }
}
