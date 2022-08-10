import { Namespaces } from '../../namespaces/Namespaces';
import { ArraySchemaType } from './array/ArraySchemaType';
import { AdditionalPropertiesType } from './object/AdditionalPropertiesType';
import { StringFormat } from './string/StringFormat';
import { StringSchema } from './string/StringSchema';
import { SchemaType } from './type/SchemaType';
import { TypeUtil } from './type/TypeUtil';
import { Type } from './type/Type';
import { isNullValue } from '../../util/NullCheck';

const ADDITIONAL_PROPERTY: string = 'additionalProperty';
const ENUMS: string = 'enums';
const ITEMS_STRING: string = 'items';
const SCHEMA_ROOT_PATH: string = '#/';
const REQUIRED_STRING: string = 'required';
const VERSION_STRING: string = 'version';
const NAMESPACE_STRING: string = 'namespace';
const TEMPORARY: string = '_';

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
                        new AdditionalPropertiesType().setSchemaValue(
                            Schema.ofRef(SCHEMA_ROOT_PATH),
                        ),
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
                        new AdditionalPropertiesType().setSchemaValue(
                            Schema.ofRef(SCHEMA_ROOT_PATH),
                        ),
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
                ['minItems', Schema.ofInteger('minItems')],
                ['maxItems', Schema.ofInteger('maxItems')],
                ['uniqueItems', Schema.ofBoolean('uniqueItems')],

                [
                    '$defs',
                    Schema.of('$defs', SchemaType.OBJECT).setAdditionalProperties(
                        new AdditionalPropertiesType().setSchemaValue(
                            Schema.ofRef(SCHEMA_ROOT_PATH),
                        ),
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

    public static fromListOfSchemas(list: any): Schema[] {
        return isNullValue(list) && !Array.isArray(list)
            ? undefined
            : Array.from(list).map((e) => Schema.from(e));
    }

    public static fromMapOfSchemas(map: any): Map<string, Schema> {
        if (isNullValue(map)) return undefined;
        const retMap = new Map<string, Schema>();

        Object.entries(map).forEach(([k, v]) => retMap.set(k, Schema.from(v)));

        return retMap;
    }

    public static from(obj: any): Schema {
        if (isNullValue(obj)) return undefined;

        let schema: Schema = new Schema();
        schema.namespace = obj.namespace;
        schema.name = obj.name;

        schema.version = obj.version;

        schema.ref = obj.ref;

        schema.type = TypeUtil.from(schema.type);
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
        schema.additionalProperties = AdditionalPropertiesType.from(obj.additionalProperties);
        schema.required = obj.required;
        schema.propertyNames = Schema.from(obj.propertyNames);
        schema.minProperties = obj.minProperties;
        schema.maxProperties = obj.maxProperties;
        schema.patternProperties = Schema.fromMapOfSchemas(obj.patternProperties);

        // Array
        schema.items = ArraySchemaType.from(obj.items);
        schema.contains = Schema.from(obj.contains);
        schema.minItems = obj.minItems;
        schema.maxItems = obj.maxItems;
        schema.uniqueItems = obj.uniqueItems;

        schema.$defs = Schema.fromMapOfSchemas(obj.$defs);
        schema.permission = obj.permission;

        return schema;
    }

    private namespace: string = TEMPORARY;
    private name: string;

    private version: number = 1;

    private ref: string;

    private type: Type;
    private anyOf: Schema[];
    private allOf: Schema[];
    private oneOf: Schema[];
    private not: Schema;

    private description: string;
    private examples: any[];
    private defaultValue: any;
    private comment: string;
    private enums: any[];
    private constant: any;

    // String
    private pattern: string;
    private format: StringFormat;
    private minLength: number;
    private maxLength: number;

    // Number
    private multipleOf: number;
    private minimum: number;
    private maximum: number;
    private exclusiveMinimum: number;
    private exclusiveMaximum: number;

    // Object
    private properties: Map<string, Schema>;
    private additionalProperties: AdditionalPropertiesType;
    private required: string[];
    private propertyNames: StringSchema;
    private minProperties: number;
    private maxProperties: number;
    private patternProperties: Map<string, Schema>;

    // Array
    private items: ArraySchemaType;
    private contains: Schema;
    private minItems: number;
    private maxItems: number;
    private uniqueItems: boolean;

    private $defs: Map<string, Schema>;
    private permission: string;

    public getTitle(): string {
        return this.getFullName();
    }

    private getFullName(): string {
        if (!this.namespace || this.namespace == TEMPORARY) return this.name;

        return this.namespace + '.' + this.name;
    }

    public get$defs(): Map<string, Schema> {
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
    public getName(): string {
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
    public getRef(): string {
        return this.ref;
    }
    public setRef(ref: string): Schema {
        this.ref = ref;
        return this;
    }
    public getType(): Type {
        return this.type;
    }
    public setType(type: Type): Schema {
        this.type = type;
        return this;
    }
    public getAnyOf(): Schema[] {
        return this.anyOf;
    }
    public setAnyOf(anyOf: Schema[]): Schema {
        this.anyOf = anyOf;
        return this;
    }
    public getAllOf(): Schema[] {
        return this.allOf;
    }
    public setAllOf(allOf: Schema[]): Schema {
        this.allOf = allOf;
        return this;
    }
    public getOneOf(): Schema[] {
        return this.oneOf;
    }
    public setOneOf(oneOf: Schema[]): Schema {
        this.oneOf = oneOf;
        return this;
    }
    public getNot(): Schema {
        return this.not;
    }
    public setNot(not: Schema): Schema {
        this.not = not;
        return this;
    }
    public getDescription(): string {
        return this.description;
    }
    public setDescription(description: string): Schema {
        this.description = description;
        return this;
    }
    public getExamples(): any[] {
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
    public getComment(): string {
        return this.comment;
    }
    public setComment(comment: string): Schema {
        this.comment = comment;
        return this;
    }
    public getEnums(): any[] {
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
    public getPattern(): string {
        return this.pattern;
    }
    public setPattern(pattern: string): Schema {
        this.pattern = pattern;
        return this;
    }
    public getFormat(): StringFormat {
        return this.format;
    }
    public setFormat(format: StringFormat): Schema {
        this.format = format;
        return this;
    }
    public getMinLength(): number {
        return this.minLength;
    }
    public setMinLength(minLength: number): Schema {
        this.minLength = minLength;
        return this;
    }
    public getMaxLength(): number {
        return this.maxLength;
    }
    public setMaxLength(maxLength: number): Schema {
        this.maxLength = maxLength;
        return this;
    }
    public getMultipleOf(): number {
        return this.multipleOf;
    }
    public setMultipleOf(multipleOf: number): Schema {
        this.multipleOf = multipleOf;
        return this;
    }
    public getMinimum(): number {
        return this.minimum;
    }
    public setMinimum(minimum: number): Schema {
        this.minimum = minimum;
        return this;
    }
    public getMaximum(): number {
        return this.maximum;
    }
    public setMaximum(maximum: number): Schema {
        this.maximum = maximum;
        return this;
    }
    public getExclusiveMinimum(): number {
        return this.exclusiveMinimum;
    }
    public setExclusiveMinimum(exclusiveMinimum: number): Schema {
        this.exclusiveMinimum = exclusiveMinimum;
        return this;
    }
    public getExclusiveMaximum(): number {
        return this.exclusiveMaximum;
    }
    public setExclusiveMaximum(exclusiveMaximum: number): Schema {
        this.exclusiveMaximum = exclusiveMaximum;
        return this;
    }
    public getProperties(): Map<string, Schema> {
        return this.properties;
    }
    public setProperties(properties: Map<string, Schema>): Schema {
        this.properties = properties;
        return this;
    }
    public getAdditionalProperties(): AdditionalPropertiesType {
        return this.additionalProperties;
    }
    public setAdditionalProperties(additionalProperties: AdditionalPropertiesType): Schema {
        this.additionalProperties = additionalProperties;
        return this;
    }
    public getRequired(): string[] {
        return this.required;
    }
    public setRequired(required: string[]): Schema {
        this.required = required;
        return this;
    }
    public getPropertyNames(): StringSchema {
        return this.propertyNames;
    }
    public setPropertyNames(propertyNames: StringSchema): Schema {
        this.propertyNames = propertyNames;
        return this;
    }
    public getMinProperties(): number {
        return this.minProperties;
    }
    public setMinProperties(minProperties: number): Schema {
        this.minProperties = minProperties;
        return this;
    }
    public getMaxProperties(): number {
        return this.maxProperties;
    }
    public setMaxProperties(maxProperties: number): Schema {
        this.maxProperties = maxProperties;
        return this;
    }
    public getPatternProperties(): Map<string, Schema> {
        return this.patternProperties;
    }
    public setPatternProperties(patternProperties: Map<string, Schema>): Schema {
        this.patternProperties = patternProperties;
        return this;
    }
    public getItems(): ArraySchemaType {
        return this.items;
    }
    public setItems(items: ArraySchemaType): Schema {
        this.items = items;
        return this;
    }
    public getContains(): Schema {
        return this.contains;
    }
    public setContains(contains: Schema): Schema {
        this.contains = contains;
        return this;
    }
    public getMinItems(): number {
        return this.minItems;
    }
    public setMinItems(minItems: number): Schema {
        this.minItems = minItems;
        return this;
    }
    public getMaxItems(): number {
        return this.maxItems;
    }
    public setMaxItems(maxItems: number): Schema {
        this.maxItems = maxItems;
        return this;
    }
    public getUniqueItems(): boolean {
        return this.uniqueItems;
    }
    public setUniqueItems(uniqueItems: boolean): Schema {
        this.uniqueItems = uniqueItems;
        return this;
    }
    public getPermission(): string {
        return this.permission;
    }
    public setPermission(permission: string): Schema {
        this.permission = permission;
        return this;
    }
}
