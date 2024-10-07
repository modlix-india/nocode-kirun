package com.fincity.nocode.kirun.engine.json.schema.convertor.exception;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.convertor.enums.ConversionMode;
import com.google.gson.JsonElement;

import lombok.Getter;

@Getter
public class SchemaConversionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6155360538579271997L;

    private final String schemaPath;
    private final String source;
    private final ConversionMode mode;
    private final List<SchemaConversionException> scrList;

    public SchemaConversionException(String schemaPath, JsonElement source, ConversionMode mode, Throwable t) {
        this(schemaPath, source, mode, "Error converting schema: " + (t != null ? t.getMessage() : "Unknown error"), null);
    }

    public SchemaConversionException(String schemaPath, JsonElement source, ConversionMode mode, String message,
            List<SchemaConversionException> scr) {
        super(message + (scr == null ? ""
                : scr.stream()
                        .map(SchemaConversionException::getMessage)
                        .collect(Collectors.joining("\n", "\n", ""))));
        this.mode = mode;
        this.source = source != null ? source.toString() : "No source provided";
        this.schemaPath = schemaPath;
        this.scrList = scr;
    }

    public SchemaConversionException(String schemaPath, JsonElement source, ConversionMode mode, String message) {
        this(schemaPath, source, mode, message, null);
    }

    @Override
    public String getMessage() {
        if (schemaPath != null && !schemaPath.isBlank())
            return this.schemaPath + " - " + super.getMessage();
        return super.getMessage();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        if (scrList == null || scrList.isEmpty())
            return super.getStackTrace();

        List<StackTraceElement[]> traces = new ArrayList<>();
        traces.add(super.getStackTrace());
        for (SchemaConversionException scr : this.scrList) {
            traces.add(scr.getStackTrace());
        }

        StackTraceElement[] traceElements = new StackTraceElement[traces.stream()
                .mapToInt(e -> e.length).sum()];

        int i = 0;
        for (StackTraceElement[] trace : traces)
            for (StackTraceElement e : trace)
                traceElements[i++] = e;

        return traceElements;
    }
}
