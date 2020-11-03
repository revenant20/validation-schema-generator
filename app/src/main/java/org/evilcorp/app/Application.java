package org.evilcorp.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.evilcorp.validation.parser.JsonParser;
import org.evilcorp.validation.parser.ResponseWrapperWriter;
import org.evilcorp.validation.parser.ResponseWrapperWriterImpl;
import org.evilcorp.validation.parser.SchemaValidationCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    @Bean
    public ResponseWrapperWriter getResponseWrapperWriter() {
        return new ResponseWrapperWriterImpl();
    }

    @Bean
    public SchemaValidationCreator getSchemaValidationCreator(ObjectMapper objectMapper, ResponseWrapperWriter responseWrapperWriter) {
        return new JsonParser(objectMapper, responseWrapperWriter);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
