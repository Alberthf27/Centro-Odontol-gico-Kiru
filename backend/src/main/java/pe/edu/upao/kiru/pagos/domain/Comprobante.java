package pe.edu.upao.kiru.pagos.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.empleados.domain.Empleado;

public class Comprobante {

    private String idComprobante;
    private String nroComprobante;
    private LocalDateTime fecha;
    private BigDecimal montoTotal;
    private String tipoPago;
    private String estado;
    private Cliente cliente;
    private Empleado empleado;

    public String getIdComprobante() {
        return idComprobante;
    }

    public String getNroComprobante() {
        return nroComprobante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public String getEstado() {
        return estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }
}
