package org.evilcorp.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.evilcorp.validation.parser.JsonParser;
import org.evilcorp.validation.parser.MessageType;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(new File("schemas/body.json"));
        JsonParser parser = new JsonParser();
        parser.pars(jsonNode, MessageType.RQ);
    }
}