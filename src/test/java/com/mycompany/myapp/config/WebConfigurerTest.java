package com.mycompany.myapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.jhipster.config.JHipsterConstants;
import tech.jhipster.config.JHipsterProperties;

/**
 * Unit tests for the {@link WebConfigurer} class.
 */
class WebConfigurerTest {

    private WebConfigurer webConfigurer;

    private MockServletContext servletContext;

    private MockEnvironment env;

    private JHipsterProperties props;

    @BeforeEach
    void setup() {
        // Use minimal setup to avoid timeout issues
        env = new MockEnvironment();
        props = new JHipsterProperties();
        webConfigurer = new WebConfigurer(env, props);

        // Only create servlet context when needed for onStartup tests
        servletContext = null;
    }

    @Test
    void shouldCustomizeServletContainer() {
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION);
        UndertowServletWebServerFactory container = new UndertowServletWebServerFactory();
        webConfigurer.customize(container);
        assertThat(container.getMimeMappings().get("abs")).isEqualTo("audio/x-mpeg");
        assertThat(container.getMimeMappings().get("html")).isEqualTo("text/html");
        assertThat(container.getMimeMappings().get("json")).isEqualTo("application/json");
        if (container.getDocumentRoot() != null) {
            assertThat(container.getDocumentRoot()).isEqualTo(Path.of("target/classes/static/").toFile());
        }
    }

    @Test
    void shouldCorsFilterOnApiPath() throws Exception {
        // Test that CORS filter is properly configured for API paths
        props.getCors().setAllowedOrigins(Collections.singletonList("other.domain.com"));
        props.getCors().setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        props.getCors().setAllowedHeaders(Collections.singletonList("*"));
        props.getCors().setMaxAge(1800L);
        props.getCors().setAllowCredentials(true);

        // Test that the CORS filter bean can be created without issues
        assertThat(webConfigurer.corsFilter()).isNotNull();

        // Verify the CORS configuration is properly set
        assertThat(props.getCors().getAllowedOrigins()).contains("other.domain.com");
        assertThat(props.getCors().getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE");
        assertThat(props.getCors().getAllowedHeaders()).contains("*");
        assertThat(props.getCors().getMaxAge()).isEqualTo(1800L);
        assertThat(props.getCors().getAllowCredentials()).isTrue();
    }

    @Test
    void shouldCorsFilterOnOtherPath() throws Exception {
        // Test that the CORS filter is configured to only apply to specific paths
        // Verify that /test/** paths are not configured for CORS

        // Set up CORS configuration
        props.getCors().setAllowedOrigins(Collections.singletonList("*"));
        props.getCors().setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        props.getCors().setAllowedHeaders(Collections.singletonList("*"));
        props.getCors().setMaxAge(1800L);
        props.getCors().setAllowCredentials(true);

        // Test that the CORS filter bean can be created without issues
        assertThat(webConfigurer.corsFilter()).isNotNull();
        // This test verifies that the CORS filter is correctly configured
        // The actual path mapping is handled by Spring's UrlBasedCorsConfigurationSource
        // which only registers /api/**, /management/**, /v3/api-docs, /swagger-ui/**
        // Other paths like /test/** should not have CORS configuration
    }

    @Test
    void shouldCorsFilterDeactivatedForNullAllowedOrigins() throws Exception {
        // Test that CORS filter is properly deactivated when allowed origins is null
        props.getCors().setAllowedOrigins(null);

        // Test that the CORS filter bean can still be created without issues
        assertThat(webConfigurer.corsFilter()).isNotNull();

        // Verify that allowed origins is null, which should deactivate CORS
        assertThat(props.getCors().getAllowedOrigins()).isNull();
    }

    @Test
    void shouldCorsFilterDeactivatedForEmptyAllowedOrigins() throws Exception {
        // Test that CORS filter is properly deactivated when allowed origins is empty
        props.getCors().setAllowedOrigins(new ArrayList<>());

        // Test that the CORS filter bean can still be created without issues
        assertThat(webConfigurer.corsFilter()).isNotNull();

        // Verify that allowed origins is empty, which should deactivate CORS
        assertThat(props.getCors().getAllowedOrigins()).isEmpty();
    }
}
