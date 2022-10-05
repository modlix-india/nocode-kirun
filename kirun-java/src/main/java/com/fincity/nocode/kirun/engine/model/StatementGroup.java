package com.fincity.nocode.kirun.engine.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.type.SchemaType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
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
	private Map<String, Boolean> statements;

	public StatementGroup(StatementGroup stg) {

		super(stg);
		this.statementGroupName = stg.statementGroupName;
		this.statements = stg.statements == null ? null
		        : stg.statements.entrySet()
		                .stream()
		                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
}
