package org.evilcorp.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.evilcorp.app.controller.CreateSchemaReq;

import java.io.IOException;

public interface JsonParserService {
    JsonNode pars(CreateSchemaReq req) throws IOException;
}
