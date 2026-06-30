package community.backend.global.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile({"local", "default"})
public class ImageConfig implements WebMvcConfigurer {

  private final String baseUrl;
  private final Path imageRootPath;

  public ImageConfig(
      @Value("${app.image.root-dir:image}") String rootDir,
      @Value("${app.image.base-url:/image}") String baseUrl
  ) {
    this.baseUrl = baseUrl;
    this.imageRootPath = Path.of(System.getProperty("user.dir")).resolve(rootDir).toAbsolutePath().normalize();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String resourceHandlerPattern = normalizeBaseUrl(baseUrl) + "/**";
    String resourceLocation = imageRootPath.toUri().toString();
    registry.addResourceHandler(resourceHandlerPattern)
        .addResourceLocations(resourceLocation);
  }

  private static String normalizeBaseUrl(String url) {
    if (url == null || url.isBlank()) {
      return "/image";
    }
    if (!url.startsWith("/")) {
      return "/" + url;
    }
    return url;
  }
}
