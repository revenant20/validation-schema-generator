package org.evilcorp.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.evilcorp.validation.parser.JsonParser;
import org.evilcorp.validation.parser.SchemaValidationCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    @Bean
    public SchemaValidationCreator getSchemaValidationCreator(ObjectMapper objectMapper) {
        return new JsonParser(objectMapper);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
