package org.evilcorp.validation.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface SchemaValidationCreator {

    JsonNode parse(JsonNode node, ValidationParameters parameters) throws IOException;
}
