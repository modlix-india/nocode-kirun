package com.fincity.nocode.kirun.engine.runtime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor.TokenValueExtractor;
import com.fincity.nocode.kirun.engine.runtime.tokenextractors.ArgumentsTokenValueExtractor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Bracket notation over keys whose characters collide with operators.
 *
 * The JS engine has had a bracket-notation suite for a while; this is its
 * counterpart, so the two runtimes are held to the same contract. Both share
 * the same {@code splitPath} shape, so a divergence here is a real bug rather
 * than a dialect difference.
 *
 * The motivating case is real: an app definition's {@code properties.metas}
 * entries carry an attribute literally named {@code http-equiv}, and a hyphen
 * is also subtraction.
 */
public class ExpressionEvaluatorBracketNotationTest {

    private Map<String, TokenValueExtractor> valuesMap() {

        JsonObject obj = new JsonObject();
        obj.add("key-with-hyphen", new JsonPrimitive("hyphenValue"));
        obj.add("key with space", new JsonPrimitive("spaceValue"));
        obj.add("key+plus", new JsonPrimitive("plusValue"));
        obj.add("key:colon", new JsonPrimitive("colonValue"));
        obj.add("key/slash", new JsonPrimitive("slashValue"));
        obj.add("mail.props.port", new JsonPrimitive(587));
        obj.add("a-b", new JsonPrimitive("literalAMinusB"));
        obj.add("a", new JsonPrimitive(10));
        obj.add("b", new JsonPrimitive(3));

        JsonObject m1 = new JsonObject();
        m1.add("http-equiv", new JsonPrimitive("X-UA-Compatible"));
        m1.add("content", new JsonPrimitive("IE=edge"));
        JsonObject metas = new JsonObject();
        metas.add("m1", m1);

        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive("first"));
        arr.add(new JsonPrimitive("second"));
        arr.add(new JsonPrimitive("third"));

        Map<String, JsonElement> arguments = new HashMap<>();
        arguments.put("arr", arr);
        arguments.put("i", new JsonPrimitive(0));
        arguments.put("obj", obj);
        arguments.put("metas", metas);
        arguments.put("k", new JsonPrimitive("m1"));

