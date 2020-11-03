package org.evilcorp.validation.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ResponseWrapperWriter {

    void write(ObjectNode response);
}
