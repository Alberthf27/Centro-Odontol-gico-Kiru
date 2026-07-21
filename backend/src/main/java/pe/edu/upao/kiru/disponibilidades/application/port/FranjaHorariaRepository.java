package pe.edu.upao.kiru.disponibilidades.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;

public interface FranjaHorariaRepository {

    List<FranjaHoraria> findPorOdontologoYFecha(String idOdontologo, LocalDate fecha);

    Optional<FranjaHoraria> findById(String idFranja);

    Optional<FranjaHoraria> findByCitaId(String idCita);

    boolean ocuparSiDisponible(String idFranja);

    void liberar(String idFranja);
}
