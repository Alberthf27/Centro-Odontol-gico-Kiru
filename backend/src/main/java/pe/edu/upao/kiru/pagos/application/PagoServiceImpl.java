package pe.edu.upao.kiru.pagos.application;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upao.kiru.clientes.application.port.ClienteRepository;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.pagos.application.dto.RegistrarPagoCommand;
import pe.edu.upao.kiru.pagos.application.dto.PagoRegistrado;
import pe.edu.upao.kiru.pagos.application.port.CodigoComprobanteGenerator;
import pe.edu.upao.kiru.pagos.application.port.ComprobanteRepository;
import pe.edu.upao.kiru.pagos.application.port.PagoRepository;
import pe.edu.upao.kiru.pagos.domain.Comprobante;
import pe.edu.upao.kiru.pagos.domain.Pago;
import pe.edu.upao.kiru.pagos.domain.TipoComprobante;
import pe.edu.upao.kiru.reservas.application.port.PedidoReservaRepository;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import pe.edu.upao.kiru.shared.exception.BusinessRuleException;
import pe.edu.upao.kiru.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private final ClienteRepository clienteRepository;
    private final PedidoReservaRepository pedidoReservaRepository;
    private final PagoRepository pagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final CodigoComprobanteGenerator codigoComprobanteGenerator;
    private final Clock clock;

    public PagoServiceImpl(
            ClienteRepository clienteRepository,
            PedidoReservaRepository pedidoReservaRepository,
            PagoRepository pagoRepository,
            ComprobanteRepository comprobanteRepository,
            CodigoComprobanteGenerator codigoComprobanteGenerator,
            Clock clock
    ) {
        this.clienteRepository = clienteRepository;
        this.pedidoReservaRepository = pedidoReservaRepository;
        this.pagoRepository = pagoRepository;
        this.comprobanteRepository = comprobanteRepository;
        this.codigoComprobanteGenerator = codigoComprobanteGenerator;
        this.clock = clock;
    }

    @Override
    public PagoRegistrado registrarPago(String authUserId, RegistrarPagoCommand command) {
        Cliente cliente = clienteRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cliente autenticado."));
        PedidoReserva pedido = pedidoReservaRepository.findById(command.idPedido())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el pedido de reserva."));

        if (!pedido.perteneceA(cliente)) {
            throw new ResourceNotFoundException("No se encontró el pedido del cliente autenticado.");
        }
        if (!pedido.estaPendienteDePago()) {
            throw new BusinessRuleException("El pedido ya no está pendiente de pago.");
        }
        if (pedido.getMontoTotal().compareTo(command.monto()) != 0) {
            throw new BusinessRuleException("El monto del pago no coincide con el total del pedido.");
        }

        LocalDateTime fechaPago = LocalDateTime.now(clock);
        Pago pago = pagoRepository.save(new Pago(
                pedido,
                command.metodoPago(),
                command.monto(),
                fechaPago
        ));
        Comprobante comprobante = comprobanteRepository.save(new Comprobante(
                codigoComprobanteGenerator.siguienteBoleta(),
                TipoComprobante.BOLETA,
                pago.getMonto(),
                fechaPago,
                pago
        ));
        pedido.marcarPagado();
        pedidoReservaRepository.save(pedido);
        return new PagoRegistrado(pago, comprobante);
    }
}
