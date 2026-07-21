package pe.edu.upao.kiru.clientes.web;

import java.time.LocalDate;
import pe.edu.upao.kiru.clientes.domain.Cliente;

public record ClienteResponse(
        String idCliente,
        String dni,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String celular,
        String domicilio,
        String estado
) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getIdCliente(),
                cliente.getDni(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getFechaNacimiento(),
                cliente.getCelular(),
                cliente.getDomicilio(),
                cliente.getEstado()
        );
    }
}
