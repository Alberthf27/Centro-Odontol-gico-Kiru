package pe.edu.upao.kiru.servicios.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upao.kiru.servicios.domain.EstadoPlanTratamiento;

public interface SesionJpaSpringRepository extends JpaRepository<SesionJpaEntity, Integer> {

    List<SesionJpaEntity> findByIdPlanAndIdCitaIsNullOrderByOrdenAsc(Integer idPlan);

    Optional<SesionJpaEntity> findByIdCita(Integer idCita);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from SesionJpaEntity s, PlanTratamientoJpaEntity p
            where s.idSesion = :idSesion
              and s.idPlan = p.idPlan
              and p.idCliente = :idCliente
              and p.estado = :estado
              and s.idCita is null
            """)
    Optional<SesionJpaEntity> findPendienteParaClienteConBloqueo(
            @Param("idSesion") Integer idSesion,
            @Param("idCliente") Integer idCliente,
            @Param("estado") EstadoPlanTratamiento estado
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update SesionJpaEntity s set s.idCita = :idCita where s.idSesion = :idSesion and s.idCita is null")
    int asociarCita(@Param("idSesion") Integer idSesion, @Param("idCita") Integer idCita);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update public.sesion_plan
            set id_cita = null
            where id_cita = (
                select id_cita from public.cita where nro_cita = :nroCita
            )
            """, nativeQuery = true)
    int liberarPorNroCita(@Param("nroCita") String nroCita);
}
