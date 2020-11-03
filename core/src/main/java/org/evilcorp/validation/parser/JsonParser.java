package org.evilcorp.validation.parser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RequiredArgsConstructor
public class JsonParser implements SchemaValidationCreator {

    private static final String OBJECT = "object";
    private static final String TYPE = "type";
    private static final String STRING = "string";
    private static final String MAX_LENGTH = "maxLength";
    private static final String PROPERTIES = "properties";
    private static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    private static final String ARRAY = "array";

    private final ObjectMapper mapper;
    private final ResponseWrapperWriter responseWrapperWriter;

    public JsonNode parse(JsonNode mess, ValidationParameters parameters) throws IOException {
        var schema = mapper.createObjectNode();
        schema.put("$schema", "http://json-schema.org/draft-04/schema#");
        schema.put("title", "title");
        schema.put("description", "description");
        switch (parameters.getMessageType()) {
            case RQ:
                setType(schema, OBJECT, "null");
                parseObject(mess, schema, parameters);
                break;
            case RS_ARR:
                schema.put(TYPE, OBJECT);
                schema.put(ADDITIONAL_PROPERTIES, false);
                var properties = schema.putObject(PROPERTIES);
                properties.putObject("success").put(TYPE, "boolean");
                var bodyArr = properties.putObject("body");
                setType(bodyArr, ARRAY, "null");
                bodyArr.put(ADDITIONAL_PROPERTIES, false);
                bodyArr.put("maxItems", 1000);
                var items = bodyArr.putObject("items");
                setType(items, OBJECT, "null");
                responseWrapperWriter.write(properties);
                parseObject(mess, items, parameters);
                break;
            case RS:
                schema.put(TYPE, OBJECT);
                schema.put(ADDITIONAL_PROPERTIES, false);
                var propOne = schema.putObject(PROPERTIES);
                propOne.putObject("success").put(TYPE, "boolean");
                var bodyOne = propOne.putObject("body");
                setType(bodyOne, OBJECT, "null");
                responseWrapperWriter.write(propOne);
                parseObject(mess, bodyOne, parameters);
                break;
        }
        return schema;
    }

    public void parseToFile(JsonNode mess, ValidationParameters parameters) throws IOException {
        var schema = parse(mess, parameters);
        writeToFile("validation-schema.json", mapper.writeValueAsString(schema));
    }

    private void parseObject(JsonNode body, ObjectNode message, ValidationParameters parameters) {
        setType(message, OBJECT, "null");
        message.put(ADDITIONAL_PROPERTIES, false);
        var properties = message.putObject(PROPERTIES);
        body.fieldNames()
                .forEachRemaining(el -> {
                    var field = body.get(el);
                    var ell = properties.putObject(el);
                            if (field.isInt()) {
                                setType(ell, "integer", "null");
                                ell
                                        .put("minimum", 0)
                                        .put("maximum", parameters.getMaxInteger() == null ?
                                                Integer.MAX_VALUE : parameters.getMaxInteger());
                            } else if (field.isBoolean()) {
                                setType(ell, "boolean", "null");
                            } else if (field.isValueNode()) {
                                setType(ell, STRING, "null");
                                ell.put(MAX_LENGTH, parameters.getMaxLength() == null ? 249 : parameters.getMaxLength());
                            } else if (field.isObject()) {
                                parseObject(field, ell, parameters);
                            } else if (field.isArray()) {
                                parsArray(field, ell, parameters);
                            }
                        }
                );
    }

    private void parsArray(JsonNode field, ObjectNode ell, ValidationParameters parameters) {
        setType(ell, ARRAY, "null");
        ell.put("maxItems", 200);
        ell.put("additionalItems", false);
        var items = ell.putObject("items");
        var arr = (ArrayNode) field;
        var elements = arr.elements();
        parseObject(elements.next(), items, parameters);
    }

    private void writeToFile(String fileName, String json) throws IOException {
        var schemas = new File("schemas");
        schemas.mkdir();
        try (var writer = new FileWriter(new File(schemas, fileName))) {
            writer.write(json);
        }
    }

    private void setType(ObjectNode node, String... types) {
        var jsonNodes = node.putArray(TYPE);
        for (var type : types) {
            jsonNodes.add(type);
        }
    }
}
