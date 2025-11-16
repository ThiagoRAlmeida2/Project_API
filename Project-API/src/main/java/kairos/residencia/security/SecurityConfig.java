package kairos.residencia.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/api/projetos/public"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/eventos").permitAll()
                        .requestMatchers(
                                "/api/projetos/*/inscrever",
                                "/api/projetos/inscricoes"
                        ).hasRole("ALUNO")
                        .requestMatchers(HttpMethod.DELETE, "/api/projetos/*/cancelar-inscricao").hasRole("ALUNO")
                        .requestMatchers(HttpMethod.POST, "/api/eventos/*/inscrever").hasRole("ALUNO")
                        .requestMatchers(HttpMethod.GET, "/api/eventos/minhas-inscricoes").hasRole("ALUNO")
                        .requestMatchers(HttpMethod.DELETE, "/api/eventos/*/cancelar").hasRole("ALUNO")
                        .requestMatchers(
                                "/api/projetos/meus",
                                "/api/projetos/criar",
                                "/api/projetos/*/encerrar",
                                "/api/usuario/dashboard/candidatos",
                                "/api/usuario/aluno/**",
                                "/api/usuario/inscricao/**",
                                "/api/eventos/criar"
                        ).hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.DELETE, "/api/eventos/*").hasRole("EMPRESA")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}