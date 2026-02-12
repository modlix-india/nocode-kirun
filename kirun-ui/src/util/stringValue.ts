import { duplicate, isNullValue, Repository, Schema, SchemaType, SchemaUtil } from '@fincity/kirun-js';

interface StringValue {
    isExpression: boolean;
    isValue: boolean;
    string?: string;
}

export function stringValue(paramValue: any): StringValue | undefined {
    if (paramValue === undefined) return undefined;

    const value = Object.values(paramValue)
        .filter((e) => !isNullValue(e))
        .sort((a: any, b: any) => (a.order ?? 0) - (b.order ?? 0))
        .reduce(
            (a: StringValue, c: any) => ({
                isExpression: a.isExpression || c.type === 'EXPRESSION',
                isValue: a.isValue || c.type === 'VALUE',
                string:
                    a.string +
                    (a.string ? '\n' : '') +
                    (c.type === 'EXPRESSION'
                        ? c.expression
                        : typeof c.value === 'object'
                          ? JSON.stringify(c.value, undefined, 2)
                          : c.value),
            }),
            { isExpression: false, isValue: false, string: '' } as StringValue,
        );

    return value;
}

export function correctStatementNames(def: any) {
    def = duplicate(def) ?? {};

    Object.keys(def?.steps ?? {}).forEach((k) => {
        if (k === def.steps[k].statementName) return;

        let x = def.steps[k];
        delete def.steps[k];
        def.steps[x.statementName] = x;
    });

    return def;
}

export async function makeObjectPaths(
    prefix: string,
    schema: Schema,
    schemaRepository: Repository<Schema>,
    set: Set<string>,
): Promise<void> {
    let s: Schema | undefined = schema;
    if (!isNullValue(schema.getRef()))
        s = await SchemaUtil.getSchemaFromRef(s, schemaRepository, schema.getRef());

    if (
        isNullValue(s) ||
        !s?.getType()?.contains(SchemaType.OBJECT) ||
        isNullValue(s.getProperties())
    )
        return;

    for (const [propName, subSchema] of Array.from(s.getProperties()!)) {
        const path = prefix + '.' + propName;
        set.add(path);
        await makeObjectPaths(path, subSchema, schemaRepository, set);
    }
}
