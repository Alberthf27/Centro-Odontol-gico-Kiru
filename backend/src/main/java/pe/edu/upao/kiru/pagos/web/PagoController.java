package pe.edu.upao.kiru.pagos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upao.kiru.pagos.application.PagoService;
import pe.edu.upao.kiru.pagos.application.dto.RegistrarPagoCommand;
import pe.edu.upao.kiru.shared.security.AuthenticatedClientProvider;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    public PagoController(
            PagoService pagoService,
            AuthenticatedClientProvider authenticatedClientProvider
    ) {
        this.pagoService = pagoService;
        this.authenticatedClientProvider = authenticatedClientProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse registrar(@Valid @RequestBody RegistrarPagoCommand command) {
        return PagoResponse.from(pagoService.registrarPago(
                authenticatedClientProvider.authUserId(),
                command
        ));
    }
}
