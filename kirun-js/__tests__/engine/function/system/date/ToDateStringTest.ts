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
            ['dateFormat', "YYYY.MM.DD N 'at' HH:mm:ss 'Z'"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2023.07.21 CE at 22:34:58 Z",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '-002023-07-21T17:04:58.798Z'],
            ['dateFormat', 'yyyy.MM.DD NN'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '-2023.07.21 BCE',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '+121023-07-21T17:04:58.798Z'],
            ['dateFormat', 'yyyy.MM.DD NNN'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '121023.07.21 CE',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-07-21T17:04:58.798Z'],
            ['dateFormat', 'yyyy.MM.DD NNNN'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2023.07.21 After Common Era',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '-012023-07-21T17:04:58.798Z'],
            ['dateFormat', 'yyyy.MM.DD NNNN'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '-12023.07.21 Before Common Era',
    );
});


test('tdtest', async () => {
    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.000Z'],
            ['dateFormat', "yyyy.MM.DD N 'at' HH:mm:ss Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2001.07.04 CE at 17:38:56 +05:30',
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.000Z'],
            ['dateFormat', "ddd, MMM D, ''YY"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "Wed, Jul 4, '01",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.000Z'],
            ['dateFormat', "h:mm a"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "5:38 pm",
    );
   
    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.000Z'],
            ['dateFormat', "HH 'o''clock' A, Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "17 o'clock PM, +05:30",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.000Z'],
            ['dateFormat', "h:mm A, Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "5:38 PM, +05:30",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.000Z'],
            ['dateFormat', "ddd, D MMM yyyy HH:mm:ss Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "Wed, 4 Jul 2001 17:38:56 +05:30",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.000Z'],
            ['dateFormat', "yyMMDDHHmmssZ"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "010704173856+05:30",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.235Z'],
            ['dateFormat', "yyyy-MM-DD'T'HH:mm:ss.SSSZZ"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001-07-04T17:38:56.235+0530",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-06-01T12:08:56.000Z'],
            ['dateFormat', "YYYY-'W'ww-d"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001-W22-6",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-06-01T12:08:56.000Z'],
            ['dateFormat', "YYYY----X"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001----991397336000",
    );

    
    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-06-01T12:08:56.000Z'],
            ['dateFormat', "YYYY----''x''"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001----'991397336'",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.235Z'],
            ['dateFormat', "yyyy-MM-DD'T'HH:mm:ss.SZ"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001-07-04T17:38:56.2+05:30",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.235Z'],
            ['dateFormat', "yyyy-MM-DD'T'HH:mm:ss.SSZ"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001-07-04T17:38:56.23+05:30",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.235Z'],
            ['dateFormat', "yyyy-MM-DD'T'HH:mm:ss.SSSZ"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001-07-04T17:38:56.235+05:30",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T12:08:56.235Z'],
            ['dateFormat', "yyyy-MM-DD'T'HH:mm:ss.SSSS....SSSSSZ"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001-07-04T17:38:56.0235....00235+05:30",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T13:08:56.235Z'],
            ['dateFormat', "yyyy-MM-DD'T'kk:mm:ss.SSSS....SSSSSZ"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2001-07-04T18:38:56.0235....00235+05:30",
    );
    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-07-04T13:08:56.235Z'],
            ['dateFormat', "dth"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "4th",
    );


    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-01-04T13:08:56.235Z'],
            ['dateFormat', "Qth"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "1st",
    );
    
    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-01-04T13:08:56.235Z'],
            ['dateFormat', "Mth"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "1st",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-02-23T13:08:56.235Z'],
            ['dateFormat', "DDD"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "54",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-02-23T13:08:56.235Z'],
            ['dateFormat', "dth"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "5th",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-02-23T13:08:56.235Z'],
            ['dateFormat', "dddd"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "Thursday",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-10-23T13:08:56.235Z'],
            ['dateFormat', "wth"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "43rd",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2023-10-23T13:08:56.235Z'],
            ['dateFormat', "YYYY"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "2023",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '+123444-10-23T13:08:56.235Z'],
            ['dateFormat', "Y"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "123444",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '-123444-10-23T13:08:56.235Z'],
            ['dateFormat', "YYYY"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "-123444",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '-123444-10-23T13:08:56.235Z'],
            ['dateFormat', "Y"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "-123444",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '2001-03-25T13:08:56.235Z'],
            ['dateFormat', "Dth"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "25th",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '-123456-10-23T13:08:56.235Z'],
            ['dateFormat', "YY"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "-56",
    );

    fep.setArguments(
        new Map<string, any>([
            ['isoDate', '+003456-10-23T13:08:56.235Z'],
            ['dateFormat', "YY"],
        ]),
    );
    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        "56",
    );
})
