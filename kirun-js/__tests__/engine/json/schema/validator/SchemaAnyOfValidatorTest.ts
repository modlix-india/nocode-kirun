import { AdditionalType, ArraySchemaType, HybridRepository } from '../../../../../src';
import { Schema } from '../../../../../src/engine/json/schema/Schema';
import { SchemaType } from '../../../../../src/engine/json/schema/type/SchemaType';
import { TypeUtil } from '../../../../../src/engine/json/schema/type/TypeUtil';
import { SchemaValidationException } from '../../../../../src/engine/json/schema/validator/exception/SchemaValidationException';
import { SchemaValidator } from '../../../../../src/engine/json/schema/validator/SchemaValidator';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';

test('filter condition with ref schema', async () => {
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
                [
                    'multiValue',
                    Schema.ofArray('multiValue').setItems(
                        new ArraySchemaType().setSingleSchema(Schema.ofAny('singleType')),
                    ),
                ],
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
            async find(namespace: string, name: string): Promise<Schema | undefined> {
                if (namespace !== 'Test') return undefined;
                return schemaMap.get(name);
            },

            async filter(name: string): Promise<string[]> {
                return Array.from(schemaMap.values())
                    .filter((e) => e.getFullName().toLowerCase().indexOf(name.toLowerCase()) !== -1)
                    .map((e) => e.getFullName());
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

    expect(
        await SchemaValidator.validate([], Schema.ofRef('Test.filterCondition'), repo, tempOb),
    ).toBe(tempOb);
});

// test('complex condition with ref schema', async () => {
//     let complexOperator: Schema = Schema.ofString('complexOperator')
//         .setNamespace('Test')
//         .setEnums(['AND', 'OR']);

//     // let arraySchema: Schema = Schema.ofArray('conditions', Schema.ofRef('#'));
//     let arraySchema: Schema = Schema.ofArray('conditions', Schema.ofRef('Test.complexCondition'));

//     let complexCondition: Schema = Schema.ofObject('complexCondition')
//         .setNamespace('Test')
//         .setProperties(
//             new Map<string, Schema>([
//                 ['negate', Schema.ofBoolean('negate')],
//                 ['complexConditionOperator', Schema.ofRef('Test.complexOperator')],
//                 ['conditions', arraySchema],
//             ]),
//         );

//     var schemaMap = new Map<string, Schema>([
//         ['complexOperator', complexOperator],
//         ['complexCondition', complexCondition],
//     ]);

//     const repo = new HybridRepository<Schema>(
//         {
//             async find(namespace: string, name: string): Promise<Schema | undefined> {
//                 if (namespace !== 'Test') return undefined;
//                 return schemaMap.get(name);
//             },
//             async filter(name: string): Promise<string[]> {
//                 return Array.from(schemaMap.values())
//                     .filter((e) => e.getFullName().toLowerCase().indexOf(name.toLowerCase()) !== -1)
//                     .map((e) => e.getFullName());
//             },
//         },
//         new KIRunSchemaRepository(),
//     );

//     let ja: any[] = [];

//     let mjob = {
//         conditions: [...ja],
//         negate: true,
//         complexConditionOperator: 'AND',
//     };

//     let njob = {
//         conditions: [...ja],
//         negate: true,
//         complexConditionOperator: 'OR',
//     };

//     ja.push(mjob);
//     ja.push(njob);

//     let bjob = {
//         conditions: [...ja],
//         negate: true,
//         complexConditionOperator: 'AND',
//     };

//     expect(await SchemaValidator.validate([], complexCondition, repo, bjob)).toBe(bjob);
// });

// test('filter complex condition with ref schema', async () => {
//     let filterOperator: Schema = Schema.ofString('filterOperator')
//         .setNamespace('Test')
//         .setEnums(['EQUALS', 'LESS_THAN', 'GREATER_THAN', 'LESS_THAN_EQUAL', 'IN'])
//         .setDefaultValue('EQUALS');

//     let filterCondition: Schema = Schema.ofObject('filterCondition')
//         .setNamespace('Test')
//         .setProperties(
//             new Map<string, Schema>([
//                 ['negate', Schema.ofBoolean('negate').setDefaultValue(false)],
//                 ['operator', Schema.ofRef('Test.filterOperator')],
//                 ['field', Schema.ofString('field')],
//                 ['value', Schema.ofAny('value')],
//                 ['toValue', Schema.ofAny('toValue')],
//                 [
//                     'multiValue',
//                     Schema.ofArray('multiValue').setItems(
//                         new ArraySchemaType().setSingleSchema(Schema.ofAny('singleType')),
//                     ),
//                 ],
//                 ['isValue', Schema.ofBoolean('isValue').setDefaultValue(false)],
//                 ['isToValue', Schema.ofBoolean('isToValue').setDefaultValue(false)],
//             ]),
//         )
//         .setRequired(['operator', 'field'])
//         .setAdditionalProperties(new AdditionalType().setBooleanValue(false));

//     let complexOperator: Schema = Schema.ofString('complexOperator')
//         .setNamespace('Test')
//         .setEnums(['AND', 'OR']);

//     let arraySchema: Schema = Schema.ofArray(
//         'conditions',
//         new Schema().setAnyOf([Schema.ofRef('#'), Schema.ofRef('Test.FilterCondition')]),
//     );

//     let complexCondition: Schema = Schema.ofObject('complexCondition')
//         .setNamespace('Test')
//         .setProperties(
//             new Map<string, Schema>([
//                 ['conditions', arraySchema],
//                 ['negate', Schema.ofBoolean('negate').setDefaultValue(false)],
//                 ['operator', Schema.ofRef('Test.complexOperator')],
//             ]),
//         )
//         .setRequired(['conditions', 'operator'])
//         .setAdditionalProperties(new AdditionalType().setBooleanValue(false));

//     var schemaMap = new Map<string, Schema>([
//         ['filterOperator', filterOperator],
//         ['filterCondition', filterCondition],
//         ['complexOperator', complexOperator],
//         ['complexCondition', complexCondition],
//     ]);

//     const repo = new HybridRepository<Schema>(
//         {
//             async find(namespace: string, name: string): Promise<Schema | undefined> {
//                 if (namespace !== 'Test') return undefined;
//                 return schemaMap.get(name);
//             },

//             async filter(name: string): Promise<string[]> {
//                 return Array.from(schemaMap.values())
//                     .filter((e) => e.getFullName().toLowerCase().indexOf(name.toLowerCase()) !== -1)
//                     .map((e) => e.getFullName());
//             },
//         },
//         new KIRunSchemaRepository(),
//     );

//     var tempOb = {
//         field: 'a.b.c.d',
//         value: 'surendhar',
//         operator: 'LESS_THAN',
//         negate: true,
//         isValue: false,
//     };

//     var tempOb1 = {
//         ...tempOb,
//         operator: 'GREATER_THAN',
//         isValue: true,
//     };

//     var tempOb2 = {
//         field: 'k.lm.mno',
//         operator: 'IN',
//         multiValue: [1, 2, 3, 4, 5, 5, 6, 7],
//         negate: true,
//     };
//     var ja: any[] = [];

//     ja.push(tempOb);
//     ja.push(tempOb1);
//     ja.push(tempOb2);

//     var mjob = {
//         conditions: [],
//         negate: false,
//         operator: 'AND',
//     };

//     var bjob = {
//         conditions: [mjob],
//         negate: true,
//         operator: 'OR',
//     };

//     var res = await SchemaValidator.validate([], complexCondition, repo, bjob);

//     expect(res).toBe(bjob);
// });
