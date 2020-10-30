package org.evilcorp.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.evilcorp.app.service.JsonParserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class Controller {

    private final JsonParserService service;

    @SneakyThrows
    @PostMapping("/create")
    public JsonNode getSchemas(@RequestBody CreateSchemaReq req) {
        return service.pars(req);
    }
}
