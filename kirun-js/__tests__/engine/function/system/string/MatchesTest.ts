import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { Matches } from '../../../../../src/engine/function/system/string/Matches';

const matchesF = new Matches();

test('simple matches test', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string>([
            ['regex', '(\\d{2}).(\\d{2}).(\\d{4})'],
            ['string', '10.12.1222'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeTruthy();

    fep.setArguments(
        new Map<string, string>([
            ['regex', '(\\d{2}).(\\d{2}).(\\d{4})$'],
            ['string', '10.12.1222 '],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeFalsy();

    fep.setArguments(
        new Map<string, string>([
            ['regex', '(\\d{2}).(\\d{2}).(\\d{4})'],
            ['string', 'fdsgjhg10.12.122 2'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeFalsy();
});

test('simple name test', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string>([
            ['regex', '(\\w+),\\s(Mr|Ms|Mrs|Dr)\\.\\s?(\\w+)'],
            ['string', 'smith, Mr.John'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeTruthy();

    fep.setArguments(
        new Map<string, string>([
            ['regex', '(\\w+),\\s(Mr|Ms|Mrs|Dr)\\.\\s?(\\w+)'],
            ['string', 'How are you doing smith, Mr.John??'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeTruthy();

    fep.setArguments(
        new Map<string, string>([
            ['regex', '$(\\w+),\\s(Mr|Ms|Mrs|Dr)\\.\\s?(\\w+)$'],
            ['string', 'smith, Mr.Johnadf'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeFalsy();
});

test('simple time test', async () => {
    let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
        new KIRunFunctionRepository(),
        new KIRunSchemaRepository(),
    );

    fep.setArguments(
        new Map<string, string>([
            ['regex', '^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$'],
            ['string', '03:33:33'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeTruthy();

    fep.setArguments(
        new Map<string, string>([
            ['regex', '([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?$'],
            ['string', 'How are you d 03:33:33oing smith, Mr.John??'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeFalsy();

    fep.setArguments(
        new Map<string, string>([
            ['regex', '([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?'],
            ['string', 'surendhar12:12:12-02:54asd'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeTruthy();
    fep.setArguments(
        new Map<string, string>([
            ['regex', '^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?([+-][01][0-9]:[0-5][0-9])?'],
            ['string', 'surendhar12:12:12-02:54asd'],
        ]),
    );

    expect((await matchesF.execute(fep)).allResults()[0].getResult().get('result')).toBeFalsy();
});
