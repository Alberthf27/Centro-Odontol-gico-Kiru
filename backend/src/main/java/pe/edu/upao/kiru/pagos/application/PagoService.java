package pe.edu.upao.kiru.pagos.application;

import pe.edu.upao.kiru.pagos.application.dto.RegistrarPagoCommand;
import pe.edu.upao.kiru.pagos.application.dto.PagoRegistrado;

/**
 * Puerto de aplicación para la participación web del Cliente en Registrar pago.
 */
public interface PagoService {

    PagoRegistrado registrarPago(String authUserId, RegistrarPagoCommand command);
}
