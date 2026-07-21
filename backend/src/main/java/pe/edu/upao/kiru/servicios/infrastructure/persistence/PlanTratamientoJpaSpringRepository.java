package pe.edu.upao.kiru.servicios.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upao.kiru.servicios.domain.EstadoPlanTratamiento;

interface PlanTratamientoJpaSpringRepository extends JpaRepository<PlanTratamientoJpaEntity, Integer> {

    @Query("""
            select distinct p
            from PlanTratamientoJpaEntity p
            join fetch p.tratamiento
            where p.idCliente = :idCliente
              and p.estado = :estado
              and exists (
                  select s.idSesion
                  from SesionJpaEntity s
                  where s.idPlan = p.idPlan and s.idCita is null
              )
            order by p.fechaInicio desc
            """)
    List<PlanTratamientoJpaEntity> findConSesionesPendientes(
            @Param("idCliente") Integer idCliente,
            @Param("estado") EstadoPlanTratamiento estado
    );
}
