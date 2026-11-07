<p align="center">
  <a href="https://kirun.modlix.com">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/logo-dark.png">
      <img src="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/logo-light.png" alt="kirun" width="280">
    </picture>
  </a>
</p>

<h3 align="center">kirun-java: the Java runtime for KIRun</h3>

<p align="center">
  <a href="https://kirun.modlix.com/playground"><strong>Playground</strong></a> ·
  <a href="https://kirun.modlix.com/docs">Docs</a> ·
  <a href="https://kirun.modlix.com/functions">Functions</a> ·
  <a href="https://github.com/modlix-india/nocode-kirun">GitHub</a>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/com.fincity.nocode/kirun-java"><img src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Ffincity%2Fnocode%2Fkirun-java%2Fmaven-metadata.xml&style=flat-square&label=maven%20central&color=7AA2F7&labelColor=0A0B11" alt="Maven Central"></a>
  <a href="https://github.com/modlix-india/nocode-kirun/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-9ECE6A?style=flat-square&labelColor=0A0B11" alt="MIT License"></a>
</p>

---

KIRun runs functions that are stored as data: a JSON definition with parameters, events and a graph
of steps. kirun-java runs those definitions on the server, on
[Project Reactor](https://projectreactor.io): every function returns a `Mono<FunctionOutput>`. The
same definitions run in the browser on
[kirun-js](https://github.com/modlix-india/nocode-kirun/tree/master/kirun-js), and on
[Rust](https://github.com/modlix-india/nocode-kirun/tree/master/kirun-rust) and
[Python](https://github.com/modlix-india/nocode-kirun/tree/master/kirun-py).

## Install

Java 21 or later.

```xml
<dependency>
    <groupId>com.fincity.nocode</groupId>
    <artifactId>kirun-java</artifactId>
    <version><!-- the version on the badge above --></version>
</dependency>
```

## Run a function

kirun-java reads the JSON form of a definition. Write the function as text in the
[playground](https://kirun.modlix.com/playground) and copy the JSON from its JSON tab, or compile the
text with kirun-js, kirun-rust or kirun-py.

```java
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.model.FunctionOutput;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;

Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter()).create();
FunctionDefinition fd = gson.fromJson(json, FunctionDefinition.class);

new ReactiveKIRuntime(fd)
    .execute(new ReactiveFunctionExecutionParameters(
            new KIRunReactiveFunctionRepository(), new KIRunReactiveSchemaRepository())
        .setArguments(Map.of("n", new JsonPrimitive(9))))
    .map(FunctionOutput::next)
    .subscribe(e -> System.out.println(e.getName() + " " + e.getResult()));
```

With the Fizz function from the [main README](https://github.com/modlix-india/nocode-kirun#quick-start)
this prints `output {say="Fizz"}`. `new ReactiveKIRuntime(fd, true)` turns on debug mode.

## Documentation

- [Architecture](https://kirun.modlix.com/docs/architecture): definitions, steps, the execution plan
- [Expressions](https://kirun.modlix.com/docs/expressions)
- [Built-in functions](https://kirun.modlix.com/docs/functions)
- [Custom functions](https://kirun.modlix.com/docs/custom-functions): `AbstractReactiveFunction` and your own repositories
- [Schemas](https://kirun.modlix.com/docs/schemas) and [debugging](https://kirun.modlix.com/docs/debugging)

## Build

```bash
./mvnw clean install
./mvnw test
```

## License

MIT. Built by [Modlix](https://modlix.com).
