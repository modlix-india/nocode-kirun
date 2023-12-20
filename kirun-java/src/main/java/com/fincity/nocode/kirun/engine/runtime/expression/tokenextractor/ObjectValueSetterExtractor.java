package com.fincity.nocode.kirun.engine.runtime.expression.tokenextractor;

import java.util.LinkedList;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.runtime.expression.Expression;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionToken;
import com.fincity.nocode.kirun.engine.runtime.expression.ExpressionTokenValue;
import com.fincity.nocode.kirun.engine.runtime.expression.Operation;
import com.fincity.nocode.kirun.engine.util.string.StringFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ObjectValueSetterExtractor extends TokenValueExtractor {

    private JsonElement store;
    private String prefix;

    public ObjectValueSetterExtractor(JsonElement store, String prefix) {
        super();
        this.store = store;
        this.prefix = prefix;
    }

    public JsonElement getStore() {
        return this.store;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    public void setStore(JsonElement store) {
        this.store = store;
    }

    public void setValue(String token, JsonElement value, Boolean overwrite, Boolean deleteOnNull) {
  
        this.store = store.deepCopy();
        this.modifyStore(token, value, overwrite, deleteOnNull);
    }

    @Override
    protected JsonElement getValueInternal(String token) {

        String[] parts = token.split(TokenValueExtractor.REGEX_DOT);
        return this.retrieveElementFrom(token, parts, 1, getStore());

    }

    private void modifyStore(String stringToken, JsonElement value, Boolean overwrite, Boolean deleteOnNull) {

        overwrite = overwrite != null ? overwrite : true;
        deleteOnNull = deleteOnNull != null ? deleteOnNull : false;
        Expression exp = new Expression(stringToken);
        LinkedList<ExpressionToken> tokens = exp.getTokens();
        tokens.removeLast();
        LinkedList<Operation> ops = exp.getOperations();
        Operation op = ops.removeLast();
        ExpressionToken token = !tokens.isEmpty() ? tokens.removeLast() : new ExpressionToken("");   // made change here
        String mem = token instanceof ExpressionTokenValue tokenExp
                ? tokenExp.getElement().getAsString()
                : token.getExpression();
        JsonElement el = this.store;

        while (!ops.isEmpty()) {
            if (op == Operation.OBJECT_OPERATOR) {
                el = this.getDataFromObject(el, mem, ops.peekLast());
            } else {
                el = this.getDataFromArray(el, mem, ops.peekLast());
            }

            op = ops.removeLast();
            token = tokens.removeLast();
            mem = token instanceof ExpressionTokenValue tokenExp
                    ? tokenExp.getElement().getAsString()
                    : token.getExpression();
        }

        if (op == Operation.OBJECT_OPERATOR)
            this.putDataInObject(el, mem, value, overwrite, deleteOnNull);
        else
            this.putDataInArray(el, mem, value, overwrite, deleteOnNull);

    }

    private JsonElement getDataFromArray(JsonElement el, String mem, Operation nextOp) {

        if (!el.isJsonArray())
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an array but found $", el));

        int index;

        try {
            index = Integer.valueOf(mem);
        } catch (Exception e) {
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an array index but found $", mem));

        }

        if (index < 0)
            throw new KIRuntimeException(
                    StringFormatter.format("Array index is out of bound - $", mem));

        JsonArray arr = el.getAsJsonArray();

        JsonElement je = arr.get(index);

        if (je.isJsonNull()) {                                                                     //here//
            je = nextOp == Operation.OBJECT_OPERATOR ? new JsonObject() : new JsonArray();
            el.getAsJsonArray().set(index, je);
        }

        return je;
    }

    private JsonElement getDataFromObject(JsonElement el, String mem, Operation nextOp) {

        if (el.isJsonArray() || !el.isJsonObject())
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an object but found $", el));

        JsonElement je = el.getAsJsonObject().get(mem);

        if (je.isJsonNull()) {
            je = nextOp == Operation.OBJECT_OPERATOR ? new JsonObject() : new JsonArray();
            el.getAsJsonObject().add(mem, je);
        }
        return je;
    }

    private void putDataInArray(JsonElement el, String mem, JsonElement value, Boolean overwrite,
            Boolean deleteOnNull) {

        if (!el.isJsonArray())
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an array but found $", el));

        int index;

        try {
            index = Integer.valueOf(mem);
        } catch (Exception e) {
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an array index but found $", mem));
        }

        if (index < 0)
            throw new KIRuntimeException(
                    StringFormatter.format("Array index is out of bound - $", mem));

        if (Boolean.TRUE.equals(overwrite)
                || ((el.getAsJsonArray().get(index) == null) || (el.getAsJsonArray().get(index)).isJsonNull())) {
            if (Boolean.TRUE.equals(deleteOnNull) && value.isJsonNull()) {
                el.getAsJsonArray().remove(index);
            }

            else
                el.getAsJsonArray().set(index, value);

        }

    }

    private void putDataInObject(JsonElement el, String mem, JsonElement value, Boolean overwrite,
            Boolean deleteOnNull) {

        if (el.isJsonArray() || !el.isJsonObject())
            throw new KIRuntimeException(
                    StringFormatter.format("Expected an object but found $", el));

        if (Boolean.TRUE.equals(overwrite)
                || (el.getAsJsonObject().get(mem) == null || (el.getAsJsonObject().get(mem)).isJsonNull())) {
            if (Boolean.TRUE.equals(deleteOnNull) && (value == null || value.isJsonNull())) {
                el.getAsJsonObject().remove(mem);
            }

            else
                el.getAsJsonObject().add(mem, value);

        }
    }
}
