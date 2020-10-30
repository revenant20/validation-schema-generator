package org.evilcorp.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.evilcorp.validation.parser.MessageType;

@Value
@Builder
@Jacksonized
public class CreateSchemaReq {
    JsonNode mess;
    MessageType messageType;
}
