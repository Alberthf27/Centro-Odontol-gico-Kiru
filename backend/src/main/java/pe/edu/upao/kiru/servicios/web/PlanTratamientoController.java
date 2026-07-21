package pe.edu.upao.kiru.servicios.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upao.kiru.reservas.application.ReservaService;
import pe.edu.upao.kiru.shared.security.AuthenticatedClientProvider;

@RestController
@RequestMapping("/api/planes-tratamiento")
public class PlanTratamientoController {

    private final ReservaService reservaService;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    public PlanTratamientoController(
            ReservaService reservaService,
            AuthenticatedClientProvider authenticatedClientProvider
    ) {
        this.reservaService = reservaService;
        this.authenticatedClientProvider = authenticatedClientProvider;
    }

    @GetMapping("/pendientes")
    public List<PlanTratamientoResponse> listarPendientes() {
        return reservaService.listarPlanesPendientes(authenticatedClientProvider.authUserId())
                .stream()
                .map(PlanTratamientoResponse::from)
                .toList();
    }
}
