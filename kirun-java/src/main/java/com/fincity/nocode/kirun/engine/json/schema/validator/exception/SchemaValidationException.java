package com.fincity.nocode.kirun.engine.json.schema.validator.exception;

import java.util.ArrayList;
import java.util.List;

public class SchemaValidationException extends RuntimeException {

	private static final long serialVersionUID = 6039746650450241728L;

	private final String schemaPath;
	private final List<SchemaValidationException> sveList;

	public SchemaValidationException(String schemaPath, String message, List<SchemaValidationException> sve) {

		super(message);
		this.schemaPath = schemaPath;
		this.sveList = sve;
	}

	public SchemaValidationException(String schemaPath, String message) {
		this(schemaPath, message, null);
	}

	public String getSchemaPath() {

		return this.schemaPath;
	}

	@Override
	public StackTraceElement[] getStackTrace() {

		if (sveList == null || sveList.isEmpty())
			return super.getStackTrace();

		List<StackTraceElement[]> traces = new ArrayList<>();

		traces.add(super.getStackTrace());
		for (SchemaValidationException sve : this.sveList) {
			traces.add(sve.getStackTrace());
		}

		StackTraceElement[] traceElements = new StackTraceElement[traces.stream()
		        .mapToInt(e -> e.length)
		        .sum()];

		int i = 0;
		for (StackTraceElement[] trace : traces)
			for (StackTraceElement e : trace)
				traceElements[i++] = e;

		return traceElements;
	}

	@Override
	public String getMessage() {

		if (schemaPath != null && !schemaPath.isBlank())
			return this.schemaPath + " - " + super.getMessage();

		return super.getMessage();
	}
}
