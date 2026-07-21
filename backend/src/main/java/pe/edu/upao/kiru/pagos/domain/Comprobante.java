package pe.edu.upao.kiru.pagos.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Comprobante {

    private String idComprobante;
    private String nroComprobante;
    private LocalDateTime fechaEmision;
    private BigDecimal montoTotal;
    private TipoComprobante tipoComprobante;
    private Pago pago;

    protected Comprobante() {
    }

    public Comprobante(
            String nroComprobante,
            TipoComprobante tipoComprobante,
            BigDecimal montoTotal,
            LocalDateTime fechaEmision,
            Pago pago
    ) {
        this.nroComprobante = Objects.requireNonNull(nroComprobante);
        this.tipoComprobante = Objects.requireNonNull(tipoComprobante);
        this.montoTotal = Objects.requireNonNull(montoTotal);
        this.fechaEmision = Objects.requireNonNull(fechaEmision);
        this.pago = Objects.requireNonNull(pago);
    }

    public static Comprobante reconstituir(
            String idComprobante,
            String nroComprobante,
            TipoComprobante tipoComprobante,
            BigDecimal montoTotal,
            LocalDateTime fechaEmision,
            Pago pago
    ) {
        Comprobante comprobante = new Comprobante();
        comprobante.idComprobante = idComprobante;
        comprobante.nroComprobante = nroComprobante;
        comprobante.tipoComprobante = tipoComprobante;
        comprobante.montoTotal = montoTotal;
        comprobante.fechaEmision = fechaEmision;
        comprobante.pago = pago;
        return comprobante;
    }

    public String getIdComprobante() {
        return idComprobante;
    }

    public String getNroComprobante() {
        return nroComprobante;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public TipoComprobante getTipoComprobante() {
        return tipoComprobante;
    }

    public Pago getPago() {
        return pago;
    }
}
