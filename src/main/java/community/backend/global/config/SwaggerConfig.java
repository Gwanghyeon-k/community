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

  private static final String COOKIE_SCHEME_NAME = "AccessTokenCookieAuth";

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme accessTokenCookieScheme = new SecurityScheme()
        .name("accessToken")
        .type(SecurityScheme.Type.APIKEY)
        .in(SecurityScheme.In.COOKIE);

    return new OpenAPI()
        .info(new Info().title("Community API").version("v1"))
        .components(new Components().addSecuritySchemes(COOKIE_SCHEME_NAME, accessTokenCookieScheme))
        .addSecurityItem(new SecurityRequirement().addList(COOKIE_SCHEME_NAME));
  }
}
