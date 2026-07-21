package pe.edu.upao.kiru.reservas.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upao.kiru.citas.web.CitaResponse;
import pe.edu.upao.kiru.citas.web.ReprogramarCitaRequest;
import pe.edu.upao.kiru.reservas.application.ReservaService;
import pe.edu.upao.kiru.reservas.application.dto.CrearPedidoReservaCommand;
import pe.edu.upao.kiru.reservas.application.dto.ReprogramarCitaCommand;
import pe.edu.upao.kiru.shared.security.AuthenticatedClientProvider;

@RestController
@RequestMapping("/api")
public class ReservaController {

    private final ReservaService reservaService;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    public ReservaController(
            ReservaService reservaService,
            AuthenticatedClientProvider authenticatedClientProvider
    ) {
        this.reservaService = reservaService;
        this.authenticatedClientProvider = authenticatedClientProvider;
    }

    @PostMapping("/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoReservaResponse registrar(@Valid @RequestBody CrearPedidoReservaCommand command) {
        return PedidoReservaResponse.from(reservaService.registrarPedido(
                authenticatedClientProvider.authUserId(),
                command
        ));
    }

    @GetMapping("/reservas/configuracion")
    public ReservaConfiguracionResponse configuracion() {
        return new ReservaConfiguracionResponse(reservaService.obtenerPrecioConsulta());
    }

    @GetMapping("/citas")
    public List<CitaResponse> listarCitas() {
        return reservaService.listarCitasPropias(authenticatedClientProvider.authUserId()).stream()
                .map(CitaResponse::from)
                .toList();
    }

    @PatchMapping("/citas/{idCita}/cancelacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable String idCita) {
        reservaService.cancelarCita(authenticatedClientProvider.authUserId(), idCita);
    }

    @PatchMapping("/citas/{idCita}/reprogramacion")
    public CitaResponse reprogramar(
            @PathVariable String idCita,
            @Valid @RequestBody ReprogramarCitaRequest request
    ) {
        return CitaResponse.from(reservaService.reprogramarCita(
                authenticatedClientProvider.authUserId(),
                new ReprogramarCitaCommand(idCita, request.idFranjaNueva())
        ));
    }
}
