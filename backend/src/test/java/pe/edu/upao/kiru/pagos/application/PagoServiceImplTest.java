package pe.edu.upao.kiru.pagos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.clientes.application.port.ClienteRepository;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;
import pe.edu.upao.kiru.odontologos.domain.Odontologo;
import pe.edu.upao.kiru.pagos.application.dto.RegistrarPagoCommand;
import pe.edu.upao.kiru.pagos.application.dto.PagoRegistrado;
import pe.edu.upao.kiru.pagos.application.port.CodigoComprobanteGenerator;
import pe.edu.upao.kiru.pagos.application.port.ComprobanteRepository;
import pe.edu.upao.kiru.pagos.application.port.PagoRepository;
import pe.edu.upao.kiru.pagos.domain.Comprobante;
import pe.edu.upao.kiru.pagos.domain.EstadoPago;
import pe.edu.upao.kiru.pagos.domain.Pago;
import pe.edu.upao.kiru.reservas.application.port.PedidoReservaRepository;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;
import pe.edu.upao.kiru.shared.exception.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    private static final String AUTH_USER_ID = "auth-cliente-1";
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 20, 10, 0);

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private PedidoReservaRepository pedidoReservaRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private ComprobanteRepository comprobanteRepository;
    @Mock
    private CodigoComprobanteGenerator codigoComprobanteGenerator;

    private PagoServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new PagoServiceImpl(
                clienteRepository,
                pedidoReservaRepository,
                pagoRepository,
                comprobanteRepository,
                codigoComprobanteGenerator,
                clock
        );
    }

    @Test
    void registraPagoCompletoYConfirmaElPedido() {
        Cliente cliente = cliente("71234567");
        PedidoReserva pedido = pedido(cliente, "100.00");
        RegistrarPagoCommand command = new RegistrarPagoCommand("PED-1", "PASARELA_SIMULADA", new BigDecimal("100.00"));
        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(pedidoReservaRepository.findById("PED-1")).thenReturn(Optional.of(pedido));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(codigoComprobanteGenerator.siguienteBoleta()).thenReturn("BOL-001");
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoReservaRepository.save(pedido)).thenReturn(pedido);

        PagoRegistrado resultado = service.registrarPago(AUTH_USER_ID, command);
        Pago pago = resultado.pago();

        assertThat(pago.getMonto()).isEqualByComparingTo("100.00");
        assertThat(pago.getFecha()).isEqualTo(AHORA);
        assertThat(resultado.comprobante().getNroComprobante()).isEqualTo("BOL-001");
        assertThat(pedido.getEstadoPago()).isEqualTo(EstadoPago.PAGADO);
        verify(comprobanteRepository).save(any(Comprobante.class));
        verify(pedidoReservaRepository).save(pedido);
    }

    @Test
    void rechazaPagoConMontoDistintoAlPedido() {
        Cliente cliente = cliente("71234567");
        PedidoReserva pedido = pedido(cliente, "100.00");
        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(pedidoReservaRepository.findById("PED-1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.registrarPago(
                AUTH_USER_ID,
                new RegistrarPagoCommand("PED-1", "PASARELA_SIMULADA", new BigDecimal("90.00"))
        )).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("monto");

        verify(pagoRepository, never()).save(any());
        assertThat(pedido.getEstadoPago()).isEqualTo(EstadoPago.PENDIENTE);
    }

    @Test
    void rechazaPagarNuevamenteUnPedidoPagado() {
        Cliente cliente = cliente("71234567");
        PedidoReserva pedido = pedido(cliente, "100.00");
        pedido.marcarPagado();
        when(clienteRepository.findByAuthUserId(AUTH_USER_ID)).thenReturn(Optional.of(cliente));
        when(pedidoReservaRepository.findById("PED-1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.registrarPago(
                AUTH_USER_ID,
                new RegistrarPagoCommand("PED-1", "PASARELA_SIMULADA", new BigDecimal("100.00"))
        )).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("pendiente");

        verify(pagoRepository, never()).save(any());
    }

    private Cliente cliente(String dni) {
        return new Cliente(dni, "Ana", "Rojas", LocalDate.of(2000, 1, 1), "999111222", "Trujillo");
    }

    private PedidoReserva pedido(Cliente cliente, String precio) {
        Odontologo odontologo = new Odontologo();
        FranjaHoraria franja = new FranjaHoraria(
                "FR-1", AHORA.plusDays(2).toLocalDate(), LocalTime.of(10, 0), LocalTime.of(11, 0), true, odontologo
        );
        Tratamiento tratamiento = new Tratamiento(
                "TR-1", "Consulta", false, new BigDecimal(precio), "ACTIVO"
        );
        Cita cita = new Cita("CIT-1", cliente, odontologo, tratamiento, franja);
        PedidoReserva pedido = new PedidoReserva("PED-1", AHORA, cliente);
        pedido.agregarCita(cita);
        return pedido;
    }
}
