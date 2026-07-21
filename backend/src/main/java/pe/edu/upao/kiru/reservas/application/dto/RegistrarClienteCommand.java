package pe.edu.upao.kiru.reservas.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegistrarClienteCommand(
        @NotBlank @Size(max = 20) String dni,
        @NotBlank @Size(max = 80) String nombres,
        @NotBlank @Size(max = 80) String apellidos,
        @Past LocalDate fechaNacimiento,
        @Size(max = 30) String celular,
        @Size(max = 180) String domicilio
) {
}
