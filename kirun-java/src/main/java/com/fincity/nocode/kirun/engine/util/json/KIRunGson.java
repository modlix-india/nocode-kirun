package com.fincity.nocode.kirun.engine.util.json;

import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType;
import com.fincity.nocode.kirun.engine.json.schema.array.ArraySchemaType.ArraySchemaTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType;
import com.fincity.nocode.kirun.engine.json.schema.object.AdditionalType.AdditionalTypeAdapter;
import com.fincity.nocode.kirun.engine.json.schema.type.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Central factory for a Gson that can round-trip a
 * {@link com.fincity.nocode.kirun.engine.json.schema.Schema}.
 *
 * Schema needs three custom adapters, two of which need a back-reference to the finished Gson
 * instance, so assembling one correctly is easy to get subtly wrong. Anything that serialises a
 * Schema - or anything containing one, such as a suspended execution's context - should come
 * through here rather than building its own GsonBuilder.
 */
public class KIRunGson {

	private static final Gson INSTANCE = build();

	private KIRunGson() {
	}

	/**
	 * A shared Gson. Gson is immutable and thread-safe once built, so this can be used freely
	 * from any thread.
	 */
	public static Gson get() {
		return INSTANCE;
	}

	private static Gson build() {

		GsonBuilder builder = new GsonBuilder();

		builder.registerTypeAdapter(Type.class, new Type.SchemaTypeAdapter());

		AdditionalTypeAdapter ata = new AdditionalTypeAdapter();
		builder.registerTypeAdapter(AdditionalType.class, ata);

		ArraySchemaTypeAdapter asta = new ArraySchemaTypeAdapter();
		builder.registerTypeAdapter(ArraySchemaType.class, asta);

		Gson gson = builder.create();

		// Both adapters delegate nested schema (de)serialisation back to the completed Gson.
		ata.setGson(gson);
		asta.setGson(gson);

		return gson;
	}
}
