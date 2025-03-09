package com.flashdash.activity.config;

import com.flashdash.activity.FlashDashActivityApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = FlashDashActivityApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CorsConfigTest {

    @Autowired
    private CorsConfig corsConfig;

    @Test
    void shouldProvideCorrectCorsConfigurationSource() {
        // Act
        UrlBasedCorsConfigurationSource source = corsConfig.corsConfigurationSource();

        // Assert
        assertThat(source).isNotNull();
        CorsConfiguration config = source.getCorsConfigurations().get("/**");
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost:8080");
        assertThat(config.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(config.getAllowedHeaders()).containsExactly("*");
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    void shouldProvideCorsFilter() {
        // Act
        CorsFilter corsFilter = corsConfig.corsFilter();

        // Assert
        assertThat(corsFilter).isNotNull();
    }
}
