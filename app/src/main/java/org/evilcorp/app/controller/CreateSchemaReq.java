package org.evilcorp.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CreateSchemaReq {
    JsonNode mess;
    ValidationConfiguration configuration;
}
