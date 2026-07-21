package pe.edu.upao.kiru.odontologos.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface OdontologoJpaSpringRepository extends JpaRepository<OdontologoJpaEntity, Integer> {

    @Query("select odontologo from OdontologoJpaEntity odontologo "
            + "join fetch odontologo.empleado empleado "
            + "where odontologo.idOdontologo = :id")
    Optional<OdontologoJpaEntity> findConEmpleadoById(Integer id);

    @Query("select odontologo from OdontologoJpaEntity odontologo "
            + "join fetch odontologo.empleado empleado "
            + "where empleado.estado = true "
            + "order by empleado.apellidos asc, empleado.nombres asc")
    List<OdontologoJpaEntity> findActivos();
}
