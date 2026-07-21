package pe.edu.upao.kiru.servicios.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface TratamientoJpaSpringRepository extends JpaRepository<TratamientoJpaEntity, Integer> {

    List<TratamientoJpaEntity> findByEstadoTrueOrderByNombreAsc();
}
