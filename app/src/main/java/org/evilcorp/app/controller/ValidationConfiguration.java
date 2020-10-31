package org.evilcorp.app.controller;

import lombok.Builder;
import lombok.Value;
import org.evilcorp.validation.parser.MessageType;
import org.evilcorp.validation.parser.ValidationParameters;
import org.springframework.lang.NonNull;

@Value
@Builder
public class ValidationConfiguration implements ValidationParameters {
    @NonNull
    MessageType messageType;
    Integer maxLength;
    Integer maxInteger;
}
