package pe.edu.upao.kiru.reservas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import pe.edu.upao.kiru.reservas.application.dto.CrearPedidoReservaCommand;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarCitaCommand;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarClienteCommand;
import pe.edu.upao.kiru.reservas.application.dto.ReprogramarCitaCommand;
import pe.edu.upao.kiru.reservas.application.port.CodigoReservaGenerator;
import pe.edu.upao.kiru.reservas.application.port.PedidoReservaRepository;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import pe.edu.upao.kiru.servicios.application.port.TratamientoRepository;
import pe.edu.upao.kiru.servicios.application.port.PlanTratamientoRepository;
import pe.edu.upao.kiru.servicios.domain.EstadoPlanTratamiento;
import pe.edu.upao.kiru.servicios.domain.PlanTratamiento;
import pe.edu.upao.kiru.servicios.domain.Sesion;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;
import pe.edu.upao.kiru.shared.exception.BusinessRuleException;
import pe.edu.upao.kiru.shared.exception.ConflictException;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    private static final String AUTH_USER_ID = "auth-cliente-1";
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 20, 10, 0);

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private OdontologoRepository odontologoRepository;
    @Mock
    private TratamientoRepository tratamientoRepository;
    @Mock
    private PlanTratamientoRepository planTratamientoRepository;
    @Mock
    private FranjaHorariaRepository franjaHorariaRepository;
    @Mock
    private CitaRepository citaRepository;
    @Mock
    private PedidoReservaRepository pedidoReservaRepository;
    @Mock
    private CodigoReservaGenerator codigoReservaGenerator;

    private ReservaServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new ReservaServiceImpl(
                clienteRepository,
                odontologoRepository,
                tratamientoRepository,
                planTratamientoRepository,
                franjaHorariaRepository,
                citaRepository,
                pedidoReservaRepository,
                codigoReservaGenerator,
                clock,
                new BigDecimal("50.00")
        );
    }

    @Test
    void noRegistraClienteConDniDuplicado() {
        RegistrarClienteCommand command = new RegistrarClienteCommand(
                "71234567", "Ana", "Rojas", LocalDate.of(2000, 1, 1), "999111222", "Trujillo"
        );
        when(clienteRepository.existsByDni(command.dni())).thenReturn(true);

        assertThatThrownBy(() -> service.registrarCliente(AUTH_USER_ID, command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("DNI");
        verify(clienteRepository, never()).save(any(), any());
    }

    @Test
    void registraPedidoConVariasCitasYSumaElMonto() {
        Cliente cliente = cliente();
        Odontologo odontologo = new Odontologo();
        Tratamiento limpieza = tratamiento("TR-2", "Limpieza", "120.00");
        FranjaHoraria primera = franja("FR-1", AHORA.plusDays(2), odontologo);
        FranjaHoraria segunda = franja("FR-2", AHORA.plusDays(3), odontologo);
        CrearPedidoReservaCommand command = new CrearPedidoReservaCommand(List.of(
                new RegistrarCitaCommand(TipoCita.CONSULTA, null, null, "OD-1", "FR-1"),
                new RegistrarCitaCommand(TipoCita.TRATAMIENTO, "TR-2", null, "OD-1", "FR-2")
        ));

        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(codigoReservaGenerator.siguientePedido()).thenReturn("PED-1");
        when(codigoReservaGenerator.siguienteCita()).thenReturn("CIT-1", "CIT-2");
        when(odontologoRepository.findById("OD-1")).thenReturn(Optional.of(odontologo));
        when(tratamientoRepository.findById("TR-2")).thenReturn(Optional.of(limpieza));
        when(franjaHorariaRepository.findById("FR-1")).thenReturn(Optional.of(primera));
        when(franjaHorariaRepository.findById("FR-2")).thenReturn(Optional.of(segunda));
        when(franjaHorariaRepository.ocuparSiDisponible("FR-1")).thenReturn(true);
        when(franjaHorariaRepository.ocuparSiDisponible("FR-2")).thenReturn(true);
        when(pedidoReservaRepository.save(any(PedidoReserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PedidoReserva pedido = service.registrarPedido(AUTH_USER_ID, command);

        assertThat(pedido.getCitas()).hasSize(2);
        assertThat(pedido.getMontoTotal()).isEqualByComparingTo("170.00");
        assertThat(primera.estaDisponible()).isFalse();
        assertThat(segunda.estaDisponible()).isFalse();
        verify(franjaHorariaRepository).ocuparSiDisponible("FR-1");
        verify(franjaHorariaRepository).ocuparSiDisponible("FR-2");
    }

    @Test
    void rechazaUnaFranjaQueFueOcupadaPorOtraReserva() {
        Cliente cliente = cliente();
        Odontologo odontologo = new Odontologo();
        FranjaHoraria franja = franja("FR-1", AHORA.plusDays(2), odontologo);
        CrearPedidoReservaCommand command = new CrearPedidoReservaCommand(List.of(
                new RegistrarCitaCommand(TipoCita.CONSULTA, null, null, "OD-1", "FR-1")
        ));

        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(codigoReservaGenerator.siguientePedido()).thenReturn("PED-1");
        when(odontologoRepository.findById("OD-1")).thenReturn(Optional.of(odontologo));
        when(franjaHorariaRepository.findById("FR-1")).thenReturn(Optional.of(franja));
        when(franjaHorariaRepository.ocuparSiDisponible("FR-1")).thenReturn(false);

        assertThatThrownBy(() -> service.registrarPedido(AUTH_USER_ID, command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("disponible");
        verify(pedidoReservaRepository, never()).save(any());
    }

    @Test
    void registraSoloUnaSesionPendienteDelPlanActivoDelCliente() {
        Cliente cliente = Cliente.reconstituir(
                "CL-1", "71234567", "Ana", "Rojas", LocalDate.of(2000, 1, 1),
                "999111222", "Trujillo", true
        );
        Odontologo odontologo = Odontologo.reconstituir("OD-1", "Ortodoncia", "Dra. Salazar");
        Tratamiento ortodoncia = new Tratamiento(
                "TR-1", "Ortodoncia", true, new BigDecimal("120.00"), "ACTIVO"
        );
        Sesion sesion = new Sesion("SES-1", "Control de ortodoncia", 2, "Control pendiente");
        PlanTratamiento plan = new PlanTratamiento(
                "PLAN-1", "Plan activo", LocalDate.of(2026, 7, 1),
                EstadoPlanTratamiento.ACTIVO, BigDecimal.ZERO, cliente, odontologo,
                ortodoncia, List.of(sesion)
        );
        FranjaHoraria franja = franja("FR-1", AHORA.plusDays(2), odontologo);
        CrearPedidoReservaCommand command = new CrearPedidoReservaCommand(List.of(
                new RegistrarCitaCommand(
                        TipoCita.TRATAMIENTO, "TR-1", "SES-1", "OD-1", "FR-1"
                )
        ));

        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(codigoReservaGenerator.siguientePedido()).thenReturn("PED-1");
        when(codigoReservaGenerator.siguienteCita()).thenReturn("CIT-1");
        when(odontologoRepository.findById("OD-1")).thenReturn(Optional.of(odontologo));
        when(tratamientoRepository.findById("TR-1")).thenReturn(Optional.of(ortodoncia));
        when(planTratamientoRepository.findActivoConSesionPendienteParaActualizar("CL-1", "SES-1"))
                .thenReturn(Optional.of(plan));
        when(franjaHorariaRepository.findById("FR-1")).thenReturn(Optional.of(franja));
        when(franjaHorariaRepository.ocuparSiDisponible("FR-1")).thenReturn(true);
        when(pedidoReservaRepository.save(any(PedidoReserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PedidoReserva pedido = service.registrarPedido(AUTH_USER_ID, command);

        assertThat(pedido.getCitas()).singleElement().satisfies(cita -> {
            assertThat(cita.getTipoCita()).isEqualTo(TipoCita.TRATAMIENTO);
            assertThat(cita.getSesion()).isSameAs(sesion);
            assertThat(cita.getOdontologo()).isSameAs(odontologo);
        });
        assertThat(pedido.getMontoTotal()).isEqualByComparingTo("120.00");
    }

    @Test
    void rechazaCancelacionConMenosDeVeinticuatroHoras() {
        Cliente cliente = cliente();
        FranjaHoraria franja = franja("FR-1", AHORA.plusHours(23), new Odontologo());
        Cita cita = cita("CIT-1", cliente, franja);
        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(citaRepository.findById("CIT-1")).thenReturn(Optional.of(cita));

        assertThatThrownBy(() -> service.cancelarCita(AUTH_USER_ID, "CIT-1"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("24 horas");
        verify(citaRepository, never()).save(any());
        verify(franjaHorariaRepository, never()).liberar(any());
    }

    @Test
    void permiteCancelacionConExactamenteVeinticuatroHoras() {
        Cliente cliente = cliente();
        FranjaHoraria franja = franja("FR-1", AHORA.plusHours(24), new Odontologo());
        Cita cita = cita("CIT-1", cliente, franja);
        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(citaRepository.findById("CIT-1")).thenReturn(Optional.of(cita));
        when(citaRepository.save(cita)).thenReturn(cita);

        service.cancelarCita(AUTH_USER_ID, "CIT-1");

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
        verify(franjaHorariaRepository).liberar("FR-1");
    }

    @Test
    void reprogramaLaMismaCitaYLiberaraLaFranjaAnterior() {
        Cliente cliente = cliente();
        Odontologo odontologo = new Odontologo();
        FranjaHoraria anterior = franja("FR-1", AHORA.plusDays(2), odontologo);
        FranjaHoraria nueva = franja("FR-2", AHORA.plusDays(4), odontologo);
        Cita cita = cita("CIT-1", cliente, anterior);
        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(citaRepository.findById("CIT-1")).thenReturn(Optional.of(cita));
        when(franjaHorariaRepository.findById("FR-2")).thenReturn(Optional.of(nueva));
        when(franjaHorariaRepository.ocuparSiDisponible("FR-2")).thenReturn(true);
        when(citaRepository.save(cita)).thenReturn(cita);

        Cita actualizada = service.reprogramarCita(
                AUTH_USER_ID,
                new ReprogramarCitaCommand("CIT-1", "FR-2")
        );

        assertThat(actualizada).isSameAs(cita);
        assertThat(actualizada.getEstado()).isEqualTo(EstadoCita.REPROGRAMADA);
        assertThat(actualizada.getFranjaHoraria()).isSameAs(nueva);
        var orden = inOrder(franjaHorariaRepository, citaRepository);
        orden.verify(franjaHorariaRepository).ocuparSiDisponible("FR-2");
        orden.verify(franjaHorariaRepository).liberar("FR-1");
        orden.verify(citaRepository).save(cita);
    }

    private Cliente cliente() {
        return new Cliente(
                "71234567", "Ana", "Rojas", LocalDate.of(2000, 1, 1), "999111222", "Trujillo"
        );
    }

    private Tratamiento tratamiento(String id, String nombre, String precio) {
        return new Tratamiento(id, nombre, false, new BigDecimal(precio), "ACTIVO");
    }

    private FranjaHoraria franja(String id, LocalDateTime inicio, Odontologo odontologo) {
        return new FranjaHoraria(
                id,
                inicio.toLocalDate(),
                inicio.toLocalTime(),
                inicio.toLocalTime().plusHours(1),
                true,
                odontologo
        );
    }

    private Cita cita(String id, Cliente cliente, FranjaHoraria franja) {
        return new Cita(id, cliente, franja.getOdontologo(), tratamiento("TR-1", "Consulta", "80.00"), franja);
    }
}
