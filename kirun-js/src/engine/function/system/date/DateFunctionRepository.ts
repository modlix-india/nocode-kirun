import { Repository } from '../../../Repository';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Namespaces } from '../../../namespaces/Namespaces';
import { MapUtil } from '../../../util/MapUtil';
import { DateCompareUtil } from '../../../util/date/DateCompareUtil';
import isValidISO8601DateTime from '../../../util/date/isValidISODate';
import { Function } from '../../Function';
import { AbstractCompareDateFunction } from './AbstractCompareDateFunction';
import { AbstractTimeFunction } from './AbstractTimeFunction';
import {
    setDate,
    setFullYear,
    setHours,
    setMilliSeconds,
    setMinutes,
    setMonth,
    setSeconds,
} from '../../../util/date/SetDateFunctionsUtil';
import addYears from '../../../util/addYears';

const iso8601Pattern =
    /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d+)?(Z|([+-]\d{2}:\d{2}))?$/;

export class DateFunctionRepository implements Repository<Function> {
    private static readonly repoMap: Map<String, Function> = MapUtil.ofArrayEntries(
        AbstractTimeFunction.ofEntryDateAndStringWithOutputName('getDate', 'date', (inputDate) => {
            const match = isValidISO8601DateTime(inputDate);
            const date = new Date(inputDate);
            if (match && date.toString() != 'Invalid Date') {
                return date.getDate();
            }
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
        }),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName('getDay', 'day', (inputDate) => {
            return new Date(inputDate).getUTCDay();
        }),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getFullYear',
            'year',
            (inputDate) => {
                return new Date(inputDate).getUTCFullYear();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getMonth',
            'month',
            (inputDate) => {
                return new Date(inputDate).getUTCMonth();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getHours',
            'hours',
            (inputDate) => {
                let modifyString = inputDate;
                if (inputDate.length == 29 || inputDate.length == 24) {
                    modifyString = inputDate.slice(0, 23) + 'Z';
                } else {
                    modifyString = inputDate.slice(0, 26) + 'Z';
                }
                return new Date(modifyString).getUTCHours();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getMinutes',
            'minutes',
            (inputDate) => {
                let modifyString = inputDate;
                if (inputDate.length == 29 || inputDate.length == 24) {
                    modifyString = inputDate.slice(0, 23) + 'Z';
                } else {
                    modifyString = inputDate.slice(0, 26) + 'Z';
                }
                return new Date(modifyString).getUTCMinutes();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getSeconds',
            'seconds',
            (inputDate) => {
                return new Date(inputDate).getUTCSeconds();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getMilliSeconds',
            'milliSeconds',
            (inputDate) => {
                return new Date(inputDate).getUTCMilliseconds();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName('getTime', 'time', (inputDate) => {
            return new Date(inputDate).getTime();
        }),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getTimeZoneOffset',
            'timeZoneOffset',
            (inputDate) => {
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    const timezoneOffsetString = match[8];
                    if (
                        timezoneOffsetString === 'Z' ||
                        timezoneOffsetString === '+00:00' ||
                        timezoneOffsetString === '-00:00'
                    ) {
                        return 0;
                    } else if (timezoneOffsetString) {
                        const parts = timezoneOffsetString.split(':');
                        const hours = parseInt(parts[0].substring(1), 10);
                        const minutes = parseInt(parts[1], 10);
                        return parts[0][0] === '+'
                            ? -1 * (hours * 60 + minutes)
                            : 1 * (hours * 60 + minutes);
                    }
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputDate(
            'setTime',
            'timeValue',
            'time',
            (inputDate, value) => {
                let offsetString;
                let modifyString = inputDate;
                if (inputDate.length == 29 || inputDate.length == 24) {
                    offsetString = inputDate.slice(23);
                    modifyString = inputDate.slice(0, 23) + 'Z';
                } else {
                    offsetString = inputDate.slice(26);
                    modifyString = inputDate.slice(0, 26) + 'Z';
                }
                const newDate = new Date(modifyString).setTime(value);
                inputDate = new Date(newDate).toISOString();
                if (inputDate.length > 24) {
                    inputDate = inputDate.slice(0, 26) + offsetString;
                } else {
                    inputDate = inputDate.slice(0, 23) + offsetString;
                }

                return inputDate;
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setFullYear',
            'yearValue',
            'year',
            (inputDate, value) => setFullYear(inputDate, value),
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMonth',
            'monthValue',
            'month',
            (inputDate, value) => setMonth(inputDate, value),
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setDate',
            'dateValue',
            'date',
            (inputDate, value) => setDate(inputDate, value),
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setHours',
            'hoursValue',
            'hours',
            (inputDate, value) => setHours(inputDate, value),
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMinutes',
            'minutesValue',
            'minutes',
            (inputDate, value) => setMinutes(inputDate, value),
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setSeconds',
            'secondsValue',
            'seconds',
            (inputDate, value) => setSeconds(inputDate, value),
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMilliSeconds',
            'milliSecondsValue',
            'milliSeconds',
            (inputDate, value) => setMilliSeconds(inputDate, value),
        ),

        AbstractTimeFunction.ofEntryDateAndBooleanWithOutputName(
            'IsLeapYear',
            'leap',
            (inputDate) => {
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    const year = parseInt(match[1]);
                    return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractCompareDateFunction.ofEntryTwoDateAndBooleanOutput(
            'IsSame',
            (firstDate, secondDate, fields) => {
                const dateCompareUtil = new DateCompareUtil();
                return dateCompareUtil.compareFields(firstDate, secondDate, 'same', fields);
            },
        ),

        AbstractCompareDateFunction.ofEntryTwoDateAndBooleanOutput(
            'IsBefore',
            (firstDate, secondDate, fields) => {
                const dateCompareUtil = new DateCompareUtil();
                return dateCompareUtil.compareFields(firstDate, secondDate, 'before', fields);
            },
        ),

        AbstractCompareDateFunction.ofEntryTwoDateAndBooleanOutput(
            'IsAfter',
            (firstDate, secondDate, fields) => {
                const dateCompareUtil = new DateCompareUtil();
                return dateCompareUtil.compareFields(firstDate, secondDate, 'after', fields);
            },
        ),

        AbstractCompareDateFunction.ofEntryThreeDateAndBooleanOutput(
            'InBetween',
            'betweenDate',
            (firstDate, secondDate, thirdDate, fields) => {
                const dateCompareUtil = new DateCompareUtil();
                return dateCompareUtil.inBetween(firstDate, secondDate, thirdDate, fields);
            },
        ),
    );

    private static readonly filterableNames = Array.from(
        DateFunctionRepository.repoMap.values(),
    ).map((e) => e.getSignature().getFullName());

    public async find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.DATE) {
            return Promise.resolve(undefined);
        }
        return Promise.resolve(DateFunctionRepository.repoMap.get(name));
    }

    public async filter(name: string): Promise<string[]> {
        return Promise.resolve(
            DateFunctionRepository.filterableNames.filter(
                (e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1,
            ),
        );
    }
}
