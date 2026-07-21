package pe.edu.upao.kiru.servicios.web;

import java.math.BigDecimal;
import pe.edu.upao.kiru.servicios.domain.Tratamiento;

public record TratamientoResponse(
        String idTratamiento,
        String nombre,
        boolean multiplesSesiones,
        BigDecimal precio
) {
    public static TratamientoResponse from(Tratamiento tratamiento) {
        return new TratamientoResponse(
                tratamiento.getIdTratamiento(),
                tratamiento.getNombre(),
                tratamiento.isMultiplesSesiones(),
                tratamiento.getPrecio()
        );
    }
}
