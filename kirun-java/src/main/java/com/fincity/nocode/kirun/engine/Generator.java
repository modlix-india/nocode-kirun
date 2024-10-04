package com.fincity.nocode.kirun.engine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.model.Event;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;

public class Generator {

    public static class GenerationException extends RuntimeException {

        public GenerationException(String message) {
            super(message);
        }
    }

    public static String escapeCsv(String value) {
        if (value == null)
            return "";
        return value.replace("\"", "\"\"");
    }

    public static void main(String[] args) {

        KIRunReactiveFunctionRepository repo = new KIRunReactiveFunctionRepository();
        List<String> functions = repo.filter("").collectList().block();
        if (functions == null)
            return;
        Collections.sort(functions);

        StringBuilder csv = new StringBuilder();

        for (String functionName : functions) {
            int ind = functionName.lastIndexOf('.');
            ReactiveFunction func = repo.find(functionName.substring(0, ind), functionName.substring(ind + 1)).block();

            if (func == null) {
                throw new GenerationException("Function definition not found for " + functionName);
            }

            FunctionSignature signature = func.getSignature();

            csv.append(escapeCsv(signature.getFullName()));
            csv.append("\n");
            csv.append("Parameter,Type,Schema\n");
            if (signature.getParameters() != null) {
                csv.append(signature.getParameters().entrySet().stream()
                        .map(entry -> {
                            String parameterName = entry.getKey();
                            com.fincity.nocode.kirun.engine.model.Parameter parameterType = entry.getValue();
                            String schemaType = parameterType.getSchema().getType() == null ? ""
                                    : parameterType.getSchema().getType().getAllowedSchemaTypes().stream()
                                            .map(SchemaType::toString)
                                            .map(String::toLowerCase)
                                            .sorted()
                                            .collect(Collectors.joining(";"));
                            return escapeCsv(parameterName) + "," + escapeCsv(parameterType.getType().toString()) + ","
                                    + escapeCsv(schemaType);
                        })
                        .collect(Collectors.joining("\n")) + "\n");
            }

            csv.append("Events\n");
            csv.append(signature.getEvents().entrySet().stream()
                    .map(entry -> {
                        String eventName = entry.getKey();
                        Event event = entry.getValue();
                        StringBuilder str = new StringBuilder(
                                escapeCsv(eventName) + "," + escapeCsv(event.getName()) + "\n");
                        str.append("Event Parameter,,Schema\n");
                        str.append(event.getParameters().entrySet().stream()
                                .map(evParam -> {
                                    String eventParameterName = evParam.getKey();
                                    String schemaType = evParam.getValue().getType() == null ? ""
                                            : evParam.getValue().getType().getAllowedSchemaTypes().stream()
                                                    .map(SchemaType::toString)
                                                    .map(String::toLowerCase)
                                                    .sorted()
                                                    .collect(Collectors.joining(";"));
                                    return escapeCsv(eventParameterName) + ",," + escapeCsv(schemaType);
                                })
                                .collect(Collectors.joining("\n")));

                        return str;
                    })
                    .collect(Collectors.joining("\n")));

            csv.append("\n\n");
        }

        System.out.println(csv);
    }
}
