package org.example.validation.parser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class JsonParser {

    public static final String OBJECT = "object";
    public static final String TYPE = "type";
    public static final String STRING = "string";
    public static final String MAX_LENGTH = "maxLength";
    public static final String PROPERTIES = "properties";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String ARRAY = "array";


    public void pars(JsonNode mess, MessageType type) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode schema = mapper.createObjectNode();
        schema.put("$schema", "http://json-schema.org/draft-04/schema#");
        schema.put("title", "title");
        schema.put("description", "description");

        switch (type) {
            case RQ:
                setType(schema, OBJECT, "null");
                parsObject(mess, schema);
                break;
            case RS_ARR:
                schema.put(TYPE, OBJECT);
                schema.put(ADDITIONAL_PROPERTIES, false);
                ObjectNode properties = schema.putObject(PROPERTIES);
                properties.putObject("success").put(TYPE, "boolean");
                ObjectNode bodyArr = properties.putObject("body");
                setType(bodyArr, ARRAY, "null");
                bodyArr.put(ADDITIONAL_PROPERTIES, false);
                bodyArr.put("maxItems", 1000);
                ObjectNode items = bodyArr.putObject("items");
                setType(items, OBJECT, "null");
                putErrorAndMessegesForResponse(properties);
                parsObject(mess, items);
                break;
            case RS:
                schema.put(TYPE, OBJECT);
                schema.put(ADDITIONAL_PROPERTIES, false);
                ObjectNode propOne = schema.putObject(PROPERTIES);
                propOne.putObject("success").put(TYPE, "boolean");
                ObjectNode bodyOne = propOne.putObject("body");
                setType(bodyOne, OBJECT, "null");
                putErrorAndMessegesForResponse(propOne);
                parsObject(mess, bodyOne);
                break;
        }
        writeToFile("validation-schema.json", mapper.writeValueAsString(schema));

    }

    private void parsObject(JsonNode body, ObjectNode message) {
        setType(message, OBJECT, "null");
        message.put(ADDITIONAL_PROPERTIES, false);
        ObjectNode properties = message.putObject(PROPERTIES);
        body.fieldNames()
                .forEachRemaining(el -> {
                            JsonNode field = body.get(el);
                            ObjectNode ell = properties.putObject(el);
                            if (field.isInt()) {
                                setType(ell, "integer", "null");
                                ell.put("minimum", 0).put("maximum", Integer.MAX_VALUE);
                            } else if (field.isBoolean()) {
                                setType(ell, "boolean", "null");
                            } else if (field.isValueNode()) {
                                setType(ell, STRING, "null");
                                ell.put(MAX_LENGTH, 249);
                            } else if (field.isObject()) {
                                parsObject(field, ell);
                            } else if (field.isArray()) {
                                parsArray(field, ell);
                            }
                        }
                );
    }

    private void putErrorAndMessegesForResponse(ObjectNode properties) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("responseTemplate.json").getFile());
        JsonNode jsonNode = new ObjectMapper().readTree(file);
        properties.set("messages", jsonNode.get("messages"));
        properties.set("error", jsonNode.get("error"));
    }

    private void parsArray(JsonNode field, ObjectNode ell) {
        setType(ell, ARRAY, "null");
        ell.put("maxItems", 200);
        ell.put("additionalItems", false);
        ObjectNode items = ell.putObject("items");
        ArrayNode arr = (ArrayNode) field;
        Iterator<JsonNode> elements = arr.elements();
        parsObject(elements.next(), items);
    }

    private void writeToFile(String fileName, String json) throws IOException {
        System.out.println(json);
        File schemas = new File("schemas");
        schemas.mkdir();
        try (FileWriter writer = new FileWriter(new File(schemas, fileName))) {
            writer.write(json);
        }
    }

    private void setType(ObjectNode node, String... types) {
        ArrayNode jsonNodes = node.putArray(TYPE);
        for (String type : types) {
            jsonNodes.add(type);
        }
    }
}
