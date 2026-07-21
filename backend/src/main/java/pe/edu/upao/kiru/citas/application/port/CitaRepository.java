package pe.edu.upao.kiru.citas.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.clientes.domain.Cliente;

public interface CitaRepository {

    Cita save(Cita cita);

    Optional<Cita> findById(String idCita);

    List<Cita> findByCliente(Cliente cliente);

    List<Cita> findByFecha(LocalDate fecha);

    List<Cita> findTodas();
}
