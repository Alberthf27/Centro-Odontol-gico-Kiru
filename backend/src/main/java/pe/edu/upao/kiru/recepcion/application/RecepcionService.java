package pe.edu.upao.kiru.recepcion.application;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarCitaCommand;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarClienteCommand;
import pe.edu.upao.kiru.reservas.application.dto.ReprogramarCitaCommand;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import pe.edu.upao.kiru.servicios.domain.PlanTratamiento;

/**
 * Puerto de aplicación para los casos de uso del Recepcionista
 * (CU "Monitorear citas" y operaciones asociadas de recepción).
 */
public interface RecepcionService {

    Cliente buscarCliente(String termino);

    Cliente registrarClientePresencial(RegistrarClienteCommand command);

    /** Citas de una fecha ordenadas por hora; la vista por defecto del monitor. */
    List<Cita> listarCitasPorFecha(LocalDate fecha);

    List<Cita> listarCitasDeCliente(String idCliente);

    List<PlanTratamiento> listarPlanesPendientes(String idCliente);

    /** Registro presencial/telefónico de una cita a nombre del cliente atendido en recepción. */
    PedidoReserva registrarCitaPresencial(String idCliente, RegistrarCitaCommand command);

    Cita registrarAsistenciaPaciente(String idCita);

    void cancelarCita(String idCita);

    Cita reprogramarCita(ReprogramarCitaCommand command);
}
