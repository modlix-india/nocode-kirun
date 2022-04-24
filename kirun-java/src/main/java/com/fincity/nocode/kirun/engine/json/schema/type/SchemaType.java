package com.fincity.nocode.kirun.engine.json.schema.type;

public enum SchemaType {

	INTEGER("Integer"), LONG("Long"), FLOAT("Float"), DOUBLE("Double"), STRING("String"), OBJECT("Object"),
	ARRAY("Array"), BOOLEAN("Boolean"), NULL("Null");

	private String printableName;

	SchemaType(String printableName) {
		this.printableName = printableName;
	}

	public String getPrintableName() {
		return this.printableName;
	}

	public static SchemaType genericValueOf(String type) {
		return SchemaType.valueOf(type.toUpperCase());
	}
}
