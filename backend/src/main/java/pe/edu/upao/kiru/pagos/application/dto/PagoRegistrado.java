package pe.edu.upao.kiru.pagos.application.dto;

import pe.edu.upao.kiru.pagos.domain.Comprobante;
import pe.edu.upao.kiru.pagos.domain.Pago;

public record PagoRegistrado(Pago pago, Comprobante comprobante) {
}
