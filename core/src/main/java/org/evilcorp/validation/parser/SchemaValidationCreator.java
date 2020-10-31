package org.evilcorp.validation.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface SchemaValidationCreator {

    JsonNode pars(JsonNode node, ValidationParameters parameters) throws IOException;
}