package com.fincity.nocode.kirun.engine.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reactor.test.StepVerifier;

class KIRuntimeLoopBugBranchTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;

	 @BeforeEach
	 public void setUpStreams() {
	 System.setOut(new PrintStream(outContent));
	 System.setErr(new PrintStream(errContent));
	 }

	 @AfterEach
	 public void restoreStreams() {
	 System.setOut(originalOut);
	 System.setErr(originalErr);
	 }

	@Test
	void testBranching() {

		AdditionalTypeAdapter addType = new AdditionalTypeAdapter();
		ArraySchemaTypeAdapter asType = new ArraySchemaTypeAdapter();

		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
				.registerTypeAdapter(AdditionalType.class, addType)
				.registerTypeAdapter(ArraySchemaType.class, asType)
				.create();

		addType.setGson(gson);
		asType.setGson(gson);

		var func = """
					{
  "name": "testloophistory",
  "events": {
				    "output": {
				      "name": "output",
				      "parameters": {
				        "result": {
				          "schema": {
				            "type": [
				              "INTEGER"
				            ],
				            "version": 1
				          }
				        }
				      }
				    }
				  },
  "steps": {
    "create": {
      "statementName": "create",
      "name": "Create",
      "namespace": "System.Context",
      "position": {
        "left": 129.6015625,
        "top": 23.8984375
      },
      "parameterMap": {
        "name": {
          "1nngcowEVDtBrXBhyFsfqB": {
            "key": "1nngcowEVDtBrXBhyFsfqB",
            "type": "VALUE",
            "expression": "",
            "order": 1,
            "value": "arr"
          }
        },
        "schema": {
          "paM69eD4wlyJv3NLnlpRf": {
            "key": "paM69eD4wlyJv3NLnlpRf",
            "type": "VALUE",
            "expression": "",
            "order": 1,
            "value": {
              "type": "ARRAY",
              "items": {
                "type": "STRING"
              }
            }
          }
        }
      }
    },
    "loop": {
      "statementName": "loop",
      "name": "ForEachLoop",
      "namespace": "System.Loop",
      "position": {
        "left": 622.4461805555555,
        "top": 855.7760416666667
      },
      "parameterMap": {
        "source": {
          "4fQaZPve4hAoLyIFbaSnEE": {
            "key": "4fQaZPve4hAoLyIFbaSnEE",
            "type": "EXPRESSION",
            "expression": "Context.arr",
            "order": 1
          }
        }
      },
      "dependentStatements": {
        "Steps.set.output": true,
        "Steps.set1.output": true
      }
    },
    "create1": {
      "statementName": "create1",
      "name": "Create",
      "namespace": "System.Context",
      "position": {
        "left": 93.890625,
        "top": 453.55381944444446
      },
      "parameterMap": {
        "name": {
          "5ntX0A6D5lObvGesm9kciW": {
            "key": "5ntX0A6D5lObvGesm9kciW",
            "type": "VALUE",
            "expression": "",
            "order": 1,
            "value": "count"
          }
        },
        "schema": {
          "1Se6m0293IcsOiFRKQlQen": {
            "key": "1Se6m0293IcsOiFRKQlQen",
            "type": "VALUE",
            "expression": "",
            "order": 1,
            "value": {
              "type": "INTEGER"
            }
          }
        }
      }
    },
    "set1": {
      "statementName": "set1",
      "name": "Set",
      "namespace": "System.Context",
      "position": {
        "left": 356.77951388888886,
        "top": 633.9982638888889
      },
      "parameterMap": {
        "name": {
          "3QBReTdFoCDSMa7SBrjHje": {
            "key": "3QBReTdFoCDSMa7SBrjHje",
            "type": "VALUE",
            "expression": "",
            "order": 1,
            "value": "Context.count"
          }
        },
        "value": {
          "5Luu5FddZu8SYKOrgkdUoV": {
            "key": "5Luu5FddZu8SYKOrgkdUoV",
            "type": "VALUE",
            "order": 1,
            "value": 0
          }
        }
      },
      "dependentStatements": {
        "Steps.create1.output": true
      }
    },
    "generateEvent": {
      "statementName": "generateEvent",
      "name": "GenerateEvent",
      "namespace": "System",
      "position": {
        "left": 857.1128472222222,
        "top": 1194.109375
      },
      "parameterMap": {
        "results": {
          "1Ddtjl1fJKLsUvZdr127RI": {
            "key": "1Ddtjl1fJKLsUvZdr127RI",
            "type": "VALUE",
            "expression": "",
            "order": 1,
            "value": {
              "name": "count",
              "value": {
                "isExpression": true,
                "value": "Context.count"
              }
            }
          }
        }
      },
      "dependentStatements": {
        "Steps.loop.output": true
      }
    },
    "set": {
      "statementName": "set",
      "name": "Set",
      "namespace": "System.Context",
      "position": {
        "left": 332.6015625,
        "top": 230.8984375
      },
      "parameterMap": {
        "name": {
          "w8fhgGiU2OmUfqFAiTPh9": {
            "key": "w8fhgGiU2OmUfqFAiTPh9",
            "type": "VALUE",
            "expression": "",
            "order": 1,
            "value": "Context.arr"
          }
        },
        "value": {
          "4dFnOUA9f80JhkqsKZJFvz": {
            "key": "4dFnOUA9f80JhkqsKZJFvz",
            "type": "VALUE",
            "order": 1,
            "value": [
              "kiran",
              "hi",
              "",
              "",
              "hello",
              "",
              "how"
            ]
          }
        }
      },
      "dependentStatements": {
        "Steps.create.output": true
      }
    },
    "trim": {
      "statementName": "trim",
      "name": "Trim",
      "namespace": "System.String",
      "position": {
        "left": 907.1015625,
        "top": 760.3984375
      },
      "parameterMap": {
        "value": {
          "7uvjy8hwDzlkIO6s8NLWr3": {
            "key": "7uvjy8hwDzlkIO6s8NLWr3",
            "type": "EXPRESSION",
            "expression": "Steps.loop.iteration.each",
            "order": 1
          }
        }
      }
    },
    "if": {
      "statementName": "if",
      "name": "If",
      "namespace": "System",
      "position": {
        "left": 1273.890625,
        "top": 680.3315972222222
      },
      "parameterMap": {
        "condition": {
          "2rnqfOmJtOVK4RzhK0lPDu": {
            "key": "2rnqfOmJtOVK4RzhK0lPDu",
            "type": "EXPRESSION",
            "expression": "Steps.trim.output.value != ''",
            "order": 1
          }
        }
      }
    },
    "set2": {
      "statementName": "set2",
      "name": "Set",
      "namespace": "System.Context",
      "position": {
        "left": 1833.6684027777778,
        "top": 967.9982638888889
      },
      "parameterMap": {
        "name": {
          "7PMLUgrXK2AK1WqAPMsD4": {
            "key": "7PMLUgrXK2AK1WqAPMsD4",
            "type": "VALUE",
            "expression": "",
            "order": 1,
            "value": "Context.count"
          }
        },
        "value": {
          "1sbsvevDB8XQpFLvGE9Eoq": {
            "key": "1sbsvevDB8XQpFLvGE9Eoq",
            "type": "EXPRESSION",
            "expression": "Context.count + 1",
            "order": 1
          }
        }
      },
      "dependentStatements": {
        "Steps.if.true": true
      }
    },
    "print": {
      "statementName": "print",
      "name": "Print",
      "namespace": "System",
      "position": {
        "left": 1493.2265625,
        "top": 951.09375
      },
      "parameterMap": {
        "values": {
          "7GEehmufgT3pw3P6dAEkY7": {
            "key": "7GEehmufgT3pw3P6dAEkY7",
            "type": "EXPRESSION",
            "expression": "Steps.trim.output.value",
            "order": 1
          },
          "5ESXu8zlY5wJLUzPt0ed5I": {
            "key": "5ESXu8zlY5wJLUzPt0ed5I",
            "type": "EXPRESSION",
            "expression": "Steps.loop.iteration.each",
            "order": 2
          },
          "1Lghu5LxsX0DEDqLJmpzUY": {
            "key": "1Lghu5LxsX0DEDqLJmpzUY",
            "type": "EXPRESSION",
            "order": 3
          }
        }
      },
      "dependentStatements": {
        "Steps.if.true": true
      }
    }
  },
  "namespace": ""
}
				""";

		var fd = gson.fromJson(func, FunctionDefinition.class);

		var runtime = new ReactiveKIRuntime(fd, true);

		var execution = runtime.execute(
				new ReactiveFunctionExecutionParameters(new KIRunReactiveFunctionRepository(),
						new KIRunReactiveSchemaRepository()))
				.map(fo -> fo.next().getResult().get("count").getAsInt());


		StepVerifier.create(execution)
				.expectNext(4)
				.verifyComplete();
		
		 assertEquals(8, outContent.toString().split("\n").length);
	}
}