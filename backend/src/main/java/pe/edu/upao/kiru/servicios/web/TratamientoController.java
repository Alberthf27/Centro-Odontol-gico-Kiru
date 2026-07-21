package pe.edu.upao.kiru.servicios.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upao.kiru.servicios.application.port.TratamientoRepository;

/** Catálogo activo requerido por Registrar cita médica. */
@RestController
@RequestMapping("/api/tratamientos")
public class TratamientoController {

    private final TratamientoRepository tratamientoRepository;

    public TratamientoController(TratamientoRepository tratamientoRepository) {
        this.tratamientoRepository = tratamientoRepository;
    }

    @GetMapping
    public List<TratamientoResponse> listarActivos() {
        return tratamientoRepository.findActivos().stream().map(TratamientoResponse::from).toList();
    }
}
