package org.evilcorp.app;

import org.evilcorp.validation.parser.JsonParser;
import org.evilcorp.validation.parser.SchemaValidationCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    @Bean
    public SchemaValidationCreator get() {
        return new JsonParser();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
