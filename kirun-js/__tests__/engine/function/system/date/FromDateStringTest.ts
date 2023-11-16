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

test('date from string', async () => {
    fep.setArguments(
        new Map<string, any>([
            ['date', '2001.07.04 CE at 17:38:56 +05:30'],
            ['dateFormat', 'yyyy.MM.DD N "at" HH:mm:ss Z'],
        ]),
    );

    expect((await dateString.execute(fep)).allResults()[0].getResult().get('result')).toBe(
        '2001-07-04T12:08:56.000Z',
    );
});

// describe('dateFromFormatttedString', () => {
//     it('should return a date for "yyyy.MM.DD N \'at\' HH:mm:ss z"', () => {
//         const date = dateFromFormatttedString(
//             '2001.07.04 CE at 17:38:56 +05:30',
//             "yyyy.MM.DD N 'at' HH:mm:ss Z",
//         );
//         expect(date).toEqual(new Date('2001-07-04T12:08:56.000Z'));
//     });

//     it('should return a date for "ddd, MMM D, \'\'YY"', () => {
//         const date = dateFromFormatttedString("Wed, Jul 4, '01", "ddd, MMM D, ''YY");
//         expect(date).toEqual(new Date(2001, 6, 4));
//     });

//     it('should return a date for "h:mm a"', () => {
//         const date = dateFromFormatttedString('5:38 pm', 'h:mm a');
//         const nowDate = new Date();
//         expect(date).toEqual(
//             new Date(nowDate.getFullYear(), nowDate.getMonth(), nowDate.getDate(), 17, 38),
//         );
//     });

//     it("should return a date for \"HH 'o''clock' A, Z\"", () => {
//         const date = dateFromFormatttedString("17 o'clock PM, +05:30", "HH 'o''clock' A, Z");
//         const nowDate = new Date();
//         expect(date).toEqual(
//             new Date(nowDate.getFullYear(), nowDate.getMonth(), nowDate.getDate(), 17),
//         );
//     });

//     // // "12-25-1995", "MM-DD-YYYY"
//     it('should return a date for "12-25-1995"', () => {
//         const date = dateFromFormatttedString('12-25-1995', 'MM-DD-YYYY');
//         expect(date).toEqual(new Date(1995, 11, 25));
//     });

//     // // "12/25/1995", "MM-DD-YYYY"
//     it('should return a date for "12/25/1995"', () => {
//         const date = dateFromFormatttedString('12/25/1995', 'MM-DD-YYYY');
//         expect(date).toEqual(new Date(1995, 11, 25));
//     });

//     // //'24/12/2019 09:15:00', "DD MM YYYY hh:mm:ss"
//     it('should return a date for "24/12/2019 09:15:00"', () => {
//         const date = dateFromFormatttedString('24/12/2019 09:15:00', 'DD MM YYYY hh:mm:ss');
//         expect(date).toEqual(new Date(2019, 11, 24, 9, 15, 0));
//     });

//     //"2010-10-20 4:30 +0000", "YYYY-MM-DD HH:mm Z"
//     it('should return a date for "2010-10-20 04:30 +00:00"', () => {
//         const date = dateFromFormatttedString('2010-10-20 04:30 +00:00', 'YYYY-MM-DD HH:mm Z');
//         console.log(new Date(2010, 9, 20, 10, 0).toJSON());
//         expect(date).toEqual(new Date(2010, 9, 20, 10, 0));
//     });

//     //"2023-02-12 15:00 -0800", "YYYY-MM-DD HH:mm ZZ"
//     it('should return a date for "2023-02-12 15:00 -0800"', () => {
//         const date = dateFromFormatttedString('2023-02-12 15:00 -0800', 'YYYY-MM-DD HH:mm ZZ');
//         expect(date).toEqual(new Date(2023, 1, 13, 4, 30, 0));
//     });

//     //"2023-10-22 12:23 am -07:00", "YYYY-MM-DD hh:mm a Z"
//     it('should return a date for "2023-10-22 12:23 am -07:00"', () => {
//         const date = dateFromFormatttedString('2023-10-22 12:23 am -07:00', 'YYYY-MM-DD hh:mm a Z');
//         expect(date).toEqual(new Date(2023, 9, 22, 12, 53, 0));
//     });

//     //"2023-10-22 12:23 pm", "YYYY-MM-DD hh:mm a"
// });