        ArgumentsTokenValueExtractor atv = new ArgumentsTokenValueExtractor(arguments);
        return Map.of(atv.getPrefix(), atv);
    }

    @Test
    public void hyphenInQuotedKey() {

        Map<String, TokenValueExtractor> v = valuesMap();

        assertEquals("hyphenValue",
                new ExpressionEvaluator("Arguments.obj[\"key-with-hyphen\"]").evaluate(v).getAsString());
        assertEquals("hyphenValue",
                new ExpressionEvaluator("Arguments.obj['key-with-hyphen']").evaluate(v).getAsString());
    }

    @Test
    public void httpEquivTheAppDefinitionMetaAttribute() {

        Map<String, TokenValueExtractor> v = valuesMap();

        assertEquals("X-UA-Compatible",
                new ExpressionEvaluator("Arguments.metas.m1[\"http-equiv\"]").evaluate(v).getAsString());
        assertEquals("X-UA-Compatible",
                new ExpressionEvaluator("Arguments.metas['m1']['http-equiv']").evaluate(v).getAsString());
    }

    @Test
    public void spacePlusColonAndSlashInKey() {

        Map<String, TokenValueExtractor> v = valuesMap();

        assertEquals("spaceValue", new ExpressionEvaluator("Arguments.obj[\"key with space\"]").evaluate(v).getAsString());
        assertEquals("plusValue", new ExpressionEvaluator("Arguments.obj[\"key+plus\"]").evaluate(v).getAsString());
        assertEquals("colonValue", new ExpressionEvaluator("Arguments.obj[\"key:colon\"]").evaluate(v).getAsString());
        assertEquals("slashValue", new ExpressionEvaluator("Arguments.obj[\"key/slash\"]").evaluate(v).getAsString());
    }

    @Test
    public void dottedKeyStillWorks() {

        assertEquals(587,
                new ExpressionEvaluator("Arguments.obj[\"mail.props.port\"]").evaluate(valuesMap()).getAsInt());
    }

    @Test
    public void quotedKeyWinsOverTheArithmeticItLooksLike() {

        assertEquals("literalAMinusB",
                new ExpressionEvaluator("Arguments.obj[\"a-b\"]").evaluate(valuesMap()).getAsString());
    }

    @Test
    public void theSameKeyUnquotedIsArithmeticAndThrows() {

        Map<String, TokenValueExtractor> v = valuesMap();

        assertThrows(Exception.class,
                () -> new ExpressionEvaluator("Arguments.obj.key-with-hyphen").evaluate(v));
        assertThrows(Exception.class,
                () -> new ExpressionEvaluator("Arguments.metas.m1.http-equiv").evaluate(v));
    }

    /**
     * A token path inside brackets, with an operator so the bracket content is
     * treated as a sub-expression. This is the form the engine actually
     * supports; see {@link #bareTokenPathInsideBracketsDivergesFromJs()} for the
     * form that does not.
     */
    @Test
    public void tokenPathInsideBracketsWithArithmetic() {

        Map<String, TokenValueExtractor> v = valuesMap();

        assertEquals("second",
                new ExpressionEvaluator("Arguments.arr[Arguments.i + 1]").evaluate(v).getAsString());
    }

    /**
     * A BARE token path inside brackets, with no operator to force it to be
     * evaluated. This used to diverge: Java kept the raw text as the key, so an
     * object lookup silently returned null and an array lookup threw
     * "couldn't be parsed into integer", while kirun-js resolved it. The same
     * expression therefore behaved differently in a UI function and a server
     * function.
     *
     * It was hidden because existing tests such as
     * {@code Context.a[Steps.loop.iteration.index - 1]} in {@code KIRuntimeTest}
     * carry an operator, which is what made them work, and the bare form in that
     * same file is only ever a WRITE target, which goes through the setter
     * extractor instead.
     */
    @Test
    public void bareTokenPathInsideBracketsResolves() {

        Map<String, TokenValueExtractor> v = valuesMap();

        assertEquals("IE=edge",
                new ExpressionEvaluator("Arguments.metas[Arguments.k].content").evaluate(v).getAsString());
        assertEquals("X-UA-Compatible",
                new ExpressionEvaluator("Arguments.metas[Arguments.k]['http-equiv']").evaluate(v).getAsString());
        assertEquals("first",
                new ExpressionEvaluator("Arguments.arr[Arguments.i]").evaluate(v).getAsString());
    }

    /**
     * An extractor whose ROOT is the value itself, so a bracket can sit directly
     * against the prefix. The shared Arguments/Context extractors are keyed maps,
     * which cannot express that shape.
     */
    private static class RootExtractor extends TokenValueExtractor {

        private final JsonElement root;

        RootExtractor(JsonElement root) {
            this.root = root;
        }

        @Override
        protected JsonElement getValueInternal(String token) {
            String path = token.substring(getPrefix().length());
            return retrieveElementFrom(token, TokenValueExtractor.splitPath(path), 0, this.root);
        }

        @Override
        public String getPrefix() {
            return "Context.";
        }

        @Override
        public JsonElement getStore() {
            return this.root;
        }
    }

    private Map<String, TokenValueExtractor> rootMap(JsonElement root) {
        RootExtractor e = new RootExtractor(root);
        return Map.of(e.getPrefix(), e);
    }

    /**
     * A token whose FIRST separator is a bracket used to match no extractor at all:
     * the prefix is the text up to the first dot, so {@code Context[0]} produced an
     * empty prefix and fell through to the literal extractor, which throws. In the
     * UI that throw reached the root error boundary and replaced the whole page.
     * {@code Parent[0]} was the case that surfaced it, from a repeater bound to
     * ObjectEntries output, but it was never Parent-specific.
     */
    @Test
    public void rootLevelBracketAccess() {

        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive("zero"));
        arr.add(new JsonPrimitive("one"));
        arr.add(new JsonPrimitive("two"));

        Map<String, TokenValueExtractor> v = rootMap(arr);

        assertEquals("zero", new ExpressionEvaluator("Context[0]").evaluate(v).getAsString());
        assertEquals("two", new ExpressionEvaluator("Context[2]").evaluate(v).getAsString());
        // the dotted form keeps working
        assertEquals("zero", new ExpressionEvaluator("Context.0").evaluate(v).getAsString());
    }

    @Test
    public void rootLevelQuotedKeyAccess() {

        JsonObject obj = new JsonObject();
        obj.add("a.b", new JsonPrimitive("dotted"));
        obj.add("plain", new JsonPrimitive("p"));

        Map<String, TokenValueExtractor> v = rootMap(obj);

        assertEquals("dotted", new ExpressionEvaluator("Context[\"a.b\"]").evaluate(v).getAsString());
        assertEquals("p", new ExpressionEvaluator("Context[\"plain\"]").evaluate(v).getAsString());
        assertEquals("p", new ExpressionEvaluator("Context['plain']").evaluate(v).getAsString());
    }

    @Test
    public void aBracketAfterADottedPathIsUntouched() {

        JsonArray inner = new JsonArray();
        inner.add(new JsonPrimitive(1));
        inner.add(new JsonPrimitive(2));
        JsonObject obj = new JsonObject();
        obj.add("arr", inner);

        assertEquals(2, new ExpressionEvaluator("Context.arr[1]").evaluate(rootMap(obj)).getAsInt());
    }

    /**
     * A bracket path that resolves to nothing must miss quietly, the way kirun-js
     * does, rather than throwing out of the path-string building.
     */
    @Test
    public void bareTokenPathInsideBracketsThatResolvesToNothing() {

        Map<String, TokenValueExtractor> v = valuesMap();

        JsonElement result = new ExpressionEvaluator("Arguments.metas[Arguments.nosuch].content").evaluate(v);

        assertTrue(result == null || result.isJsonNull(), "expected no value, got: " + result);
    }

    /**
     * LIMITATION, asserted so that a fix surfaces as a failing test rather than
     * going unnoticed: a bracket segment is a literal or a token path, never an
     * expression. A concatenation inside brackets is taken as the key text
     * itself and silently misses. The JS engine behaves the same way.
     */
    @Test
    public void anExpressionInsideBracketsIsNotEvaluated() {

        JsonElement result = new ExpressionEvaluator("Arguments.metas['m' + '1'].content")
                .evaluate(valuesMap());

        assertTrue(result == null || result.isJsonNull() || JsonNull.INSTANCE.equals(result),
                "expected no value, got: " + result);
    }
}
