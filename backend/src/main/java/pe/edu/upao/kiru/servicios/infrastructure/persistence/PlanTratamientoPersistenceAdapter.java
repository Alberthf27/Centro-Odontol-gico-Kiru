package pe.edu.upao.kiru.servicios.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.odontologos.application.port.OdontologoRepository;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;
import pe.edu.upao.kiru.servicios.application.port.PlanTratamientoRepository;
import pe.edu.upao.kiru.servicios.domain.EstadoPlanTratamiento;
import pe.edu.upao.kiru.servicios.domain.PlanTratamiento;
import pe.edu.upao.kiru.servicios.domain.Sesion;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;

@Repository
class PlanTratamientoPersistenceAdapter implements PlanTratamientoRepository {

    private final PlanTratamientoJpaSpringRepository planRepository;
    private final SesionJpaSpringRepository sesionRepository;
    private final OdontologoRepository odontologoRepository;

    PlanTratamientoPersistenceAdapter(
            PlanTratamientoJpaSpringRepository planRepository,
            SesionJpaSpringRepository sesionRepository,
            OdontologoRepository odontologoRepository
    ) {
        this.planRepository = planRepository;
        this.sesionRepository = sesionRepository;
        this.odontologoRepository = odontologoRepository;
    }

    @Override
    public List<PlanTratamiento> findActivosConSesionesPendientes(String idCliente) {
        try {
            return planRepository.findConSesionesPendientes(
                    Integer.valueOf(idCliente),
                    EstadoPlanTratamiento.ACTIVO
            ).stream().map(this::toDomain).toList();
        } catch (NumberFormatException exception) {
            return List.of();
        }
    }

    @Override
    public Optional<PlanTratamiento> findActivoConSesionPendienteParaActualizar(
            String idCliente,
            String idSesion
    ) {
        try {
            SesionJpaEntity sesion = sesionRepository.findPendienteParaClienteConBloqueo(
                    Integer.valueOf(idSesion),
                    Integer.valueOf(idCliente),
                    EstadoPlanTratamiento.ACTIVO
            ).orElse(null);
            if (sesion == null) {
                return Optional.empty();
            }
            return planRepository.findById(sesion.getIdPlan()).map(this::toDomain);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void liberarSesionDeCita(String nroCita) {
        sesionRepository.liberarPorNroCita(nroCita);
    }

    private PlanTratamiento toDomain(PlanTratamientoJpaEntity entity) {
        TratamientoJpaEntity tratamientoEntity = entity.getTratamiento();
        Tratamiento tratamiento = new Tratamiento(
                tratamientoEntity.getIdTratamiento().toString(),
                tratamientoEntity.getNombre(),
                tratamientoEntity.isMultiplesSesiones(),
                tratamientoEntity.getPrecio(),
                tratamientoEntity.isEstado() ? "ACTIVO" : "INACTIVO"
        );
        Odontologo odontologo = odontologoRepository.findById(entity.getIdOdontologo().toString())
                .orElseGet(() -> Odontologo.reconstituir(entity.getIdOdontologo().toString(), null));
        Cliente cliente = Cliente.reconstituir(
                entity.getIdCliente().toString(), null, null, null, null, null, null, true
        );
        List<Sesion> sesiones = sesionRepository
                .findByIdPlanAndIdCitaIsNullOrderByOrdenAsc(entity.getIdPlan())
                .stream()
                .map(sesion -> new Sesion(
                        sesion.getIdSesion().toString(),
                        sesion.getNombreTipo(),
                        sesion.getOrden(),
                        sesion.getDescripcion()
                ))
                .toList();
        return new PlanTratamiento(
                entity.getIdPlan().toString(),
                entity.getDescripcion(),
                entity.getFechaInicio(),
                entity.getEstado(),
                entity.getProgreso(),
                cliente,
                odontologo,
                tratamiento,
                sesiones
        );
    }
}
