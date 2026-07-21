package pe.edu.upao.kiru.clientes.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ClienteJpaSpringRepository extends JpaRepository<ClienteJpaEntity, Integer> {
    boolean existsByDni(String dni);
    Optional<ClienteJpaEntity> findByDni(String dni);
    Optional<ClienteJpaEntity> findByAuthUserId(UUID authUserId);
}
