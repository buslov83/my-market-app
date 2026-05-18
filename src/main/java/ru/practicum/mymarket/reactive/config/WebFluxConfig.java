package ru.practicum.mymarket.reactive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.nio.file.Path;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    private final String imagesLocation;

    public WebFluxConfig(@Value("${app.catalog.images-path}") String imagesPath) {
        this.imagesLocation = Path.of(imagesPath).toAbsolutePath().toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations(imagesLocation);
    }
}
