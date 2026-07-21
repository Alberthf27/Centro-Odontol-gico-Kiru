package pe.edu.upao.kiru.clientes.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upao.kiru.reservas.application.ReservaService;
import pe.edu.upao.kiru.reservas.application.dto.RegistrarClienteCommand;
import pe.edu.upao.kiru.shared.security.AuthenticatedClientProvider;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ReservaService reservaService;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    public ClienteController(
            ReservaService reservaService,
            AuthenticatedClientProvider authenticatedClientProvider
    ) {
        this.reservaService = reservaService;
        this.authenticatedClientProvider = authenticatedClientProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse registrar(@Valid @RequestBody RegistrarClienteCommand command) {
        return ClienteResponse.from(reservaService.registrarCliente(
                authenticatedClientProvider.authUserId(),
                command
        ));
    }

    @GetMapping("/me")
    public ClienteResponse obtenerActual() {
        return ClienteResponse.from(reservaService.obtenerClienteActual(
                authenticatedClientProvider.authUserId()
        ));
    }
}
