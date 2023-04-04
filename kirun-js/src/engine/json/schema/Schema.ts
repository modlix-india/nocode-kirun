import { Namespaces } from '../../namespaces/Namespaces';
import { ArraySchemaType } from './array/ArraySchemaType';
import { StringFormat } from './string/StringFormat';
import { SchemaType } from './type/SchemaType';
import { TypeUtil } from './type/TypeUtil';
import { Type } from './type/Type';
import { isNullValue } from '../../util/NullCheck';
import { SingleType } from './type/SingleType';
import { MultipleType } from './type/MultipleType';

const ADDITIONAL_PROPERTY: string = 'additionalProperty';
const ADDITIONAL_ITEMS: string = 'additionalItems';
const ENUMS: string = 'enums';
const ITEMS_STRING: string = 'items';
const SCHEMA_ROOT_PATH: string = 'System.Schema';
const REQUIRED_STRING: string = 'required';
const VERSION_STRING: string = 'version';
const NAMESPACE_STRING: string = 'namespace';
const TEMPORARY: string = '_';

export class AdditionalType {
    private booleanValue?: boolean;
    private schemaValue?: Schema;

    constructor(apt: AdditionalType | undefined = undefined) {
        if (!apt) return;
        this.booleanValue = apt.booleanValue;
        if (!apt.schemaValue) return;
        this.schemaValue = new Schema(apt.schemaValue);
    }

    public getBooleanValue(): boolean | undefined {
        return this.booleanValue;
    }

    public getSchemaValue(): Schema | undefined {
        return this.schemaValue;
    }

    public setBooleanValue(booleanValue: boolean): AdditionalType {
        this.booleanValue = booleanValue;
        return this;
    }

    public setSchemaValue(schemaValue: Schema): AdditionalType {
        this.schemaValue = schemaValue;
        return this;
    }

    public static from(obj: any): AdditionalType | undefined {
        if (isNullValue(obj)) return undefined;
        const ad = new AdditionalType();
        if (typeof obj === 'boolean') ad.booleanValue = obj;
        else {
            let keys = Object.keys(obj);
            if (keys.indexOf('booleanValue') != -1) ad.booleanValue = obj.booleanValue;
            else if (keys.indexOf('schemaValue') != -1)
                ad.schemaValue = Schema.from(obj.schemaValue);
            else ad.schemaValue = Schema.from(obj);
        }
        return ad;
    }
}

export class Schema {
    public static readonly NULL: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName('Null')
        .setType(TypeUtil.of(SchemaType.NULL))
        .setConstant(undefined);

    private static readonly TYPE_SCHEMA: Schema = new Schema()
        .setType(TypeUtil.of(SchemaType.STRING))
        .setEnums([
            'INTEGER',
            'LONG',
            'FLOAT',
            'DOUBLE',
            'STRING',
            'OBJECT',
            'ARRAY',
            'BOOLEAN',
            'NULL',
        ]);

