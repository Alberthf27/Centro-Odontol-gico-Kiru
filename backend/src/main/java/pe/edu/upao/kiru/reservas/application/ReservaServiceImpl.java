package pe.edu.upao.kiru.reservas.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upao.kiru.citas.application.port.CitaRepository;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.citas.domain.TipoCita;
import pe.edu.upao.kiru.clientes.application.port.ClienteRepository;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.disponibilidades.application.port.FranjaHorariaRepository;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;
import pe.edu.upao.kiru.odontologos.application.port.OdontologoRepository;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;
import pe.edu.upao.kiru.reservas.application.dto.CrearPedidoReservaCommand;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarCitaCommand;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarClienteCommand;
import pe.edu.upao.kiru.reservas.application.dto.ReprogramarCitaCommand;
import pe.edu.upao.kiru.reservas.application.port.CodigoReservaGenerator;
import pe.edu.upao.kiru.reservas.application.port.PedidoReservaRepository;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import pe.edu.upao.kiru.servicios.application.port.TratamientoRepository;
import pe.edu.upao.kiru.servicios.application.port.PlanTratamientoRepository;
import pe.edu.upao.kiru.servicios.domain.PlanTratamiento;
import pe.edu.upao.kiru.servicios.domain.Sesion;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;
import pe.edu.upao.kiru.shared.exception.BusinessRuleException;
import pe.edu.upao.kiru.shared.exception.ConflictException;
import pe.edu.upao.kiru.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ClienteRepository clienteRepository;
    private final OdontologoRepository odontologoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final PlanTratamientoRepository planTratamientoRepository;
    private final FranjaHorariaRepository franjaHorariaRepository;
    private final CitaRepository citaRepository;
    private final PedidoReservaRepository pedidoReservaRepository;
    private final CodigoReservaGenerator codigoReservaGenerator;
    private final Clock clock;
    private final BigDecimal precioConsulta;

    public ReservaServiceImpl(
            ClienteRepository clienteRepository,
            OdontologoRepository odontologoRepository,
            TratamientoRepository tratamientoRepository,
            PlanTratamientoRepository planTratamientoRepository,
            FranjaHorariaRepository franjaHorariaRepository,
            CitaRepository citaRepository,
            PedidoReservaRepository pedidoReservaRepository,
            CodigoReservaGenerator codigoReservaGenerator,
            Clock clock,
            @Value("${app.reservas.precio-consulta:50.00}") BigDecimal precioConsulta
    ) {
        this.clienteRepository = clienteRepository;
        this.odontologoRepository = odontologoRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.planTratamientoRepository = planTratamientoRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.citaRepository = citaRepository;
        this.pedidoReservaRepository = pedidoReservaRepository;
        this.codigoReservaGenerator = codigoReservaGenerator;
        this.clock = clock;
        this.precioConsulta = precioConsulta;
    }

    @Override
    public Cliente registrarCliente(String authUserId, RegistrarClienteCommand command) {
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
        return clienteRepository.save(cliente, authUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerClienteActual(String authUserId) {
        return obtenerCliente(authUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FranjaHoraria> consultarDisponibilidad(String idOdontologo, LocalDate fecha) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (fecha.isBefore(ahora.toLocalDate())) {
            throw new BusinessRuleException("No se puede consultar disponibilidad de una fecha pasada.");
        }
        LocalTime horaActual = ahora.toLocalTime();
        return franjaHorariaRepository.findPorOdontologoYFecha(idOdontologo, fecha).stream()
                .filter(franja -> !fecha.equals(ahora.toLocalDate())
                        || franja.getHoraInicio().isAfter(horaActual))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanTratamiento> listarPlanesPendientes(String authUserId) {
        Cliente cliente = obtenerCliente(authUserId);
        return planTratamientoRepository.findActivosConSesionesPendientes(cliente.getIdCliente());
    }

    @Override
    public BigDecimal obtenerPrecioConsulta() {
        return precioConsulta;
    }

    @Override
    public PedidoReserva registrarPedido(String authUserId, CrearPedidoReservaCommand command) {
        Cliente cliente = obtenerCliente(authUserId);
        PedidoReserva pedido = new PedidoReserva(
                codigoReservaGenerator.siguientePedido(),
                LocalDateTime.now(clock),
                cliente
        );

        validarSesionesNoRepetidas(command);

        for (RegistrarCitaCommand citaCommand : command.citas()) {
            CitaConMonto cita = crearCita(cliente, citaCommand);
            pedido.agregarCita(cita.cita(), cita.monto());
        }

        return pedidoReservaRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPropias(String authUserId) {
        return citaRepository.findByCliente(obtenerCliente(authUserId));
    }

    @Override
    public void cancelarCita(String authUserId, String idCita) {
        Cliente cliente = obtenerCliente(authUserId);
        Cita cita = obtenerCita(idCita);
        validarPropiedad(cita, cliente);
        validarPlazo(cita, LocalDateTime.now(clock));

        cita.cancelar();
        citaRepository.save(cita);
        planTratamientoRepository.liberarSesionDeCita(cita.getNroCita());
        franjaHorariaRepository.liberar(cita.getFranjaHoraria().getIdFranja());
    }

    @Override
    public Cita reprogramarCita(
            String authUserId,
            ReprogramarCitaCommand command
    ) {
        Cliente cliente = obtenerCliente(authUserId);
        Cita cita = obtenerCita(command.idCita());
        validarPropiedad(cita, cliente);
        validarPlazo(cita, LocalDateTime.now(clock));

        FranjaHoraria nuevaFranja = obtenerFranja(command.idFranjaNueva());
        if (!nuevaFranja.estaDisponible()
                || !franjaHorariaRepository.ocuparSiDisponible(nuevaFranja.getIdFranja())) {
            throw new ConflictException("La nueva franja horaria ya no está disponible.");
        }

        String idFranjaAnterior = cita.getFranjaHoraria().getIdFranja();
        // La BD permite que una cita esté vinculada a una sola franja. Liberar la
        // anterior antes de guardar evita que ambas la referencien durante el cambio;
        // la transacción revierte también esta liberación si el guardado falla.
        franjaHorariaRepository.liberar(idFranjaAnterior);
        cita.reprogramar(nuevaFranja);
        nuevaFranja.asignarCita(cita);
        Cita actualizada = citaRepository.save(cita);
        return actualizada;
    }

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

    private void validarSesionesNoRepetidas(CrearPedidoReservaCommand command) {
        Set<String> sesiones = new HashSet<>();
        boolean repetida = command.citas().stream()
                .map(RegistrarCitaCommand::idSesion)
                .filter(idSesion -> idSesion != null && !idSesion.isBlank())
                .anyMatch(idSesion -> !sesiones.add(idSesion));
        if (repetida) {
            throw new BusinessRuleException("Una sesión de tratamiento no puede añadirse dos veces al pedido.");
        }
    }

    private Cliente obtenerCliente(String authUserId) {
        return clienteRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cliente autenticado."));
    }

    private Cita obtenerCita(String idCita) {
        return citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la cita."));
    }

    private FranjaHoraria obtenerFranja(String idFranja) {
        return franjaHorariaRepository.findById(idFranja)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la franja horaria."));
    }

    private void validarPropiedad(Cita cita, Cliente cliente) {
        if (!cita.perteneceA(cliente)) {
            throw new ResourceNotFoundException("No se encontró la cita del cliente autenticado.");
        }
    }

    private void validarPlazo(Cita cita, LocalDateTime ahora) {
        if (!cita.permiteCancelacionOReprogramacion(ahora)) {
            throw new BusinessRuleException(
                    "La cancelación o reprogramación requiere al menos 24 horas de anticipación."
            );
        }
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
