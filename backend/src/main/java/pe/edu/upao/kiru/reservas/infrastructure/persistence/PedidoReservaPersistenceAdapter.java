package pe.edu.upao.kiru.reservas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.citas.application.port.CitaRepository;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.citas.infrastructure.persistence.CitaJpaEntity;
import pe.edu.upao.kiru.citas.infrastructure.persistence.CitaJpaSpringRepository;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.disponibilidades.infrastructure.persistence.FranjaHorariaJpaSpringRepository;
import pe.edu.upao.kiru.reservas.application.port.PedidoReservaRepository;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import pe.edu.upao.kiru.servicios.infrastructure.persistence.SesionJpaSpringRepository;
import pe.edu.upao.kiru.shared.exception.ConflictException;

@Repository
class PedidoReservaPersistenceAdapter implements PedidoReservaRepository {

    private final PedidoReservaJpaSpringRepository repository;
    private final CitaJpaSpringRepository citaRepository;
    private final CitaRepository citaPort;
    private final FranjaHorariaJpaSpringRepository franjaRepository;
    private final SesionJpaSpringRepository sesionRepository;

    PedidoReservaPersistenceAdapter(
            PedidoReservaJpaSpringRepository repository,
            CitaJpaSpringRepository citaRepository,
            CitaRepository citaPort,
            FranjaHorariaJpaSpringRepository franjaRepository,
            SesionJpaSpringRepository sesionRepository
    ) {
        this.repository = repository;
        this.citaRepository = citaRepository;
        this.citaPort = citaPort;
        this.franjaRepository = franjaRepository;
        this.sesionRepository = sesionRepository;
    }

    @Override
    public PedidoReserva save(PedidoReserva pedido) {
        Optional<PedidoReservaJpaEntity> existing = repository.findByNroPedido(pedido.getNroPedido());
        PedidoReservaJpaEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.actualizar(pedido.getMontoTotal(), pedido.getEstadoPago());
            entity = repository.save(entity);
        } else {
            entity = repository.save(new PedidoReservaJpaEntity(
                    Integer.valueOf(pedido.getCliente().getIdCliente()),
                    pedido.getNroPedido(),
                    pedido.getFecha(),
                    pedido.getMontoTotal(),
                    pedido.getEstadoPago()
            ));
            for (Cita cita : pedido.getCitas()) {
                CitaJpaEntity citaEntity = citaRepository.save(new CitaJpaEntity(
                        entity.getIdPedido(),
                        Integer.valueOf(cita.getCliente().getIdCliente()),
                        Integer.valueOf(cita.getOdontologo().getIdOdontologo()),
                        cita.getTratamiento() == null ? null : Integer.valueOf(cita.getTratamiento().getIdTratamiento()),
                        cita.getNroCita(),
                        cita.getFecha(),
                        cita.getHoraInicio(),
                        cita.getHoraFin(),
                        cita.getEstado()
                ));
                franjaRepository.asignarCita(Integer.valueOf(cita.getFranjaHoraria().getIdFranja()), citaEntity.getIdCita());
                if (cita.getSesion() != null
                        && sesionRepository.asociarCita(
                                Integer.valueOf(cita.getSesion().getIdSesion()),
                                citaEntity.getIdCita()
                        ) != 1) {
                    throw new ConflictException("La sesión seleccionada ya fue programada.");
                }
            }
        }
        return toDomain(entity);
    }

    @Override
    public Optional<PedidoReserva> findById(String idPedido) {
        try {
            return repository.findById(Integer.valueOf(idPedido)).map(this::toDomain);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private PedidoReserva toDomain(PedidoReservaJpaEntity entity) {
        Cliente cliente = Cliente.reconstituir(
                entity.getIdCliente().toString(), null, null, null, null, null, null, true
        );
        List<Cita> citas = citaRepository.findByIdPedido(entity.getIdPedido()).stream()
                .map(cita -> citaPort.findById(cita.getNroCita()).orElseThrow())
                .toList();
        return PedidoReserva.reconstituir(
                entity.getIdPedido().toString(),
                entity.getNroPedido(),
                entity.getFechaCreacion(),
                entity.getMontoTotal(),
                entity.getEstadoPago(),
                cliente,
                citas
        );
    }
}
