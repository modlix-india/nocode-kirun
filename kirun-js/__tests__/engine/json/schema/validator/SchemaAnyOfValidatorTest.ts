import { AdditionalPropertiesType, HybridRepository } from '../../../../../src';
import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaType } from '../../../../../src/engine/json/schema/type/SchemaType';
import { TypeUtil } from '../../../../../src/engine/json/schema/type/TypeUtil';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

test('filter condition with ref schema', () => {
    let filterOperator: Schema = Schema.ofString('filterOperator')
        .setNamespace('Test')
        .setEnums(['EQUALS', 'LESS_THAN', 'GREATER_THAN', 'LESS_THAN_EQUAL']);

    let filterCondition: Schema = Schema.ofObject('filterCondition')
        .setNamespace('Test')
        .setProperties(
            new Map<string, Schema>([
                ['negate', Schema.ofBoolean('negate')],
                ['filterConditionOperator', Schema.ofRef('Test.filterOperator')],
                ['field', Schema.ofString('field')],
                ['value', Schema.ofAny('value')],
                ['toValue', Schema.ofAny('toValue')],
                ['isValue', Schema.ofBoolean('isValue')],
                ['isToValue', Schema.ofBoolean('isToValue')],
            ]),
        );

    var schemaMap = new Map<string, Schema>([
        ['filterOperator', filterOperator],
        ['filterCondition', filterCondition],
    ]);

    const repo = new HybridRepository<Schema>(
        {
            find(namespace: string, name: string): Schema | undefined {
                if (namespace !== 'Test') return undefined;
                return schemaMap.get(name);
            },
        },
        new KIRunSchemaRepository(),
    );

    var tempOb = {
        field: 'a.b.c.d',
        value: 'surendhar',
        filterConditionOperator: 'LESS_THAN',
        negate: true,
        isValue: false,
    };

    expect(SchemaValidator.validate([], filterCondition, repo, tempOb)).toBe(tempOb);
});

test('complex condition with ref schema', () => {
    let complexOperator: Schema = Schema.ofString('complexOperator')
        .setNamespace('Test')
        .setEnums(['AND', 'OR']);

    // let arraySchema: Schema = Schema.ofArray('conditions', Schema.ofRef('#'));
    let arraySchema: Schema = Schema.ofArray('conditions', Schema.ofRef('Test.complexCondition'));

    let complexCondition: Schema = Schema.ofObject('complexCondition')
        .setNamespace('Test')
        .setProperties(
            new Map<string, Schema>([
                ['negate', Schema.ofBoolean('negate')],
                ['complexConditionOperator', Schema.ofRef('Test.complexOperator')],
                ['conditions', arraySchema],
            ]),
        );

    var schemaMap = new Map<string, Schema>([
        ['complexOperator', complexOperator],
        ['complexCondition', complexCondition],
    ]);

    const repo = new HybridRepository<Schema>(
        {
            find(namespace: string, name: string): Schema | undefined {
                if (namespace !== 'Test') return undefined;
                return schemaMap.get(name);
            },
        },
        new KIRunSchemaRepository(),
    );

    let ja: any[] = [];

    let mjob = {
        conditions: [...ja],
        negate: true,
        complexConditionOperator: 'AND',
    };

    let njob = {
        conditions: [...ja],
        negate: true,
        complexConditionOperator: 'OR',
    };

    ja.push(mjob);
    ja.push(njob);

    let bjob = {
        conditions: [...ja],
        negate: true,
        complexConditionOperator: 'AND',
    };

    expect(SchemaValidator.validate([], complexCondition, repo, bjob)).toBe(bjob);
});

test('filter complex condition with ref schema', () => {
    let filterOperator: Schema = Schema.ofString('filterOperator')
        .setNamespace('Test')
        .setEnums(['EQUALS', 'LESS_THAN', 'GREATER_THAN', 'LESS_THAN_EQUAL'])
        .setDefaultValue('EQUALS');

    let filterCondition: Schema = Schema.ofObject('filterCondition')
        .setNamespace('Test')
        .setProperties(
            new Map<string, Schema>([
                ['negate', Schema.ofBoolean('negate').setDefaultValue(false)],
                ['operator', Schema.ofRef('Test.filterOperator')],
                ['field', Schema.ofString('field')],
                ['value', Schema.ofAny('value')],
                ['toValue', Schema.ofAny('toValue')],
                ['isValue', Schema.ofBoolean('isValue').setDefaultValue(false)],
                ['isToValue', Schema.ofBoolean('isToValue').setDefaultValue(false)],
            ]),
        )
        .setRequired(['operator', 'field'])
        .setAdditionalProperties(new AdditionalPropertiesType().setBooleanValue(false));

    let complexOperator: Schema = Schema.ofString('complexOperator')
        .setNamespace('Test')
        .setEnums(['AND', 'OR']);

    let arraySchema: Schema = Schema.ofArray(
        'conditions',
        new Schema().setAnyOf([Schema.ofRef('#'), Schema.ofRef('Test.FilterCondition')]),
    );

    let complexCondition: Schema = Schema.ofObject('complexCondition')
        .setNamespace('Test')
        .setProperties(
            new Map<string, Schema>([
                ['conditions', arraySchema],
                ['negate', Schema.ofBoolean('negate').setDefaultValue(false)],
                ['operator', Schema.ofRef('Test.complexOperator')],
            ]),
        )
        .setRequired(['conditions', 'operator'])
        .setAdditionalProperties(new AdditionalPropertiesType().setBooleanValue(false));

    var schemaMap = new Map<string, Schema>([
        ['filterOperator', filterOperator],
        ['filterCondition', filterCondition],
        ['complexOperator', complexOperator],
        ['complexCondition', complexCondition],
    ]);

    const repo = new HybridRepository<Schema>(
        {
            find(namespace: string, name: string): Schema | undefined {
                if (namespace !== 'Test') return undefined;
                return schemaMap.get(name);
            },
        },
        new KIRunSchemaRepository(),
    );

    var tempOb = {
        field: 'a.b.c.d',
        value: 'surendhar',
        operator: 'LESS_THAN',
        negate: true,
        isValue: false,
    };

    var tempOb1 = {
        ...tempOb,
        operator: 'GREATER_THAN',
        isValue: true,
    };

    var ja: any[] = [];

    ja.push(tempOb);
    ja.push(tempOb1);

    var mjob = {
        conditions: [],
        negate: false,
        operator: 'AND',
    };

    var bjob = {
        conditions: [mjob],
        negate: true,
        operator: 'OR',
    };

    var res = SchemaValidator.validate([], complexCondition, repo, bjob);

    expect(res).toBe(bjob);
});
