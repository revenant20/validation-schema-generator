package org.evilcorp.app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.evilcorp.app.controller.CreateSchemaReq;
import org.evilcorp.app.service.JsonParserService;
import org.evilcorp.validation.parser.SchemaValidationCreator;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class JsonParserServiceImpl implements JsonParserService {

    private final SchemaValidationCreator parser;

    @Override
    public JsonNode pars(CreateSchemaReq rq) throws IOException {
        return parser.pars(rq.getMess(), rq.getConfiguration());
    }
}
