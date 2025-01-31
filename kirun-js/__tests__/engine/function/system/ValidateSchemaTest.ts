import { FunctionExecutionParameters, KIRunFunctionRepository, KIRunSchemaRepository } from '../../../../src';
import { ValidateSchema } from '../../../../src/engine/function/system/ValidateSchema';

describe('ValidateSchema', () => {
    let validator: ValidateSchema;

    beforeEach(() => {
        validator = new ValidateSchema();
    });

    test('complex object validation should pass', async () => {
        const schema = {
            name: 'lead',
            type: ['OBJECT'],
            version: 1,
            properties: {
                name: {
                    type: ['STRING'],
                    minLength: 3
                },
                mobileNumber: {
                    type: ['STRING'],
                    minLength: 3
                },
                formType: {
                    type: ['STRING'],
                    enums: ['LEAD_FORM', 'CONTACT_FORM']
                },
                email: {
                    type: ['STRING']
                },
                address: {
                    type: ['OBJECT'],
                    properties: {
                        street: {
                            type: ['STRING'],
                            minLength: 5
                        },
                        city: {
                            type: ['STRING']
                        },
                        state: {
                            type: ['STRING']
                        },
                        postalCode: {
                            type: ['STRING'],
                            pattern: '^[0-9]{6}$'
                        }
                    },
                    required: ['street', 'city', 'state']
                },
                employment: {
                    type: ['OBJECT'],
                    properties: {
                        company: {
                            type: ['STRING']
                        },
                        position: {
                            type: ['STRING']
                        },
                        experience: {
                            type: ['INTEGER'],
                            minimum: 0
                        },
                        skills: {
                            type: ['ARRAY'],
                            items: {
                                type: ['STRING']
                            },
                            minItems: 1
                        }
                    },
                    required: ['company', 'position']
                }
            },
            required: ['name', 'email', 'formType']
        };

        const source = {
            name: 'John Doe',
            mobileNumber: '9876543210',
            formType: 'LEAD_FORM',
            email: 'john.doe@example.com',
            address: {
                street: '123 Main Street',
                city: 'New York',
                state: 'NY',
                postalCode: '100001'
            },
            employment: {
                company: 'Tech Corp',
                position: 'Senior Developer',
                experience: 5,
                skills: ['Java', 'Spring', 'React']
            }
        };

        await expectValidation(schema, source, true);
    });

    test('simple string validation should pass', async () => {
        const schema = {
            type: ['STRING'],
            minLength: 3,
            maxLength: 10
        };

        await expectValidation(schema, 'Hello', true);
    });

    test('number validation should pass', async () => {
        const schema = {
            type: ['INTEGER'],
            minimum: 0,
            maximum: 100
        };

        await expectValidation(schema, 50, true);
    });

    test('simple array validation should pass', async () => {
        const schema = {
            type: ['ARRAY'],
            items: {
                type: ['STRING']
            },
            minItems: 1,
            maxItems: 3
        };

        await expectValidation(schema, ['item1', 'item2'], true);
    });

    test('validation should fail for invalid input', async () => {
        const schema = {
            type: ['STRING'],
            minLength: 5
        };

        await expectValidation(schema, 'Hi', false);
    });

    async function expectValidation(schema: any, source: any, expectedResult: boolean) {
        const context = new FunctionExecutionParameters(
            new KIRunFunctionRepository(),
            new KIRunSchemaRepository()
        ).setArguments(new Map([
            ['source', source],
            ['schema', schema]
        ]));

        const result = await validator.execute(context);
        const isValid = result.allResults()[0].getResult().get('isValid');

        expect(isValid).toBe(expectedResult);
    }
});
