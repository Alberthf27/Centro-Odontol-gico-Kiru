package pe.edu.upao.kiru.citas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaJpaSpringRepository extends JpaRepository<CitaJpaEntity, Integer> {
    Optional<CitaJpaEntity> findByNroCita(String nroCita);
    List<CitaJpaEntity> findByIdClienteOrderByIdCitaDesc(Integer idCliente);
    List<CitaJpaEntity> findByIdPedido(Integer idPedido);
}
