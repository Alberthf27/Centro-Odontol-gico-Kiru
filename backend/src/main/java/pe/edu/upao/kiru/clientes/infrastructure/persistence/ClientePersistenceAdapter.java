package pe.edu.upao.kiru.clientes.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.edu.upao.kiru.clientes.application.port.ClienteRepository;
import pe.edu.upao.kiru.clientes.domain.Cliente;

@Repository
class ClientePersistenceAdapter implements ClienteRepository {

    private final ClienteJpaSpringRepository repository;

    ClientePersistenceAdapter(ClienteJpaSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByDni(String dni) {
        return repository.existsByDni(dni);
    }

    @Override
    public Optional<Cliente> findByDni(String dni) {
        return repository.findByDni(dni).map(this::toDomain);
    }

    @Override
    public Optional<Cliente> findById(String idCliente) {
        try {
            return repository.findById(Integer.valueOf(idCliente)).map(this::toDomain);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cliente> findByAuthUserId(String authUserId) {
        try {
            return repository.findByAuthUserId(UUID.fromString(authUserId)).map(this::toDomain);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Cliente save(Cliente cliente, String authUserId) {
        ClienteJpaEntity saved = repository.save(new ClienteJpaEntity(
                cliente.getDni(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getFechaNacimiento(),
                cliente.getCelular(),
                cliente.getDomicilio(),
                UUID.fromString(authUserId)
        ));
        return toDomain(saved);
    }

    @Override
    public Cliente savePresencial(Cliente cliente) {
        ClienteJpaEntity saved = repository.save(new ClienteJpaEntity(
                cliente.getDni(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getFechaNacimiento(),
                cliente.getCelular(),
                cliente.getDomicilio(),
                null
        ));
        return toDomain(saved);
    }

    private Cliente toDomain(ClienteJpaEntity entity) {
        return Cliente.reconstituir(
                entity.getIdCliente().toString(),
                entity.getDni(),
                entity.getNombres(),
                entity.getApellidos(),
                entity.getFechaNacimiento(),
                entity.getCelular(),
                entity.getDomicilio(),
                entity.isEstado()
        );
    }
}
