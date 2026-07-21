package pe.edu.upao.kiru.recepcion.web;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upao.kiru.citas.web.ReprogramarCitaRequest;
import pe.edu.upao.kiru.clientes.web.ClienteResponse;
import pe.edu.upao.kiru.recepcion.application.RecepcionService;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarCitaCommand;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarClienteCommand;
import pe.edu.upao.kiru.reservas.application.dto.ReprogramarCitaCommand;
import pe.edu.upao.kiru.reservas.web.PedidoReservaResponse;
import pe.edu.upao.kiru.servicios.web.PlanTratamientoResponse;

/**
 * Caso de uso "Monitorear citas" del Recepcionista: búsqueda de cliente por DNI,
 * citas del día y acciones de recepción (nueva cita, asistencia, reprogramar, cancelar).
 */
@RestController
@RequestMapping("/api/recepcion")
public class RecepcionController {

    private final RecepcionService recepcionService;

    public RecepcionController(RecepcionService recepcionService) {
        this.recepcionService = recepcionService;
    }

    @GetMapping("/clientes/buscar")
    public ClienteResponse buscarCliente(@RequestParam String termino) {
        return ClienteResponse.from(recepcionService.buscarCliente(termino));
    }

    @PostMapping("/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse registrarCliente(@Valid @RequestBody RegistrarClienteCommand command) {
        return ClienteResponse.from(recepcionService.registrarClientePresencial(command));
    }

    @GetMapping("/citas")
    public List<CitaMonitoreoResponse> citasPorFecha(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return recepcionService.listarCitasPorFecha(fecha).stream()
                .map(CitaMonitoreoResponse::from)
                .toList();
    }

    @GetMapping("/clientes/{idCliente}/citas")
    public List<CitaMonitoreoResponse> citasDeCliente(@PathVariable String idCliente) {
        return recepcionService.listarCitasDeCliente(idCliente).stream()
                .map(CitaMonitoreoResponse::from)
                .toList();
    }

    @GetMapping("/clientes/{idCliente}/planes-pendientes")
    public List<PlanTratamientoResponse> planesPendientes(@PathVariable String idCliente) {
        return recepcionService.listarPlanesPendientes(idCliente).stream()
                .map(PlanTratamientoResponse::from)
                .toList();
    }

    @PostMapping("/clientes/{idCliente}/citas")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoReservaResponse registrarCita(
            @PathVariable String idCliente,
            @Valid @RequestBody RegistrarCitaCommand command
    ) {
        return PedidoReservaResponse.from(recepcionService.registrarCitaPresencial(idCliente, command));
    }

    @PatchMapping("/citas/{idCita}/asistencia")
    public CitaMonitoreoResponse registrarAsistencia(@PathVariable String idCita) {
        return CitaMonitoreoResponse.from(recepcionService.registrarAsistenciaPaciente(idCita));
    }

    @PatchMapping("/citas/{idCita}/cancelacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable String idCita) {
        recepcionService.cancelarCita(idCita);
    }

    @PatchMapping("/citas/{idCita}/reprogramacion")
    public CitaMonitoreoResponse reprogramar(
            @PathVariable String idCita,
            @Valid @RequestBody ReprogramarCitaRequest request
    ) {
        return CitaMonitoreoResponse.from(recepcionService.reprogramarCita(
                new ReprogramarCitaCommand(idCita, request.idFranjaNueva())
        ));
    }
}
