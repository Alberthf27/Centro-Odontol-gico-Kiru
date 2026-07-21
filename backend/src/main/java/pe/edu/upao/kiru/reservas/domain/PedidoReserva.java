package pe.edu.upao.kiru.reservas.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.pagos.domain.EstadoPago;
import java.util.Objects;

public class PedidoReserva {

    private String idPedido;
    private String nroPedido;
    private LocalDateTime fecha;
    private BigDecimal montoTotal;
    private EstadoPago estadoPago;
    private Cliente cliente;
    private final List<Cita> citas = new ArrayList<>();

    protected PedidoReserva() {
    }

    public PedidoReserva(String nroPedido, LocalDateTime fecha, Cliente cliente) {
        this.nroPedido = Objects.requireNonNull(nroPedido);
        this.fecha = Objects.requireNonNull(fecha);
        this.cliente = Objects.requireNonNull(cliente);
        this.montoTotal = BigDecimal.ZERO;
        this.estadoPago = EstadoPago.PENDIENTE;
    }

    public static PedidoReserva reconstituir(
            String idPedido,
            String nroPedido,
            LocalDateTime fecha,
            BigDecimal montoTotal,
            EstadoPago estadoPago,
            Cliente cliente,
            List<Cita> citas
    ) {
        PedidoReserva pedido = new PedidoReserva();
        pedido.idPedido = idPedido;
        pedido.nroPedido = nroPedido;
        pedido.fecha = fecha;
        pedido.montoTotal = montoTotal;
        pedido.estadoPago = estadoPago;
        pedido.cliente = cliente;
        pedido.citas.addAll(citas);
        return pedido;
    }

    public String getIdPedido() {
        return idPedido;
    }

    public String getNroPedido() {
        return nroPedido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(citas);
    }

    public void agregarCita(Cita cita) {
        agregarCita(cita, cita.getTratamiento() == null ? BigDecimal.ZERO : cita.getTratamiento().getPrecio());
    }

    public void agregarCita(Cita cita, BigDecimal monto) {
        citas.add(cita);
        if (monto != null) {
            montoTotal = montoTotal.add(monto);
        }
    }

    public boolean eliminarCita(String idCita) {
        return citas.removeIf(cita -> cita.getIdCita().equals(idCita));
    }

    public boolean perteneceA(Cliente cliente) {
        return this.cliente == cliente
                || (this.cliente.getIdCliente() != null
                && Objects.equals(this.cliente.getIdCliente(), cliente.getIdCliente()));
    }

    public boolean estaPendienteDePago() {
        return estadoPago == EstadoPago.PENDIENTE;
    }

    public void marcarPagado() {
        estadoPago = EstadoPago.PAGADO;
    }

    public void cancelar() {
        estadoPago = EstadoPago.CANCELADO;
    }
}
