import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    ArgumentsTokenValueExtractor,
} from '../../../../../src';
import { ObjectMake } from '../../../../../src/engine/function/system/object/ObjectMake';

const objectMake: ObjectMake = new ObjectMake();

test('testConvertStringToPhoneNumberWithCountryCode', async () => {
    const phoneNumberString = '+911234567890';
    const source = phoneNumberString;

    const resultStruct = {
        number: '{{Arguments.source}}',
        countryCode: '{{Arguments.source[1..4]}}',
    };

    const argumentsExtractor = new ArgumentsTokenValueExtractor(
        new Map<string, any>([
            ['source', source],
            ['resultStruct', resultStruct],
        ]),
    );

    const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, any>([
            ['source', source],
            ['resultStruct', resultStruct],
        ]),
    );
    fep.addTokenValueExtractor(argumentsExtractor);
    fep.setContext(new Map());
    fep.setSteps(new Map());

    const result = await objectMake.execute(fep);
    const phoneNumberObj = result.allResults()[0]?.getResult()?.get('value');

    expect(phoneNumberObj).toBeDefined();
    expect(typeof phoneNumberObj).toBe('object');
    expect(phoneNumberObj).toHaveProperty('number');
    expect(phoneNumberObj?.number).toBe(phoneNumberString);

    expect(phoneNumberObj).toHaveProperty('countryCode');
    const extractedCountryCode = phoneNumberObj?.countryCode as string;
    expect(extractedCountryCode.length).toBeLessThanOrEqual(3);
    expect(extractedCountryCode).toMatch(/^\d+$/);
    expect(extractedCountryCode).toBe('911');
});

