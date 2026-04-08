package com.cinema.booking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// API-DOCS URL: http://localhost:8080/swagger-ui/index.htm
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI cinemaBookingOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("Cổng Thanh Toán & Quản Trị Rạp Phim (REST API)")
                        .description("Bảng điểu khiển tự động sinh bằng Swagger UI. Dùng cái này Test 5 luồng API cho dễ hơn cái file Text thuần túy: \n- Catalog Quản Trị Phim\n- Upload Cloudinary\n- Core Đặt Vé & Thanh Toán")
                        .version("v1.0.0")
                        .contact(new Contact().name("Đội ngũ Kỹ Thuật").email("admin@vovantu.com"))
                        .license(new License().name("Độc quyền nội bộ").url("http://localhost:8080")));
    }
}