    public static readonly SCHEMA: Schema = new Schema()
        .setNamespace(Namespaces.SYSTEM)
        .setName('Schema')
        .setType(TypeUtil.of(SchemaType.OBJECT))
        .setProperties(
            new Map<string, Schema>([
                [
                    NAMESPACE_STRING,
                    Schema.of(NAMESPACE_STRING, SchemaType.STRING).setDefaultValue(TEMPORARY),
                ],
                ['name', Schema.ofString('name')],
                [VERSION_STRING, Schema.of(VERSION_STRING, SchemaType.INTEGER).setDefaultValue(1)],
                ['ref', Schema.ofString('ref')],
                [
                    'type',
                    new Schema().setAnyOf([
                        Schema.TYPE_SCHEMA,
                        Schema.ofArray('type', Schema.TYPE_SCHEMA),
                    ]),
                ],
                ['anyOf', Schema.ofArray('anyOf', Schema.ofRef(SCHEMA_ROOT_PATH))],
                ['allOf', Schema.ofArray('allOf', Schema.ofRef(SCHEMA_ROOT_PATH))],
                ['oneOf', Schema.ofArray('oneOf', Schema.ofRef(SCHEMA_ROOT_PATH))],

                ['not', Schema.ofRef(SCHEMA_ROOT_PATH)],
                ['title', Schema.ofString('title')],
                ['description', Schema.ofString('description')],
                ['id', Schema.ofString('id')],
                ['examples', Schema.ofAny('examples')],
                ['defaultValue', Schema.ofAny('defaultValue')],
                ['comment', Schema.ofString('comment')],
                [ENUMS, Schema.ofArray(ENUMS, Schema.ofString(ENUMS))],
                ['constant', Schema.ofAny('constant')],

                ['pattern', Schema.ofString('pattern')],
                [
                    'format',
                    Schema.of('format', SchemaType.STRING).setEnums([
                        'DATETIME',
                        'TIME',
                        'DATE',
                        'EMAIL',
                        'REGEX',
                    ]),
                ],
                ['minLength', Schema.ofInteger('minLength')],
                ['maxLength', Schema.ofInteger('maxLength')],

                ['multipleOf', Schema.ofLong('multipleOf')],
                ['minimum', Schema.ofNumber('minimum')],
                ['maximum', Schema.ofNumber('maximum')],
                ['exclusiveMinimum', Schema.ofNumber('exclusiveMinimum')],
                ['exclusiveMaximum', Schema.ofNumber('exclusiveMaximum')],

                [
                    'properties',
                    Schema.of('properties', SchemaType.OBJECT).setAdditionalProperties(
                        new AdditionalType().setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)),
                    ),
                ],
                [
                    'additionalProperties',
                    new Schema()
                        .setName(ADDITIONAL_PROPERTY)
                        .setNamespace(Namespaces.SYSTEM)
                        .setAnyOf([
                            Schema.ofBoolean(ADDITIONAL_PROPERTY),
                            Schema.ofObject(ADDITIONAL_PROPERTY).setRef(SCHEMA_ROOT_PATH),
                        ])
                        .setDefaultValue(true),
                ],
                [
                    REQUIRED_STRING,
                    Schema.ofArray(
                        REQUIRED_STRING,
                        Schema.ofString(REQUIRED_STRING),
                    ).setDefaultValue([]),
                ],
                ['propertyNames', Schema.ofRef(SCHEMA_ROOT_PATH)],
                ['minProperties', Schema.ofInteger('minProperties')],
                ['maxProperties', Schema.ofInteger('maxProperties')],
                [
                    'patternProperties',
                    Schema.of('patternProperties', SchemaType.OBJECT).setAdditionalProperties(
                        new AdditionalType().setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)),
                    ),
                ],

                [
                    ITEMS_STRING,
                    new Schema()
                        .setName(ITEMS_STRING)
                        .setAnyOf([
                            Schema.ofRef(SCHEMA_ROOT_PATH).setName('item'),
                            Schema.ofArray('tuple', Schema.ofRef(SCHEMA_ROOT_PATH)),
                        ]),
                ],

                ['contains', Schema.ofRef(SCHEMA_ROOT_PATH)],
                ['minContains', Schema.ofInteger('minContains')],
                ['maxContains', Schema.ofInteger('maxContains')],
                ['minItems', Schema.ofInteger('minItems')],
                ['maxItems', Schema.ofInteger('maxItems')],
                ['uniqueItems', Schema.ofBoolean('uniqueItems')],
                [
                    'additionalItems',
                    new Schema()
                        .setName(ADDITIONAL_ITEMS)
                        .setNamespace(Namespaces.SYSTEM)
                        .setAnyOf([
                            Schema.ofBoolean(ADDITIONAL_ITEMS),
                            Schema.ofObject(ADDITIONAL_ITEMS).setRef(SCHEMA_ROOT_PATH),
                        ]),
                ],
                [
                    '$defs',
                    Schema.of('$defs', SchemaType.OBJECT).setAdditionalProperties(
                        new AdditionalType().setSchemaValue(Schema.ofRef(SCHEMA_ROOT_PATH)),
                    ),
                ],

                ['permission', Schema.ofString('permission')],
            ]),
        )
        .setRequired([]);

    public static ofString(id: string): Schema {
        return new Schema().setType(TypeUtil.of(SchemaType.STRING)).setName(id);
    }

    public static ofInteger(id: string): Schema {
        return new Schema().setType(TypeUtil.of(SchemaType.INTEGER)).setName(id);
    }

    public static ofFloat(id: string): Schema {
        return new Schema().setType(TypeUtil.of(SchemaType.FLOAT)).setName(id);
    }

    public static ofLong(id: string): Schema {
        return new Schema().setType(TypeUtil.of(SchemaType.LONG)).setName(id);
    }

    public static ofDouble(id: string): Schema {
        return new Schema().setType(TypeUtil.of(SchemaType.DOUBLE)).setName(id);
    }

    public static ofAny(id: string): Schema {
        return new Schema()
            .setType(
                TypeUtil.of(
                    SchemaType.INTEGER,
                    SchemaType.LONG,
                    SchemaType.FLOAT,
                    SchemaType.DOUBLE,
                    SchemaType.STRING,
                    SchemaType.BOOLEAN,
                    SchemaType.ARRAY,
                    SchemaType.NULL,
                    SchemaType.OBJECT,
                ),
            )
            .setName(id);
    }

    public static ofAnyNotNull(id: string): Schema {
        return new Schema()
            .setType(
                TypeUtil.of(
                    SchemaType.INTEGER,
                    SchemaType.LONG,
                    SchemaType.FLOAT,
                    SchemaType.DOUBLE,
                    SchemaType.STRING,
                    SchemaType.BOOLEAN,
                    SchemaType.ARRAY,
                    SchemaType.OBJECT,
                ),
            )
            .setName(id);
    }

    public static ofNumber(id: string): Schema {
        return new Schema()
            .setType(
                TypeUtil.of(
                    SchemaType.INTEGER,
                    SchemaType.LONG,
                    SchemaType.FLOAT,
                    SchemaType.DOUBLE,
                ),
            )
            .setName(id);
    }

    public static ofBoolean(id: string): Schema {
        return new Schema().setType(TypeUtil.of(SchemaType.BOOLEAN)).setName(id);
    }

    public static of(id: string, ...types: SchemaType[]): Schema {
        return new Schema().setType(TypeUtil.of(...types)).setName(id);
    }

    public static ofObject(id: string): Schema {
        return new Schema().setType(TypeUtil.of(SchemaType.OBJECT)).setName(id);
    }

    public static ofRef(ref: string): Schema {
        return new Schema().setRef(ref);
    }

    public static ofArray(id: string, ...itemSchemas: Schema[]): Schema {
        return new Schema()
            .setType(TypeUtil.of(SchemaType.ARRAY))
            .setName(id)
            .setItems(ArraySchemaType.of(...itemSchemas));
    }

    public static fromListOfSchemas(list: any): Schema[] | undefined {
        if (isNullValue(list) && !Array.isArray(list)) return undefined;
        let x: Schema[] = [];
        for (let e of Array.from(list)) {
            let v = Schema.from(e);
            if (!v) continue;
            x.push(v);
        }

        return x;
    }

    public static fromMapOfSchemas(map: any): Map<string, Schema> | undefined {
        if (isNullValue(map)) return undefined;
        const retMap = new Map<string, Schema>();

        Object.entries(map).forEach(([k, v]) => {
            let value = Schema.from(v);
            if (!value) return;
            retMap.set(k, value);
        });

        return retMap;
    }

    public static from(obj: any, isStringSchema: boolean = false): Schema | undefined {
        if (isNullValue(obj)) return undefined;

        let schema: Schema = new Schema();
        schema.namespace = obj.namespace ?? TEMPORARY;
        schema.name = obj.name;

        schema.version = obj.version ?? 1;

        schema.ref = obj.ref;

        if (!isStringSchema) schema.type = TypeUtil.from(obj.type);
        else schema.type = new SingleType(SchemaType.STRING);

        schema.anyOf = Schema.fromListOfSchemas(obj.anyOf);
        schema.allOf = Schema.fromListOfSchemas(obj.allOf);
        schema.oneOf = Schema.fromListOfSchemas(obj.oneOf);
        schema.not = Schema.from(obj.not);

        schema.description = obj.description;
        schema.examples = obj.examples ? [...obj.examples] : undefined;
        schema.defaultValue = obj.defaultValue;
        schema.comment = obj.comment;
        schema.enums = obj.enums ? [...obj.enums] : undefined;
        schema.constant = obj.constant;

        // String
        schema.pattern = obj.pattern;
        schema.format = obj.format;
        schema.minLength = obj.minLength;
        schema.maxLength = obj.maxLength;

        // Number
        schema.multipleOf = obj.multipleOf;
        schema.minimum = obj.minimum;
        schema.maximum = obj.maximum;
        schema.exclusiveMinimum = obj.exclusiveMinimum;
        schema.exclusiveMaximum = obj.exclusiveMaximum;

        // Object
        schema.properties = Schema.fromMapOfSchemas(obj.properties);
        schema.additionalProperties = AdditionalType.from(obj.additionalProperties);
        schema.required = obj.required;
        schema.propertyNames = Schema.from(obj.propertyNames, true);
        schema.minProperties = obj.minProperties;
        schema.maxProperties = obj.maxProperties;
        schema.patternProperties = Schema.fromMapOfSchemas(obj.patternProperties);

        // Array
        schema.items = ArraySchemaType.from(obj.items);
        schema.additionalItems = AdditionalType.from(obj.additionalItems);
        schema.contains = Schema.from(obj.contains);
        schema.minContains = obj.minContains;
        schema.maxContains = obj.maxContains;
        schema.minItems = obj.minItems;
        schema.maxItems = obj.maxItems;
        schema.uniqueItems = obj.uniqueItems;

        schema.$defs = Schema.fromMapOfSchemas(obj.$defs);
        schema.permission = obj.permission;

        return schema;
    }

    private namespace: string = TEMPORARY;
    private name?: string;

    private version: number = 1;

    private ref?: string;

    private type?: Type;
    private anyOf?: Schema[];
    private allOf?: Schema[];
    private oneOf?: Schema[];
    private not?: Schema;

    private description?: string;
    private examples?: any[];
    private defaultValue?: any;
    private comment?: string;
    private enums?: any[];
    private constant?: any;

    // String
    private pattern?: string;
    private format?: StringFormat;
    private minLength?: number;
    private maxLength?: number;

    // Number
    private multipleOf?: number;
    private minimum?: number;
    private maximum?: number;
    private exclusiveMinimum?: number;
    private exclusiveMaximum?: number;

    // Object
    private properties?: Map<string, Schema>;
    private additionalProperties?: AdditionalType;
    private required?: string[];
    private propertyNames?: Schema;
    private minProperties?: number;
    private maxProperties?: number;
    private patternProperties?: Map<string, Schema>;

    // Array
    private items?: ArraySchemaType;
    private additionalItems?: AdditionalType;
    private contains?: Schema;
    private minContains?: number;
    private maxContains?: number;
    private minItems?: number;
    private maxItems?: number;
    private uniqueItems?: boolean;

    private $defs?: Map<string, Schema>;
    private permission?: string;

    public constructor(schema?: Schema) {
        if (!schema) return;

        this.namespace = schema.namespace;
        this.name = schema.name;

        this.version = schema.version;
        this.ref = schema.ref;

        this.type =
            schema.type instanceof SingleType
                ? new SingleType(schema.type as SingleType)
                : new MultipleType(schema.type as MultipleType);

        this.anyOf = schema.anyOf?.map((x) => new Schema(x));

        this.allOf = schema.allOf?.map((x) => new Schema(x));
        this.oneOf = schema.oneOf?.map((x) => new Schema(x));

        this.not = this.not ? new Schema(this.not) : undefined;

        this.description = schema.description;
        this.examples = schema.examples ? JSON.parse(JSON.stringify(schema.examples)) : undefined;

        this.defaultValue = schema.defaultValue
            ? JSON.parse(JSON.stringify(schema.defaultValue))
            : undefined;
        this.comment = schema.comment;
        this.enums = schema.enums ? [...schema.enums] : undefined;
        this.constant = schema.constant ? JSON.parse(JSON.stringify(schema.constant)) : undefined;

        this.pattern = schema.pattern;
        this.format = schema.format;

        this.minLength = schema.minLength;
        this.maxLength = schema.maxLength;

        this.multipleOf = schema.multipleOf;
        this.minimum = schema.minimum;
        this.maximum = schema.maximum;
        this.exclusiveMinimum = schema.exclusiveMinimum;
        this.exclusiveMaximum = schema.exclusiveMaximum;

        this.properties = schema.properties
            ? new Map(Array.from(schema.properties.entries()).map((e) => [e[0], new Schema(e[1])]))
            : undefined;

        this.additionalProperties = schema.additionalProperties
            ? new AdditionalType(schema.additionalProperties)
            : undefined;

        this.required = schema.required ? [...schema.required] : undefined;

        this.propertyNames = schema.propertyNames ? new Schema(schema.propertyNames) : undefined;
        this.minProperties = schema.minProperties;
        this.maxProperties = schema.maxProperties;

        this.patternProperties = schema.patternProperties
            ? new Map(
                  Array.from(schema.patternProperties.entries()).map((e) => [
                      e[0],
                      new Schema(e[1]),
                  ]),
              )
            : undefined;

        this.items = schema.items ? new ArraySchemaType(schema.items) : undefined;
        this.contains = schema.contains ? new Schema(this.contains) : undefined;
        this.minContains = schema.minContains;
        this.maxContains = schema.maxContains;
        this.minItems = schema.minItems;
        this.maxItems = schema.maxItems;
        this.uniqueItems = schema.uniqueItems;
        this.additionalItems = schema.additionalItems
            ? new AdditionalType(schema.additionalItems)
            : undefined;

        this.$defs = schema.$defs
            ? new Map(Array.from(schema.$defs.entries()).map((e) => [e[0], new Schema(e[1])]))
            : undefined;

        this.permission = schema.permission;
    }

    public getTitle(): string | undefined {
        if (!this.namespace || this.namespace == TEMPORARY) return this.name;

        return this.namespace + '.' + this.name;
    }

    public getFullName(): string {
        return this.namespace + '.' + this.name;
    }

    public get$defs(): Map<string, Schema> | undefined {
        return this.$defs;
    }

    public set$defs($defs: Map<string, Schema>): Schema {
        this.$defs = $defs;
        return this;
    }

    public getNamespace(): string {
        return this.namespace;
    }
    public setNamespace(namespace: string): Schema {
        this.namespace = namespace;
        return this;
    }
    public getName(): string | undefined {
        return this.name;
    }
    public setName(name: string): Schema {
        this.name = name;
        return this;
    }
    public getVersion(): number {
        return this.version;
    }
    public setVersion(version: number): Schema {
        this.version = version;
        return this;
    }
    public getRef(): string | undefined {
        return this.ref;
    }
    public setRef(ref: string): Schema {
        this.ref = ref;
        return this;
    }
    public getType(): Type | undefined {
        return this.type;
    }
    public setType(type: Type): Schema {
        this.type = type;
        return this;
    }
    public getAnyOf(): Schema[] | undefined {
        return this.anyOf;
    }
    public setAnyOf(anyOf: Schema[]): Schema {
        this.anyOf = anyOf;
        return this;
    }
    public getAllOf(): Schema[] | undefined {
        return this.allOf;
    }
    public setAllOf(allOf: Schema[]): Schema {
        this.allOf = allOf;
        return this;
    }
    public getOneOf(): Schema[] | undefined {
        return this.oneOf;
    }
    public setOneOf(oneOf: Schema[]): Schema {
        this.oneOf = oneOf;
        return this;
    }
    public getNot(): Schema | undefined {
        return this.not;
    }
    public setNot(not: Schema): Schema {
        this.not = not;
        return this;
    }
    public getDescription(): string | undefined {
        return this.description;
    }
    public setDescription(description: string): Schema {
        this.description = description;
        return this;
    }
    public getExamples(): any[] | undefined {
        return this.examples;
    }
    public setExamples(examples: any[]): Schema {
        this.examples = examples;
        return this;
    }
    public getDefaultValue(): any {
        return this.defaultValue;
    }
    public setDefaultValue(defaultValue: any): Schema {
        this.defaultValue = defaultValue;
        return this;
    }
    public getComment(): string | undefined {
        return this.comment;
    }
    public setComment(comment: string): Schema {
        this.comment = comment;
        return this;
    }
    public getEnums(): any[] | undefined {
        return this.enums;
    }
    public setEnums(enums: any[]): Schema {
        this.enums = enums;
        return this;
    }
    public getConstant(): any {
        return this.constant;
    }
    public setConstant(constant: any): Schema {
        this.constant = constant;
        return this;
    }
    public getPattern(): string | undefined {
        return this.pattern;
    }
    public setPattern(pattern: string): Schema {
        this.pattern = pattern;
        return this;
    }
    public getFormat(): StringFormat | undefined {
        return this.format;
    }
    public setFormat(format: StringFormat): Schema {
        this.format = format;
        return this;
    }
    public getMinLength(): number | undefined {
        return this.minLength;
    }
    public setMinLength(minLength: number): Schema {
        this.minLength = minLength;
        return this;
    }
    public getMaxLength(): number | undefined {
        return this.maxLength;
    }
    public setMaxLength(maxLength: number): Schema {
        this.maxLength = maxLength;
        return this;
    }
    public getMultipleOf(): number | undefined {
        return this.multipleOf;
    }
    public setMultipleOf(multipleOf: number): Schema {
        this.multipleOf = multipleOf;
        return this;
    }
    public getMinimum(): number | undefined {
        return this.minimum;
    }
    public setMinimum(minimum: number): Schema {
        this.minimum = minimum;
        return this;
    }
    public getMaximum(): number | undefined {
        return this.maximum;
    }
    public setMaximum(maximum: number): Schema {
        this.maximum = maximum;
        return this;
    }
    public getExclusiveMinimum(): number | undefined {
        return this.exclusiveMinimum;
    }
    public setExclusiveMinimum(exclusiveMinimum: number): Schema {
        this.exclusiveMinimum = exclusiveMinimum;
        return this;
    }
    public getExclusiveMaximum(): number | undefined {
        return this.exclusiveMaximum;
    }
    public setExclusiveMaximum(exclusiveMaximum: number): Schema {
        this.exclusiveMaximum = exclusiveMaximum;
        return this;
    }
    public getProperties(): Map<string, Schema> | undefined {
        return this.properties;
    }
    public setProperties(properties: Map<string, Schema>): Schema {
        this.properties = properties;
        return this;
    }
    public getAdditionalProperties(): AdditionalType | undefined {
        return this.additionalProperties;
    }
    public setAdditionalProperties(additionalProperties: AdditionalType): Schema {
        this.additionalProperties = additionalProperties;
        return this;
    }

    public getAdditionalItems(): AdditionalType | undefined {
        return this.additionalItems;
    }
    public setAdditionalItems(additionalItems: AdditionalType): Schema {
        this.additionalItems = additionalItems;
        return this;
    }

    public getRequired(): string[] | undefined {
        return this.required;
    }
    public setRequired(required: string[]): Schema {
        this.required = required;
        return this;
    }
    public getPropertyNames(): Schema | undefined {
        return this.propertyNames;
    }
    public setPropertyNames(propertyNames: Schema): Schema {
        this.propertyNames = propertyNames;
        this.propertyNames.type = new SingleType(SchemaType.STRING);
        return this;
    }
    public getMinProperties(): number | undefined {
        return this.minProperties;
    }
    public setMinProperties(minProperties: number): Schema {
        this.minProperties = minProperties;
        return this;
    }
    public getMaxProperties(): number | undefined {
        return this.maxProperties;
    }
    public setMaxProperties(maxProperties: number): Schema {
        this.maxProperties = maxProperties;
        return this;
    }
    public getPatternProperties(): Map<string, Schema> | undefined {
        return this.patternProperties;
    }
    public setPatternProperties(patternProperties: Map<string, Schema>): Schema {
        this.patternProperties = patternProperties;
        return this;
    }
    public getItems(): ArraySchemaType | undefined {
        return this.items;
    }
    public setItems(items: ArraySchemaType): Schema {
        this.items = items;
        return this;
    }
    public getContains(): Schema | undefined {
        return this.contains;
    }
    public setContains(contains: Schema): Schema {
        this.contains = contains;
        return this;
    }

    public getMinContains(): number | undefined {
        return this.minContains;
    }

    public setMinContains(minContains: number): Schema {
        this.minContains = minContains;
        return this;
    }

    public getMaxContains(): number | undefined {
        return this.maxContains;
    }

    public setMaxContains(maxContains: number): Schema {
        this.maxContains = maxContains;
        return this;
    }

    public getMinItems(): number | undefined {
        return this.minItems;
    }
    public setMinItems(minItems: number): Schema {
        this.minItems = minItems;
        return this;
    }
    public getMaxItems(): number | undefined {
        return this.maxItems;
    }
    public setMaxItems(maxItems: number): Schema {
        this.maxItems = maxItems;
        return this;
    }
    public getUniqueItems(): boolean | undefined {
        return this.uniqueItems;
    }
    public setUniqueItems(uniqueItems: boolean): Schema {
        this.uniqueItems = uniqueItems;
        return this;
    }
    public getPermission(): string | undefined {
        return this.permission;
    }
    public setPermission(permission: string): Schema {
        this.permission = permission;
        return this;
    }
}
