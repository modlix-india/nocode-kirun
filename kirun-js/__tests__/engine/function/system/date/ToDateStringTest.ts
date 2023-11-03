import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { ToDateString } from '../../../../../src/engine/function/system/date/ToDateString';

const tds: ToDateString = new ToDateString();

const fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
);

describe('test1', () => {
    test('To simple date string', async () => {
        fep.setArguments(
            new Map([
                ['isodate', '1994-12-15T08:05:30.406Z'],
                ['format', 'yyyy-MM-DD'],
            ]),
        );

        expect((await tds.execute(fep)).allResults()[0].getResult().get('result')).toBe(
            '1994-12-15',
        );
    });

    test('To simple date string with dot separator', async () => {
        fep.setArguments(
            new Map([
                ['isodate', '1994-12-15T08:05:30.406Z'],
                ['format', 'yyyy.MM.DD'],
            ]),
        );

        expect((await tds.execute(fep)).allResults()[0].getResult().get('result')).toBe(
            '1994.12.15',
        );
    });

    test('To simple date string with dot separator', async () => {
        fep.setArguments(
            new Map([
                ['isodate', '1994-12-15T08:05:30.406Z'],
                ['format', "yyyy.MM.DD 'at'"],
            ]),
        );

        expect((await tds.execute(fep)).allResults()[0].getResult().get('result')).toBe(
            '1994.12.15 at',
        );
    });

    test('To simple date string with only year', async () => {
        fep.setArguments(
            new Map([
                ['isodate', '1994-12-15T08:05:30.406Z'],
                ['format', 'YYYY'],
            ]),
        );

        expect((await tds.execute(fep)).allResults()[0].getResult().get('result')).toBe('1994');
    });

    test('simple fail', async () => {
        fep.setArguments(
            new Map([
                ['isodate', '2023-10-22T12:23:10.123-07:00'],
                ['format', 'YYYY/MM/DD hh:mm a Z'],
            ]),
        );

        expect((await tds.execute(fep)).allResults()[0].getResult().get('result')).toBe(
            '2023/10/23 12:53 am +05:30',
        );
    });
});
