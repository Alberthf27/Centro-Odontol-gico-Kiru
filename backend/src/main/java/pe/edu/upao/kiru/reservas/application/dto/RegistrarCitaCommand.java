package pe.edu.upao.kiru.reservas.application.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pe.edu.upao.kiru.citas.domain.TipoCita;

public record RegistrarCitaCommand(
        @NotNull TipoCita tipoCita,
        String idTratamiento,
        String idSesion,
        @NotBlank String idOdontologo,
        @NotBlank String idFranja
) {
    @AssertTrue(message = "La selección de consulta o tratamiento no es coherente.")
    public boolean isSeleccionCoherente() {
        if (tipoCita == null) {
            return false;
        }
        if (tipoCita == TipoCita.CONSULTA) {
            return estaVacio(idTratamiento) && estaVacio(idSesion);
        }
        return !estaVacio(idTratamiento);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
