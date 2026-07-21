package pe.edu.upao.kiru.reservas.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.clientes.domain.Cliente;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;
import pe.edu.upao.kiru.reservas.application.dto.CrearPedidoReservaCommand;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarClienteCommand;
import pe.edu.upao.kiru.reservas.application.dto.ReprogramarCitaCommand;
import pe.edu.upao.kiru.reservas.domain.PedidoReserva;
import pe.edu.upao.kiru.servicios.domain.PlanTratamiento;

/**
 * Puerto de aplicación para los casos de uso del Cliente.
 * La implementación concreta persiste mediante los adaptadores JPA del módulo.
 */
public interface ReservaService {

    Cliente registrarCliente(String authUserId, RegistrarClienteCommand command);

    Cliente obtenerClienteActual(String authUserId);

    List<FranjaHoraria> consultarDisponibilidad(String idOdontologo, LocalDate fecha);

    List<PlanTratamiento> listarPlanesPendientes(String authUserId);

    BigDecimal obtenerPrecioConsulta();

    PedidoReserva registrarPedido(String authUserId, CrearPedidoReservaCommand command);

    List<Cita> listarCitasPropias(String authUserId);

    void cancelarCita(String authUserId, String idCita);

    Cita reprogramarCita(String authUserId, ReprogramarCitaCommand command);
}
