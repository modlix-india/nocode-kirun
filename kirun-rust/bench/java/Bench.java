// kirun-java side of the benchmarks (see ../README.md). Built and run by ../run.sh.
// Usage: java Bench <bench dir> [workload...]

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.fincity.nocode.kirun.engine.json.schema.type.Type.SchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.validator.reactive.ReactiveSchemaValidator;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionEvaluator;
import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class Bench {

    interface Work {
        void run() throws Exception;
    }

    static void measure(String workload, int warmup, int runs, Work f) throws Exception {
        String override = System.getenv("KIRUN_BENCH_RUNS");
        if (override != null) runs = Integer.parseInt(override);
        for (int i = 0; i < warmup; i++) f.run();
        List<Double> times = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            long t = System.nanoTime();
            f.run();
            times.add((System.nanoTime() - t) / 1e6);
        }
        Collections.sort(times);
        System.out.printf("{\"lang\":\"java\",\"workload\":\"%s\",\"runs\":%d,\"median_ms\":%s,\"min_ms\":%s}%n",
                workload, runs, times.get(times.size() / 2), times.get(0));
    }

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args[0]);
        List<String> only = Arrays.asList(args).subList(1, args.length);
        java.util.function.Predicate<String> wants = w -> only.isEmpty() || only.contains(w);

        AdditionalTypeAdapter additional = new AdditionalTypeAdapter();
        ArraySchemaTypeAdapter arraySchema = new ArraySchemaTypeAdapter();
        Gson gson = new GsonBuilder().registerTypeAdapter(Type.class, new SchemaTypeAdapter())
                .registerTypeAdapter(AdditionalType.class, additional)
                .registerTypeAdapter(ArraySchemaType.class, arraySchema)
                .create();
        additional.setGson(gson);
        arraySchema.setGson(gson);

        JsonObject cfg = JsonParser.parseString(Files.readString(dir.resolve("config.json"))).getAsJsonObject();
        var fRepo = new KIRunReactiveFunctionRepository();
        var sRepo = new KIRunReactiveSchemaRepository();

        for (String[] w : new String[][] { { "fibonacci", "fibonacci.json" }, { "loopsum", "loopsum.json" } }) {
            if (!wants.test(w[0])) continue;
            JsonObject c = cfg.getAsJsonObject(w[0]);
            FunctionDefinition fd = gson.fromJson(Files.readString(dir.resolve("defs").resolve(w[1])),
                    FunctionDefinition.class);
            Map<String, JsonElement> fargs = Map.of("n", c.get("n"));
            measure(w[0], c.get("warmup").getAsInt(), c.get("runs").getAsInt(), () -> {
                var out = new ReactiveKIRuntime(fd)
                        .execute(new ReactiveFunctionExecutionParameters(fRepo, sRepo).setArguments(fargs))
                        .block();
                if (out == null || out.next() == null) throw new IllegalStateException("no output");
            });
        }

        if (wants.test("expression")) {
            JsonObject c = cfg.getAsJsonObject("expression");
            String expression = c.get("expression").getAsString();
            Map<String, JsonElement> eargs = new HashMap<>();
            for (var e : c.getAsJsonObject("args").entrySet()) eargs.put(e.getKey(), e.getValue());
            var atv = new ArgumentsTokenValueExtractor(eargs);
            Map<String, TokenValueExtractor> values = Map.of(atv.getPrefix(), atv);
            if (!new ExpressionEvaluator(expression).evaluate(values).equals(new JsonPrimitive(true)))
                throw new IllegalStateException("expression is not true: "
                        + new ExpressionEvaluator(expression).evaluate(values));
            int n = c.get("evaluations").getAsInt();
            measure("expression", c.get("warmup").getAsInt(), c.get("runs").getAsInt(), () -> {
                for (int i = 0; i < n; i++) new ExpressionEvaluator(expression).evaluate(values);
            });
        }

        if (wants.test("schema")) {
            JsonObject c = cfg.getAsJsonObject("schema");
            Schema schema = gson.fromJson(Files.readString(dir.resolve("defs/record_schema.json")), Schema.class);
            String text = Files.readString(dir.resolve("data/records.json"));
            measure("schema", c.get("warmup").getAsInt(), c.get("runs").getAsInt(), () -> {
                JsonElement out = ReactiveSchemaValidator.validate(null, schema, sRepo, JsonParser.parseString(text)).block();
                if (out == null || out.getAsJsonArray().size() != 5000) throw new IllegalStateException("bad output");
            });
        }
    }
}
