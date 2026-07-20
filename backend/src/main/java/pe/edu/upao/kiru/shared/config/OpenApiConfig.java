package pe.edu.upao.kiru.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI kiruOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Kiru - API de reservas")
                .version("v1")
                .description("API del monolito modular del Centro Odontologico Kiru."));
    }
}
