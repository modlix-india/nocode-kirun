package com.fincity.nocode.kirun.engine.json.schema.validator.exception;

public class SchemaReferenceException extends RuntimeException {

	private static final long serialVersionUID = 6039746650450241728L;

	private final String schemaPath;

	public SchemaReferenceException(String schemaPath, String message) {

		super(message);
		this.schemaPath = schemaPath;
	}
	
	public String getSchemaPath() {

		return this.schemaPath;
	}


	@Override
	public String getMessage() {

		if (schemaPath != null && !schemaPath.isBlank())
			return this.schemaPath + " - " + super.getMessage();

		return super.getMessage();
	}
}
