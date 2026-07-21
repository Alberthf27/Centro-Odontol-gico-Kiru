package pe.edu.upao.kiru.pagos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PagoJpaSpringRepository extends JpaRepository<PagoJpaEntity, Integer> {
}
