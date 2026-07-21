package pe.edu.upao.kiru.reservas.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ReprogramarCitaCommand(
        @NotBlank String idCita,
        @NotBlank String idFranjaNueva
) {
}
