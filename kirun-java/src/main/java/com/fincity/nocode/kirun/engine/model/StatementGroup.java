package com.fincity.nocode.kirun.engine.model;

import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatementGroup extends AbstractStatement {

	private static final long serialVersionUID = -550951603829175548L;

	private static final String SCHEMA_NAME = "StatementGroup";

	public static final Schema SCHEMA = new Schema().setNamespace(Namespaces.SYSTEM)
	        .setName(SCHEMA_NAME)
	        .setType(Type.of(SchemaType.OBJECT))
	        .setProperties(Map.of("statementGroupName", Schema.ofString("statementGroupName"), "comment",
	                Schema.ofString("comment"), "description", Schema.ofString("description"), "position",
	                Position.SCHEMA));

	private String statementGroupName;
}
