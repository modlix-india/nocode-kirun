import {
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '../../../../../src';
import { FromDateString } from '../../../../../src/engine/function/system/date/FromDateString';
import { dateFromFormatttedString } from '../../../../../src/engine/util/date/DateFormatterUtil';

const dateString = new FromDateString();

let fep: FunctionExecutionParameters = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
);

test('date from string year', async () => {
    fep.setArguments(
        new Map<string, any>([
            ['date', "2001.07.04 CE at 17:38:56 +05:30"],
            ['dateFormat', "YYYY.MM.DD NN 'at' HH:mm:ss Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date( '2001-07-04T12:08:56.000Z'),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '01.07.04 17:38:56 +05:30'],
            ['dateFormat', 'YY.MM.DD HH:mm:ss Z'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date( '2001-07-04T12:08:56.000Z'),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '123456.07.04 17:38:56 +05:30'],
            ['dateFormat', 'Y.MM.DD HH:mm:ss Z'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date('+123456-07-04T12:08:56.000Z'),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '123456.07.04 17:38:56 +05:30'],
            ['dateFormat', 'Y.MM.DD HH:mm:ss Z'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date('+123456-07-04T12:08:56.000Z'),
    );
});

test('date from string month', async () => {

    fep.setArguments(
        new Map<string, any>([
            ['date', '01.jul.04 17:38:56 +05:30'],
            ['dateFormat', 'YY.MMM.DD HH:mm:ss Z'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date( '2001-07-04T12:08:56.000Z'),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '1990.January.04 17:38:56 +05:30'],
            ['dateFormat', 'Y.MMMM.DD HH:mm:ss Z'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date('1990-01-04T12:08:56.000Z'),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '2001.7.4 17:38:56 +05:30'],
            ['dateFormat', 'Y.M.D HH:mm:ss Z'],
        ]),

        
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date('2001-07-04T12:08:56.000Z'),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', "2023 11th 16th 17:38:56 Z"],
            ['dateFormat', "Y Mth Dth HH:mm:ss Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date( '2023-11-15T19:30:17.000Z'),
    );

});

test('date from string era', async () => {

    fep.setArguments(
        new Map<string, any>([
            ['date', "122001.07.04 Before Common Era at 17:38:56 +05:30"],
            ['dateFormat', "Y.MM.DD NNNN 'at' HH:mm:ss Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date( '-122001-07-04T12:08:28.000Z'),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '1990.January.04 After Common Era at 17:38:56 +05:30'],
            ['dateFormat', "Y.MMMM.DD NNNN 'at' HH:mm:ss Z"],
        ]),
    );
    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date( '1990-01-04T12:08:56.000Z'),

    );
});
    
test('date from string time', async () => {

    fep.setArguments(
        new Map<string, any>([
            ['date', "2001.07.04 7:3:5 +02:00"],
            ['dateFormat', "Y.MM.DD H:m:s Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date( '2001-07-04T05:03:05.000Z'),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '24/12/2019 15:00:00 +02:00'],
            ['dateFormat', "DD MM YYYY HH:mm:ss Z"],
        ]),
    );
    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date( '2019-12-24T13:00:00.000Z'),

    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '24/12/2019 07:00:00 +02:00'],
            ['dateFormat', "DD MM YYYY hh:mm:ss Z"],
        ]),
    );
    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date( '2019-12-24T05:00:00.000Z'),

    );

    
    fep.setArguments(
        new Map<string, any>([
            ['date', '24/12/2019 24:00:00 +02:00'],
            ['dateFormat', "DD MM YYYY kk:mm:ss Z"],
        ]),
    );
    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date( '2019-12-24T22:00:00.000Z'),

    );

    
    fep.setArguments(
        new Map<string, any>([
            ['date', '24/12/2019 15:00:00.123 +0200'],
            ['dateFormat', "DD-MM-YYYY'T'HH:mm:ss.SSS ZZ"],
        ]),
    );
    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date( '2019-12-24T13:00:00.123Z'),

    );

    fep.setArguments(
        new Map<string, any>([
            ['date', '5:38 pm' ],
            ['dateFormat', "h:mm a"],
        ]),
    );
    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date("2023-11-16T12:08:00.000Z"),

    );
 
    fep.setArguments(
        new Map<string, any>([
            ['date', "17 o'clock PM, +05:30" ],
            ['dateFormat', "HH 'o''clock' A, Z"],
        ]),
    );
    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date("2023-11-16T11:30:00.000Z"),

    );
});

   
test('date from string week', async () => {

    fep.setArguments(
        new Map<string, any>([
            ['date', "Wed, Jul 4, '01"],
            ['dateFormat', "ddd, MMM D, ''YY"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date(2001, 6, 4),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', "Wednesday, Jul 4, '01"],
            ['dateFormat', "dddd, MMM D, ''YY"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date(2001, 6, 4),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', "Wed, Jul 4, '01"],
            ['dateFormat', "ddd, MMM D, ''YY"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date(2001, 6, 4),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', "2023-03-1 12:00:00.000"],
            ['dateFormat', "YYYY-ww-d hh:mm:ss.SSS"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date("2023-01-16T06:30:00.000Z"),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', "1360013296"],
            ['dateFormat', "X"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date("1970-01-16T17:46:53.296Z"),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', "2023-03-1 12:00:00.012340"],
            ['dateFormat', "YYYY-MM-D hh:mm:ss.SSSSSS"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
        new Date("2023-03-1 12:00:00"),
       
    );

    
    fep.setArguments(
        new Map<string, any>([
            ['date', "2023-3rd-1st 01:00:00"],
            ['dateFormat', "YYYY-wth-dth"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date("2023-01-15T18:30:00.000Z"),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', "2023 4th"],
            ['dateFormat', "YYYY Qth"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date("2023-09-30T18:30:00.000Z"),
    );

    fep.setArguments(
        new Map<string, any>([
            ['date', "2023 04 +00:00"],
            ['dateFormat', "YYYY QQ Z"],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toStrictEqual(
       new Date("2023-10-01T00:00:00.000Z"),
    );

});
