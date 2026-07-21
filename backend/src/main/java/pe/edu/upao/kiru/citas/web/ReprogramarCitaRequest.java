package pe.edu.upao.kiru.citas.web;

import jakarta.validation.constraints.NotBlank;

public record ReprogramarCitaRequest(@NotBlank String idFranjaNueva) {
}
