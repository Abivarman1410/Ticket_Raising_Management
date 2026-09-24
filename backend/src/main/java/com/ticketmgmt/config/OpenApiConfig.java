package com.ticketmgmt.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Organization Ticket Management System API")
                        .description("""
                                Backend REST API for the Organization Ticket Management System.
                                
                                Roles:
                                - EMPLOYEE: Create and track own tickets, confirm/reject resolutions
                                - ADMIN: View assigned tickets, submit resolutions, add comments
                                - MANAGER: Full organization visibility, admin management, dashboard
                                
                                Authentication: Use POST /api/auth/login to get a JWT token,
                                then click 'Authorize' and paste: Bearer <token>
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("System Manager").email("manager@company.com")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
