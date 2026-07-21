package pe.edu.upao.kiru.disponibilidades.web;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upao.kiru.reservas.application.ReservaService;

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadController {

    private final ReservaService reservaService;

    public DisponibilidadController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<FranjaHorariaResponse> listar(
            @RequestParam String idOdontologo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return reservaService.consultarDisponibilidad(idOdontologo, fecha).stream()
                .map(FranjaHorariaResponse::from)
                .toList();
    }
}
