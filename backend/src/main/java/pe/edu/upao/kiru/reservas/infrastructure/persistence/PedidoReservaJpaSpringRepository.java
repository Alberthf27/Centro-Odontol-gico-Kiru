package pe.edu.upao.kiru.reservas.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface PedidoReservaJpaSpringRepository extends JpaRepository<PedidoReservaJpaEntity, Integer> {
    Optional<PedidoReservaJpaEntity> findByNroPedido(String nroPedido);
}
