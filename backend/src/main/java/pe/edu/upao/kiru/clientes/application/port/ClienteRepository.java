package pe.edu.upao.kiru.clientes.application.port;

import java.util.Optional;
import pe.edu.upao.kiru.clientes.domain.Cliente;

public interface ClienteRepository {

    boolean existsByDni(String dni);

    Optional<Cliente> findByAuthUserId(String authUserId);

    Cliente save(Cliente cliente, String authUserId);
}
