package org.evilcorp.validation.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.io.File;

public class ResponseWrapperWriterImpl implements ResponseWrapperWriter {

    @SneakyThrows
    @Override
    public void write(ObjectNode response) {
        var classLoader = this.getClass().getClassLoader();
        var file = new File(classLoader.getResource("responseTemplate.json").getFile());
        var jsonNode = new ObjectMapper().readTree(file);
        response.set("messages", jsonNode.get("messages"));
        response.set("error", jsonNode.get("error"));
    }
}
