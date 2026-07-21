package pe.edu.upao.kiru.recepcion.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upao.kiru.citas.application.port.CitaRepository;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.citas.domain.EstadoCita;
import pe.edu.upao.kiru.citas.domain.TipoCita;
import pe.edu.upao.kiru.clientes.application.port.ClienteRepository;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.disponibilidades.application.port.FranjaHorariaRepository;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;
import pe.edu.upao.kiru.odontologos.application.port.OdontologoRepository;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarCitaCommand;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarClienteCommand;
import pe.edu.upao.kiru.reservas.application.dto.ReprogramarCitaCommand;
import pe.edu.upao.kiru.reservas.application.port.CodigoReservaGenerator;
import pe.edu.upao.kiru.reservas.application.port.PedidoReservaRepository;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import pe.edu.upao.kiru.servicios.application.port.PlanTratamientoRepository;
import pe.edu.upao.kiru.servicios.application.port.TratamientoRepository;
import pe.edu.upao.kiru.servicios.domain.PlanTratamiento;
import pe.edu.upao.kiru.servicios.domain.Sesion;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;
import pe.edu.upao.kiru.shared.exception.BusinessRuleException;
import pe.edu.upao.kiru.shared.exception.ConflictException;
import pe.edu.upao.kiru.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class RecepcionServiceImpl implements RecepcionService {

    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;
    private final OdontologoRepository odontologoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final PlanTratamientoRepository planTratamientoRepository;
    private final FranjaHorariaRepository franjaHorariaRepository;
    private final PedidoReservaRepository pedidoReservaRepository;
    private final CodigoReservaGenerator codigoReservaGenerator;
    private final Clock clock;
    private final BigDecimal precioConsulta;

    public RecepcionServiceImpl(
            ClienteRepository clienteRepository,
            CitaRepository citaRepository,
            OdontologoRepository odontologoRepository,
            TratamientoRepository tratamientoRepository,
            PlanTratamientoRepository planTratamientoRepository,
            FranjaHorariaRepository franjaHorariaRepository,
            PedidoReservaRepository pedidoReservaRepository,
            CodigoReservaGenerator codigoReservaGenerator,
            Clock clock,
            @Value("${app.reservas.precio-consulta:50.00}") BigDecimal precioConsulta
    ) {
        this.clienteRepository = clienteRepository;
        this.citaRepository = citaRepository;
        this.odontologoRepository = odontologoRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.planTratamientoRepository = planTratamientoRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.pedidoReservaRepository = pedidoReservaRepository;
        this.codigoReservaGenerator = codigoReservaGenerator;
        this.clock = clock;
        this.precioConsulta = precioConsulta;
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarClientePorDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new BusinessRuleException("El DNI debe tener 8 dígitos. Corrija el DNI ingresado.");
        }
        return clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    @Override
    public Cliente registrarClientePresencial(RegistrarClienteCommand command) {
        if (clienteRepository.existsByDni(command.dni())) {
            throw new ConflictException("Ya existe un cliente registrado con ese DNI.");
        }
        Cliente cliente = new Cliente(
                command.dni(),
                command.nombres(),
                command.apellidos(),
                command.fechaNacimiento(),
                command.celular(),
                command.domicilio()
        );
        return clienteRepository.savePresencial(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        // Sin fecha el monitor muestra todas las citas registradas (vista por
        // defecto); con fecha actúa como filtro por día.
        if (fecha == null) {
            return citaRepository.findTodas();
        }
        return citaRepository.findByFecha(fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasDeCliente(String idCliente) {
        return citaRepository.findByCliente(obtenerCliente(idCliente));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanTratamiento> listarPlanesPendientes(String idCliente) {
        return planTratamientoRepository.findActivosConSesionesPendientes(
                obtenerCliente(idCliente).getIdCliente()
        );
    }

    @Override
    public PedidoReserva registrarCitaPresencial(String idCliente, RegistrarCitaCommand command) {
        Cliente cliente = obtenerCliente(idCliente);
        PedidoReserva pedido = new PedidoReserva(
                codigoReservaGenerator.siguientePedido(),
                LocalDateTime.now(clock),
                cliente
        );
        CitaConMonto cita = crearCita(cliente, command);
        pedido.agregarCita(cita.cita(), cita.monto());
        return pedidoReservaRepository.save(pedido);
    }

    @Override
    public Cita registrarAsistenciaPaciente(String idCita) {
        Cita cita = obtenerCita(idCita);
        boolean habilitada = cita.getFecha().equals(LocalDate.now(clock))
                && (cita.getEstado() == EstadoCita.PENDIENTE || cita.getEstado() == EstadoCita.REPROGRAMADA)
                && !cita.isAsistenciaPaciente();
        if (!habilitada) {
            throw new BusinessRuleException("Asistencia no habilitada.");
        }
        cita.actualizarAsistencia(true, cita.isAsistenciaOdontologo());
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(String idCita) {
        Cita cita = obtenerCita(idCita);
        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.ATENDIDA) {
            throw new BusinessRuleException("La cita no se encuentra disponible para cancelación.");
        }
        cita.cancelar();
        citaRepository.save(cita);
        planTratamientoRepository.liberarSesionDeCita(cita.getNroCita());
        franjaHorariaRepository.liberar(cita.getFranjaHoraria().getIdFranja());
    }

    @Override
    public Cita reprogramarCita(ReprogramarCitaCommand command) {
        Cita cita = obtenerCita(command.idCita());
        if (!cita.permiteCancelacionOReprogramacion(LocalDateTime.now(clock))) {
            throw new BusinessRuleException(
                    "No se puede reprogramar la cita faltando menos de 24 horas para su inicio."
            );
        }

        FranjaHoraria nuevaFranja = obtenerFranja(command.idFranjaNueva());
        if (!nuevaFranja.estaDisponible()
                || !franjaHorariaRepository.ocuparSiDisponible(nuevaFranja.getIdFranja())) {
            throw new ConflictException("La nueva franja horaria ya no está disponible.");
        }

        franjaHorariaRepository.liberar(cita.getFranjaHoraria().getIdFranja());
        cita.reprogramar(nuevaFranja);
        nuevaFranja.asignarCita(cita);
        return citaRepository.save(cita);
    }

    /**
     * Misma regla de creación que la reserva web, pero tomando como parámetro
     * al cliente seleccionado en recepción (CU Registrar cita médica, flujo 1.1).
     */
    private CitaConMonto crearCita(Cliente cliente, RegistrarCitaCommand command) {
        Odontologo odontologo = odontologoRepository.findById(command.idOdontologo())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el odontólogo."));
        Tratamiento tratamiento = null;
        Sesion sesion = null;
        BigDecimal monto = precioConsulta;

        if (command.tipoCita() == TipoCita.TRATAMIENTO) {
            tratamiento = tratamientoRepository.findById(command.idTratamiento())
                    .filter(Tratamiento::estaActivo)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró el tratamiento activo."));
            monto = tratamiento.getPrecio();
            if (tratamiento.isMultiplesSesiones()) {
                if (command.idSesion() == null || command.idSesion().isBlank()) {
                    throw new BusinessRuleException("Selecciona una sesión pendiente del plan de tratamiento.");
                }
                PlanTratamiento plan = planTratamientoRepository
                        .findActivoConSesionPendienteParaActualizar(
                                cliente.getIdCliente(), command.idSesion()
                        )
                        .orElseThrow(() -> new ConflictException(
                                "La sesión ya fue programada o no pertenece a un plan activo del cliente."
                        ));
                if (!Objects.equals(
                        plan.getTratamiento().getIdTratamiento(),
                        tratamiento.getIdTratamiento()
                )) {
                    throw new BusinessRuleException("La sesión no corresponde al tratamiento seleccionado.");
                }
                if (!Objects.equals(
                        plan.getOdontologo().getIdOdontologo(),
                        odontologo.getIdOdontologo()
                )) {
                    throw new BusinessRuleException("La sesión debe reservarse con el odontólogo asignado al plan.");
                }
                sesion = plan.obtenerSesion(command.idSesion());
            } else if (command.idSesion() != null && !command.idSesion().isBlank()) {
                throw new BusinessRuleException("Una atención única no requiere seleccionar una sesión de plan.");
            }
        }
        FranjaHoraria franja = obtenerFranja(command.idFranja());

        if (!correspondeAlOdontologo(franja, odontologo)) {
            throw new BusinessRuleException("La franja no corresponde al odontólogo seleccionado.");
        }
        LocalDateTime inicioFranja = LocalDateTime.of(franja.getFecha(), franja.getHoraInicio());
        if (!inicioFranja.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("No se puede registrar una cita en una fecha pasada.");
        }
        if (!franja.estaDisponible()
                || !franjaHorariaRepository.ocuparSiDisponible(franja.getIdFranja())) {
            throw new ConflictException("La franja horaria ya no está disponible.");
        }

        Cita cita = new Cita(
                codigoReservaGenerator.siguienteCita(),
                cliente,
                odontologo,
                tratamiento,
                sesion,
                franja
        );
        franja.asignarCita(cita);
        return new CitaConMonto(cita, monto);
    }

    private Cliente obtenerCliente(String idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private Cita obtenerCita(String idCita) {
        return citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la cita."));
    }

    private FranjaHoraria obtenerFranja(String idFranja) {
        return franjaHorariaRepository.findById(idFranja)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la franja horaria."));
    }

    private boolean correspondeAlOdontologo(FranjaHoraria franja, Odontologo odontologo) {
        Odontologo odontologoDeFranja = franja.getOdontologo();
        return odontologoDeFranja == odontologo
                || (odontologoDeFranja != null
                && odontologoDeFranja.getIdOdontologo() != null
                && Objects.equals(odontologoDeFranja.getIdOdontologo(), odontologo.getIdOdontologo()));
    }

    private record CitaConMonto(Cita cita, BigDecimal monto) {
    }
}
