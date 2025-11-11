package kairos.residencia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CORS {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Aplica esta regra a todas as rotas (endpoints)
                        // Permite o domínio exato do seu frontend Vercel:
                        .allowedOrigins("https://work-up-platform.vercel.app/")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite os métodos necessários
                        .allowedHeaders("*") // Permite todos os cabeçalhos
                        .allowCredentials(true);
            }
        };
    }
}
