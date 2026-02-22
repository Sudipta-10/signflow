package com.example.DocsSignatureAppBE.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration for Docs Signature App
 *
 * This configuration class sets up the Swagger UI documentation
 * with proper API information, security schemes, and server details.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the custom OpenAPI specification
     *
     * @return OpenAPI bean with custom configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API Information Section
                .info(new Info()
                        .title("Docs Signature App API")
                        .description("API documentation for Document Signature Application\n\n" +
                                "This API provides authentication and document management capabilities.\n\n" +
                                "Features:\n" +
                                "- User registration and login with JWT authentication\n" +
                                "- Secure file upload for PDF documents\n" +
                                "- Document metadata storage in database\n" +
                                "- Role-based access control")
                        .version("1.0.0")
                        // Contact Information
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@docsignature.com")
                                .url("https://docsignature.com"))
                        // License Information
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))

                // Server Configuration
                .addServersItem(new Server()
                        .url("http://localhost:8084")
                        .description("Development Server"))

                // Security Components
                .components(new Components()
                        // JWT Bearer Token Security Scheme
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Token for API Authentication\n\n" +
                                                "Example: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")))

                // Apply security scheme globally
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
