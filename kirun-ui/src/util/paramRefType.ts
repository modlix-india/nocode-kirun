/**
 * A ParameterReference's `type`, normalised to the string the schema declares.
 *
 * `ParameterReference.SCHEMA` in kirun-js types `type` as a string with enums
 * EXPRESSION | VALUE, but a large amount of stored data carries it as a
 * single-element ARRAY, `["EXPRESSION"]` — that is what modlix-mcp and the
 * appbuilder generator tools have always written. The runtime never minded,
 * because `KIRuntime` compares with `==` and `['EXPRESSION'] == 'EXPRESSION'` is
 * true in JS, so such definitions run correctly and arrive here intact.
 *
 * This editor compared with `===`, and lost three ways:
 *   - `ParamEditor` rendered the reference as an empty VALUE box with neither
 *     toggle pill active, and wrote `{type: 'VALUE', value: null}` back on the
 *     next change, destroying the expression.
 *   - `stringValue` reported the step as neither expression nor value and
 *     previewed `null` on the graph node.
 *   - `ExecutionGraphLines` drew no dependency line for it.
 *
 * Read a reference's type through this, never directly. An unrecognised value
 * comes back undefined rather than being guessed at.
 */
export function paramRefType(type: any): 'EXPRESSION' | 'VALUE' | undefined {
    const value = Array.isArray(type) ? type[0] : type;
    return value === 'EXPRESSION' || value === 'VALUE' ? value : undefined;
}

/**
 * Normalise `type` in place on a list of references being loaded for editing.
 *
 * The editor writes back whatever object it is holding, so normalising on the
 * way in is what stops a stored array type from being persisted as
 * `{type: 'VALUE', value: null}` after any edit — and heals the stored shape on
 * the next save. A type this cannot recognise is left exactly as it was rather
 * than being overwritten with a guess.
 */
export function normalizeParamRefTypes<T>(refs: T[]): T[] {
    refs?.forEach((ref: any) => {
        if (!ref) return;
        const type = paramRefType(ref.type);
        if (type) ref.type = type;
    });
    return refs;
}
