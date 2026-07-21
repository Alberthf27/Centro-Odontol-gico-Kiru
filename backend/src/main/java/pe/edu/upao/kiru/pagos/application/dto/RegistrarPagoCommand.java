package pe.edu.upao.kiru.pagos.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegistrarPagoCommand(
        @NotBlank String idPedido,
        @NotBlank String metodoPago,
        @NotNull @DecimalMin("0.01") BigDecimal monto
) {
}
