package pe.edu.upao.kiru.reservas.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.pagos.domain.EstadoPago;

public class PedidoReserva {

    private String idPedido;
    private String nroPedido;
    private LocalDateTime fecha;
    private BigDecimal montoTotal;
    private EstadoPago estadoPago;
    private Cliente cliente;
    private final List<Cita> citas = new ArrayList<>();

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
        citas.add(cita);
    }

    public boolean eliminarCita(String idCita) {
        return citas.removeIf(cita -> cita.getIdCita().equals(idCita));
    }
}
