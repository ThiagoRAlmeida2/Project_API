package kairos.residencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "kairos.residencia.repository")
@EntityScan(basePackages = "kairos.residencia.model")
public class SrcApplication {
    public static void main(String[] args) {
        SpringApplication.run(SrcApplication.class, args);
    }
}
