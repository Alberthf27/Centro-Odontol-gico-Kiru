package pe.edu.upao.kiru.citas.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaJpaSpringRepository extends JpaRepository<CitaJpaEntity, Integer> {
    Optional<CitaJpaEntity> findByNroCita(String nroCita);
    List<CitaJpaEntity> findByIdClienteOrderByIdCitaDesc(Integer idCliente);
    List<CitaJpaEntity> findByIdPedido(Integer idPedido);
    List<CitaJpaEntity> findByFechaProgramadaOrderByHoraInicio(LocalDate fecha);
    List<CitaJpaEntity> findAllByOrderByFechaProgramadaAscHoraInicioAsc();
}
