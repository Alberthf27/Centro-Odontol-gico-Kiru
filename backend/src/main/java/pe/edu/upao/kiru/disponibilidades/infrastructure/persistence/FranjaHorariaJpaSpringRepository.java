package pe.edu.upao.kiru.disponibilidades.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FranjaHorariaJpaSpringRepository extends JpaRepository<FranjaHorariaJpaEntity, Integer> {

    List<FranjaHorariaJpaEntity> findByIdOdontologoAndFechaOrderByHoraInicioAsc(Integer idOdontologo, LocalDate fecha);

    Optional<FranjaHorariaJpaEntity> findByIdCita(Integer idCita);

    @Modifying
    @Query("update FranjaHorariaJpaEntity franja set franja.disponible = false where franja.idFranja = :id and franja.disponible = true")
    int ocuparSiDisponible(@Param("id") Integer id);

    @Modifying
    @Query("update FranjaHorariaJpaEntity franja set franja.disponible = true, franja.idCita = null where franja.idFranja = :id")
    int liberar(@Param("id") Integer id);

    @Modifying
    @Query("update FranjaHorariaJpaEntity franja set franja.idCita = :idCita, franja.disponible = false where franja.idFranja = :idFranja")
    int asignarCita(@Param("idFranja") Integer idFranja, @Param("idCita") Integer idCita);
}
