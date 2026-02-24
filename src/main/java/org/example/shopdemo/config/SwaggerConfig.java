package org.example.shopdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 配置OpenAPI文档
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI文档
     * @return OpenAPI对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("购物商城API文档")
                        .version("1.0.0")
                        .description("购物商城后端接口文档")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT token，格式：Bearer {token}")));
    }
}
