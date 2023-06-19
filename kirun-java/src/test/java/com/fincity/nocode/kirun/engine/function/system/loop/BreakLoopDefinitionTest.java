package com.fincity.nocode.kirun.engine.function.system.loop;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class BreakLoopDefinitionTest {

	@Test
	void test() {
		String definition = """
{
  "name": "Break Me 1",
  "events": {
    "output": {
      "name": "output",
      "parameters": {
        "returnValue": {
          "schema": {
            "type": "ARRAY",
            "items": {
              "type": "INTEGER"
            }
          }
        }
      }
    }
  },
  "steps": {
    "create": {
      "name": "Create",
      "namespace": "System.Context",
      "statementName": "create",
      "parameterMap": {
        "name": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": "array"
          }
        },
        "schema": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": {
              "type": "ARRAY",
              "items": {
                "type": "INTEGER"
              }
            }
          }
        }
      }
    },
    "createSet": {
      "name": "Set",
      "namespace": "System.Context",
      "statementName": "createSet",
      "parameterMap": {
        "name": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": "Context.array"
          }
        },
        "value": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": []
          }
        }
      },
      "dependentStatements": {
        "Steps.create.output": true
      }
    },
    "loop": {
      "name": "RangeLoop",
      "namespace": "System.Loop",
      "statementName": "loop",
      "parameterMap": {
        "from": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": 5
          }
        },
        "to": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": 10
          }
        },
        "step": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": 1
          }
        }
      },
      "dependentStatements": {
        "Steps.createSet.output": true
      }
    },
    "insert": {
      "name": "InsertLast",
      "namespace": "System.Array",
      "statementName": "insert",
      "parameterMap": {
        "source": {
          "one": {
            "key": "one",
            "type": "EXPRESSION",
            "expression": "Context.array"
          }
        },
        "element": {
          "one": {
            "key": "one",
            "type": "EXPRESSION",
            "expression": "Steps.loop.iteration.index"
          }
        }
      },
      "dependentStatements": {
        "Steps.if.false": true
      }
    },
    "set": {
      "name": "Set",
      "namespace": "System.Context",
      "statementName": "set",
      "parameterMap": {
        "name": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": "Context.array"
          }
        },
        "value": {
          "one": {
            "key": "one",
            "type": "EXPRESSION",
            "expression": "Steps.insert.output.result"
          }
        }
      }
    },
    "if": {
      "name": "If",
      "namespace": "System",
      "statementName": "if",
      "parameterMap": {
        "condition": {
          "one": {
            "key": "one",
            "type": "EXPRESSION",
            "expression": "Steps.loop.iteration.index = 7"
          }
        }
      }
    },
    "break": {
      "name": "Break",
      "namespace": "System.Loop",
      "statementName": "break",
      "parameterMap": {
        "stepName": {
          "one": {
            "key": "one",
            "type": "VALUE",
            "value": "loop"
          }
        }
      },
      "dependentStatements": {
        "Steps.if.true": true
      }
    },
    "generateEvent": {
      "statementName": "generateEvent",
      "name": "GenerateEvent",
      "namespace": "System",
      "parameterMap": {
        "eventName": {
          "5OdGxruBiEyysESbAubdX2": {
            "key": "5OdGxruBiEyysESbAubdX2",
            "type": "VALUE",
            "expression": "",
            "value": "output"
          }
        },
        "results": {
          "4o0c0kvVtWiGjgb37hMTBX": {
            "key": "4o0c0kvVtWiGjgb37hMTBX",
            "type": "VALUE",
            "order": 1,
            "value": {
              "name": "returnValue",
              "value": {
                "isExpression": true,
                "value": "Context.array"
              }
            }
          }
        }
      },
      "dependentStatements": {
        "Steps.loop.output": true
      }
    }
  }
}
		        		        """;

		Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
		        .registerTypeAdapter(AdditionalType.class, new AdditionalType.AdditionalTypeAdapter())
		        .registerTypeAdapter(ArraySchemaType.class, new ArraySchemaTypeAdapter())
		        .create();

		Mono<FunctionOutput> fo = new ReactiveKIRuntime(gson.fromJson(definition, FunctionDefinition.class))
		        .execute(new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
		                new KIRunReactiveSchemaRepository()));
		JsonArray result = new JsonArray();
		result.add(5);
		result.add(6);
		
		StepVerifier.create(fo.map(e -> e.next()
		        .getResult()
		        .get("returnValue")))
		        .expectNext(result)
		        .verifyComplete();
	}

}
