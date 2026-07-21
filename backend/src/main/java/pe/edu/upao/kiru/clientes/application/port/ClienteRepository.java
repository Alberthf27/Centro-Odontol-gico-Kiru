package pe.edu.upao.kiru.clientes.application.port;

import java.util.Optional;
import pe.edu.upao.kiru.clientes.domain.Cliente;

public interface ClienteRepository {

    boolean existsByDni(String dni);

    Optional<Cliente> findByDni(String dni);

    Optional<Cliente> findById(String idCliente);

    Optional<Cliente> findByAuthUserId(String authUserId);

    Cliente save(Cliente cliente, String authUserId);

    /**
     * Registro presencial realizado por la recepcionista: el cliente aún no
     * tiene cuenta web, por lo que se persiste sin auth_user_id.
     */
    Cliente savePresencial(Cliente cliente);
}
