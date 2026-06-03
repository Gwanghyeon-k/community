package community.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  private static final String BEARER_SCHEME_NAME = "BearerAuth";

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme bearerScheme = new SecurityScheme()
        .name("Authorization")
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .in(SecurityScheme.In.HEADER);

    return new OpenAPI()
        .info(new Info().title("Community API").version("v1"))
        .components(new Components().addSecuritySchemes(BEARER_SCHEME_NAME, bearerScheme))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));
  }
}
