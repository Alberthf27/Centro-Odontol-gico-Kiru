package pe.edu.upao.kiru.clientes.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ClienteJpaSpringRepository extends JpaRepository<ClienteJpaEntity, Integer> {
    boolean existsByDni(String dni);
    Optional<ClienteJpaEntity> findByDni(String dni);

    @Query("""
            select cliente
            from ClienteJpaEntity cliente
            where lower(concat(cliente.nombres, ' ', cliente.apellidos))
                  like lower(concat('%', :termino, '%'))
            order by cliente.idCliente
            """)
    Optional<ClienteJpaEntity> findFirstByNombreCompleto(@Param("termino") String termino);

    Optional<ClienteJpaEntity> findByAuthUserId(UUID authUserId);
}
