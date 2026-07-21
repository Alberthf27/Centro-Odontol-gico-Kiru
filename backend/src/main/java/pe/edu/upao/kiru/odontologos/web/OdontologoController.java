package pe.edu.upao.kiru.odontologos.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upao.kiru.odontologos.application.port.OdontologoRepository;

/** Catálogo necesario para que el cliente seleccione una franja de reserva. */
@RestController
@RequestMapping("/api/odontologos")
public class OdontologoController {

    private final OdontologoRepository odontologoRepository;

    public OdontologoController(OdontologoRepository odontologoRepository) {
        this.odontologoRepository = odontologoRepository;
    }

    @GetMapping
    public List<OdontologoResponse> listarActivos() {
        return odontologoRepository.findActivos().stream().map(OdontologoResponse::from).toList();
    }
}
