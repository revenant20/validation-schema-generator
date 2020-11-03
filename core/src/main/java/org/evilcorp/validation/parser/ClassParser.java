package org.evilcorp.validation.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class ClassParser {

    public static final String OBJECT = "object";
    public static final String TYPE = "type";
    public static final String STRING = "string";
    public static final String MAX_LENGTH = "maxLength";
    public static final String PROPERTIES = "properties";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String ARRAY = "array";

    private final ResponseWrapperWriter responseWrapperWriter;

    public void parse(Class<?> cl, MessageType type) throws IOException {
        var fileName = cl.getSimpleName() + ".json";
        var objectMapper = new ObjectMapper();
        var mainObj = objectMapper.createObjectNode();
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
                var properties = mainObj.putObject(PROPERTIES);
                properties.putObject("success").put(TYPE, "boolean");
                var body = properties.putObject("body");
                setType(body, ARRAY, "null");
                body.put(ADDITIONAL_PROPERTIES, false);
                body.put("maxItems", 1000);
                var items = body.putObject("items");
                setType(items, OBJECT, "null");
                responseWrapperWriter.write(properties);
                parsObject(cl, items);
                break;
            case RS:
                mainObj.put(TYPE, OBJECT);
                mainObj.put(ADDITIONAL_PROPERTIES, false);
                var propOne = mainObj.putObject(PROPERTIES);
                propOne.putObject("success").put(TYPE, "boolean");
                var bodyOne = propOne.putObject("body");
                setType(bodyOne, OBJECT, "null");
                responseWrapperWriter.write(propOne);
                parsObject(cl, bodyOne);
                break;

        }
        writeToFile(fileName, objectMapper.writeValueAsString(mainObj));
    }

    private void writeToFile(String fileName, String json) throws IOException {
        var schemas = new File("schemas");
        schemas.mkdir();
        try (var writer = new FileWriter(new File(schemas, fileName))) {
            writer.write(json);
        }
    }

    private void parsObject(Class<?> cl, ObjectNode node) {
        setType(node, OBJECT, "null");
        node.put(ADDITIONAL_PROPERTIES, false);
        var properties = node.putObject(PROPERTIES);
        for (var field : cl.getDeclaredFields()) {
            var b = Arrays.stream(field.getAnnotations())
                    .anyMatch(a -> a.annotationType() == JsonIgnore.class);
            if (b) {
                continue;
            }
            var hasJsonProperty = Arrays.stream(field.getAnnotations())
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
            var innerObject = properties.putObject(fieldName);
            var type = field.getType();
            if (type.isAssignableFrom(String.class) || type.isAssignableFrom(Date.class)) {
                var annotation = field.getAnnotation(Size.class);
                var length = annotation == null ? 200 : annotation.value();
                setType(innerObject, STRING, "null");
                innerObject.put(MAX_LENGTH, length);
            } else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Long.class)) {
                var annotation = field.getAnnotation(Size.class);
                var length = annotation == null ? 200 : annotation.value();
                setType(innerObject, "integer", "null");
                innerObject.put("minimum", 0).put("maximum", length);
            } else if (type.isAssignableFrom(Boolean.class)) {
                setType(innerObject, "boolean", "null");
            } else if (type.isAssignableFrom(List.class)) {
                parsList(field, innerObject);
            } else if (type.isEnum()) {
                setType(innerObject, STRING, "null");
                innerObject.put(MAX_LENGTH, 20);
                var anEnum = innerObject.putArray("enum");
                for (Object enumConstant : type.getEnumConstants()) {
                    anEnum.add(enumConstant.toString());
                }
            } else {
                parsObject(type, innerObject);
            }
        }
    }

    private void setType(ObjectNode node, String... types) {
        var jsonNodes = node.putArray(TYPE);
        for (var type : types) {
            jsonNodes.add(type);
        }
    }


    private void parsList(Field field, ObjectNode node) {
        setType(node, ARRAY, "null");
        node.put("maxItems", 100);
        node.put("additionalItems", false);
        var items = node.putObject("items");
        var genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            var refType = (ParameterizedType) genericType;
            var typeArgument = (Class<?>) refType.getActualTypeArguments()[0];
            parsObject(typeArgument, items);
        }
    }
}
