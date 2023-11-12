import { ToDateString } from '../../../../../src/engine/function/system/date/ToDateString';
import { KIRunFunctionRepository } from '../../../../../src/engine/repository/KIRunFunctionRepository';
import { KIRunSchemaRepository } from '../../../../../src/engine/repository/KIRunSchemaRepository';
import { FunctionExecutionParameters } from '../../../../../src/engine/runtime/FunctionExecutionParameters';

const dateString = new ToDateString();

let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
);

test('test1 IsSameFunction', async () => {
    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-10-09T17:14:21.798Z'],
            ['dateFormat', 'yyyy-MM-DD'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2023-10-09',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-10-09T17:14:21.798Z'],
            ['dateFormat', 'yyyy.MM.DD'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2023.10.09',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-10-09T17:14:21.798Z'],
            ['dateFormat', 'yyyy.MM/DD'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2023.10/09',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-07-21T17:04:58.798Z'],
            ['dateFormat', "yyyy.MM.DD 'at' HH:mm:ss z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2023.07.21 at 22:34:58 z',
    );
});

test('Era test', async () => {
    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-07-21T17:04:58.798Z'],
            ['dateFormat', "yyyy.MM.DD N 'at' HH:mm:ss z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2023.07.21 AD at 22:34:58 z',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '-002023-07-21T17:04:58.798Z'],
            ['dateFormat', 'yyyy.MM.DD NN'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '-2023.07.21 BC',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '+121023-07-21T17:04:58.798Z'],
            ['dateFormat', 'yyyy.MM.DD NNN'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '121023.07.21 AD',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-07-21T17:04:58.798Z'],
            ['dateFormat', 'yyyy.MM.DD NNNN'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2023.07.21 Anno Domini',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '-012023-07-21T17:04:58.798Z'],
            ['dateFormat', 'yyyy.MM.DD NNNN'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '-12023.07.21 Before Christ',
    );
});
