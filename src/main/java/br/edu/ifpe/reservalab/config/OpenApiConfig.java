package br.edu.ifpe.reservalab.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contrato OpenAPI em /v3/api-docs e Swagger UI em /swagger-ui.html.
 * Uma cópia versionada fica em docs/mobile/openapi.json para quem não roda o backend.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI reservaLabOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ReservaLab API")
                        .version("v1")
                        .description("Reservas de laboratórios e chamados de manutenção do IFPE. "
                                + "Autentique em POST /api/auth/login e envie 'Authorization: Bearer <token>'."))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
