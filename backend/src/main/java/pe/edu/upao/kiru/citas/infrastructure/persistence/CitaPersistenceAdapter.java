package pe.edu.upao.kiru.citas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.citas.application.port.CitaRepository;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.disponibilidades.application.port.FranjaHorariaRepository;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;
import pe.edu.upao.kiru.disponibilidades.infrastructure.persistence.FranjaHorariaJpaSpringRepository;
import pe.edu.upao.kiru.odontologos.application.port.OdontologoRepository;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;
import pe.edu.upao.kiru.servicios.application.port.TratamientoRepository;
import pe.edu.upao.kiru.servicios.domain.Sesion;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;
import pe.edu.upao.kiru.servicios.infrastructure.persistence.SesionJpaEntity;
import pe.edu.upao.kiru.servicios.infrastructure.persistence.SesionJpaSpringRepository;

@Repository
class CitaPersistenceAdapter implements CitaRepository {

    private final CitaJpaSpringRepository repository;
    private final OdontologoRepository odontologoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final FranjaHorariaRepository franjaHorariaRepository;
    private final FranjaHorariaJpaSpringRepository franjaJpaRepository;
    private final SesionJpaSpringRepository sesionRepository;

    CitaPersistenceAdapter(
            CitaJpaSpringRepository repository,
            OdontologoRepository odontologoRepository,
            TratamientoRepository tratamientoRepository,
            FranjaHorariaRepository franjaHorariaRepository,
            FranjaHorariaJpaSpringRepository franjaJpaRepository,
            SesionJpaSpringRepository sesionRepository
    ) {
        this.repository = repository;
        this.odontologoRepository = odontologoRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.franjaJpaRepository = franjaJpaRepository;
        this.sesionRepository = sesionRepository;
    }

    @Override
    public Cita save(Cita cita) {
        CitaJpaEntity entity = repository.findByNroCita(cita.getNroCita())
                .orElseThrow(() -> new IllegalStateException("Una cita nueva se registra dentro de un pedido de reserva."));
        entity.actualizar(
                cita.getEstado(),
                Integer.valueOf(cita.getOdontologo().getIdOdontologo()),
                cita.getTratamiento() == null ? null : Integer.valueOf(cita.getTratamiento().getIdTratamiento()),
                cita.getFecha(),
                cita.getHoraInicio(),
                cita.getHoraFin(),
                cita.getNotaSesion(),
                cita.isAsistenciaPaciente(),
                cita.isAsistenciaOdontologo()
        );
        CitaJpaEntity saved = repository.save(entity);
        franjaJpaRepository.asignarCita(Integer.valueOf(cita.getFranjaHoraria().getIdFranja()), saved.getIdCita());
        return toDomain(saved);
    }

    @Override
    public Optional<Cita> findById(String idCita) {
        return repository.findByNroCita(idCita).map(this::toDomain);
    }

    @Override
    public List<Cita> findByCliente(Cliente cliente) {
        if (cliente.getIdCliente() == null) return List.of();
        return repository.findByIdClienteOrderByIdCitaDesc(Integer.valueOf(cliente.getIdCliente()))
                .stream().map(this::toDomain).toList();
    }

    private Cita toDomain(CitaJpaEntity entity) {
        Cliente cliente = Cliente.reconstituir(
                entity.getIdCliente().toString(), null, null, null, null, null, null, true
        );
        Odontologo odontologo = odontologoRepository.findById(entity.getIdOdontologo().toString())
                .orElseGet(() -> Odontologo.reconstituir(entity.getIdOdontologo().toString(), null));
        Tratamiento tratamiento = entity.getIdTratamiento() == null ? null
                : tratamientoRepository.findById(entity.getIdTratamiento().toString()).orElse(null);
        Sesion sesion = sesionRepository.findByIdCita(entity.getIdCita())
                .map(this::toSesionDomain)
                .orElse(null);
        FranjaHoraria franja = franjaHorariaRepository.findByCitaId(entity.getIdCita().toString()).orElse(null);
        return Cita.reconstituir(
                entity.getNroCita(), entity.getEstado(), cliente, odontologo, tratamiento,
                sesion,
                franja,
                entity.getFechaProgramada(),
                entity.getHoraInicio(),
                entity.getHoraFin(),
                entity.getNotaSesion(),
                entity.getAsistenciaOdontologo(),
                entity.getAsistenciaCliente()
        );
    }

    private Sesion toSesionDomain(SesionJpaEntity entity) {
        return new Sesion(
                entity.getIdSesion().toString(),
                entity.getNombreTipo(),
                entity.getOrden(),
                entity.getDescripcion()
        );
    }
}
