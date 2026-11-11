package br.senai.backendassitencia.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupMessage implements CommandLineRunner {

    @Value("${server.port:8080}")
    private String port;

    @Override
    public void run(String... args) {
        log.info("event=application_started app=saep-veterinaria url=http://localhost:{}", port);

        System.out.println();
        System.out.println("🚀 API Assistência Técnica iniciada com sucesso!");
        System.out.println("🔗 http://localhost:" + port);
        System.out.println();

    }
}
