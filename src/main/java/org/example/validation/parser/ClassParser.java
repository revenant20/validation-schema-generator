package org.example.validation.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ClassParser {

    public static final String OBJECT = "object";
    public static final String TYPE = "type";
    public static final String STRING = "string";
    public static final String MAX_LENGTH = "maxLength";
    public static final String PROPERTIES = "properties";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String ARRAY = "array";

    public void parse(Class<?> cl, MessageType type) throws IOException {
        String fileName = cl.getSimpleName() + ".json";
        System.out.println(fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode mainObj = objectMapper.createObjectNode();
        mainObj.put("$schema", "http://json-schema.org/draft-04/schema#");
        mainObj.put("title", "title");
        mainObj.put("description", "description");
        switch (type) {
            case RQ:
                mainObj.put(TYPE, OBJECT);
                parsObject(cl, mainObj);
                break;
            case RS_ARR:
                mainObj.put(TYPE, OBJECT);
                mainObj.put(ADDITIONAL_PROPERTIES, false);
                ObjectNode properties = mainObj.putObject(PROPERTIES);
                properties.putObject("success").put(TYPE, "boolean");
                ObjectNode body = properties.putObject("body");
                setType(body, ARRAY, "null");
                body.put(ADDITIONAL_PROPERTIES, false);
                body.put("maxItems", 1000);
                ObjectNode items = body.putObject("items");
                setType(items, OBJECT, "null");
                putErrorAndMessegesForResponse(properties);
                parsObject(cl, items);
                break;
            case RS:
                mainObj.put(TYPE, OBJECT);
                mainObj.put(ADDITIONAL_PROPERTIES, false);
                ObjectNode propOne = mainObj.putObject(PROPERTIES);
                propOne.putObject("success").put(TYPE, "boolean");
                ObjectNode bodyOne = propOne.putObject("body");
                setType(bodyOne, OBJECT, "null");
                putErrorAndMessegesForResponse(propOne);
                parsObject(cl, bodyOne);
                break;

        }
        writeToFile(fileName, objectMapper.writeValueAsString(mainObj));
    }

    private void putErrorAndMessegesForResponse(ObjectNode properties) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource("responseTemplate.json").getFile());
        JsonNode jsonNode = new ObjectMapper().readTree(file);
        properties.set("messages", jsonNode.get("messages"));
        properties.set("error", jsonNode.get("error"));
    }

    private void writeToFile(String fileName, String json) throws IOException {
        System.out.println(json);
        File schemas = new File("schemas");
        schemas.mkdir();
        try (FileWriter writer = new FileWriter(new File(schemas, fileName))) {
            writer.write(json);
        }
    }

    private void parsObject(Class<?> cl, ObjectNode node) {
        setType(node, OBJECT, "null");
        node.put(ADDITIONAL_PROPERTIES, false);
        ObjectNode properties = node.putObject(PROPERTIES);
        for (Field field : cl.getDeclaredFields()) {
            boolean b = Arrays.stream(field.getAnnotations())
                    .anyMatch(a -> a.annotationType() == JsonIgnore.class);
            if (b) {
                continue;
            }
            boolean hasJsonProperty = Arrays.stream(field.getAnnotations())
                    .anyMatch(a -> a.annotationType() == JsonProperty.class);
            final String fieldName;
            if (hasJsonProperty) {
                fieldName = Arrays.stream(field.getAnnotations())
                        .filter(a -> a.annotationType() == JsonProperty.class)
                        .findFirst()
                        .map(annotation -> (JsonProperty) annotation)
                        .map(JsonProperty::value)
                        .map(value -> value.substring(0, 1).toLowerCase() + value.substring(1))
                        .orElse(field.getName());
            } else {
                fieldName = field.getName();
            }
            ObjectNode innerObject = properties.putObject(fieldName);
            Class<?> type = field.getType();
            if (type.isAssignableFrom(String.class) || type.isAssignableFrom(Date.class)) {
                Size annotation = field.getAnnotation(Size.class);
                int length = annotation == null ? 200 : annotation.value();
                setType(innerObject, STRING, "null");
                innerObject.put(MAX_LENGTH, length);
            } else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Long.class)) {
                Size annotation = field.getAnnotation(Size.class);
                int length = annotation == null ? 200 : annotation.value();
                setType(innerObject, "integer", "null");
                innerObject.put("minimum", 0).put("maximum", length);
            } else if (type.isAssignableFrom(Boolean.class)) {
                setType(innerObject, "boolean", "null");
            } else if (type.isAssignableFrom(List.class)) {
                parsList(field, innerObject);
            } else if (type.isEnum()) {
                setType(innerObject, STRING, "null");
                innerObject.put(MAX_LENGTH, 20);
                ArrayNode anEnum = innerObject.putArray("enum");
                for (Object enumConstant : type.getEnumConstants()) {
                    anEnum.add(enumConstant.toString());
                }
            } else {
                parsObject(type, innerObject);
            }
        }
    }

    private void setType(ObjectNode node, String... types) {
        ArrayNode jsonNodes = node.putArray(TYPE);
        for (String type : types) {
            jsonNodes.add(type);
        }
    }


    private void parsList(Field field, ObjectNode node) {
        setType(node, ARRAY, "null");
        node.put("maxItems", 100);
        node.put("additionalItems", false);
        ObjectNode items = node.putObject("items");
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType refType = (ParameterizedType) genericType;
            Class typeArgument = (Class) refType.getActualTypeArguments()[0];
            parsObject(typeArgument, items);
        }
    }
}
