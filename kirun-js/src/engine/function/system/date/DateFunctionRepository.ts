import { Repository } from '../../../Repository';
import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Namespaces } from '../../../namespaces/Namespaces';
import { MapUtil } from '../../../util/MapUtil';
import isValidISO8601DateTime from '../../../util/date/isValidISODate';
import { Function } from '../../Function';
import { AbstractTimeFunction } from './AbstractTimeFunction';

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
                return new Date(inputDate).getUTCMonth() + 1;
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getHours',
            'hours',
            (inputDate) => {
                return new Date(inputDate).getUTCHours();
            },
        ),

        AbstractTimeFunction.ofEntryDateAndStringWithOutputName(
            'getMinutes',
            'minutes',
            (inputDate) => {
                return new Date(inputDate).getUTCMinutes();
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

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setDate',
            'dateValue',
            'date',
            (inputDate, value) => {
                const offsetString = inputDate.slice(23);
                let modifyString = inputDate.slice(0, 23) + 'Z';
                const newDate = new Date(modifyString).setUTCDate(value);
                inputDate = new Date(newDate).toISOString().slice(0, 23) + offsetString;
                console.log(inputDate);
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    return parseInt(match[3]);
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputDate(
            'setTime',
            'timeValue',
            'time',
            (inputDate, value) => {
                const offsetString = inputDate.slice(23);
                let modifyString = inputDate.slice(0, 23) + 'Z';
                const newDate = new Date(modifyString).setTime(value);
                inputDate = new Date(newDate).toISOString();
                if (inputDate.length > 24) {
                    inputDate = inputDate.slice(0, 26) + offsetString;
                } else {
                    inputDate = inputDate.slice(0, 23) + offsetString;
                }
                console.log(inputDate);
                return inputDate;
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setFullYear',
            'yearValue',
            'year',
            (inputDate, value) => {
                const offsetString = inputDate.slice(23);
                let modifyString = inputDate.slice(0, 23) + 'Z';
                const newDate = new Date(modifyString).setUTCFullYear(value);
                inputDate = new Date(newDate).toISOString();
                if (inputDate.length > 24) {
                    inputDate = inputDate.slice(0, 26) + offsetString;
                } else {
                    inputDate = inputDate.slice(0, 23) + offsetString;
                }
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    return parseInt(match[1]);
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMonth',
            'monthValue',
            'month',
            (inputDate, value) => {
                const offsetString = inputDate.slice(23);
                let modifyString = inputDate.slice(0, 23) + 'Z';
                const newDate = new Date(modifyString).setUTCMonth(value);
                inputDate = new Date(newDate).toISOString();
                if (inputDate.length > 24) {
                    inputDate = inputDate.slice(0, 26) + offsetString;
                } else {
                    inputDate = inputDate.slice(0, 23) + offsetString;
                }
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    return parseInt(match[2]);
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setHours',
            'hoursValue',
            'hours',
            (inputDate, value) => {
                const offsetString = inputDate.slice(23);
                let modifyString = inputDate.slice(0, 23) + 'Z';
                const newDate = new Date(modifyString).setUTCHours(value);
                inputDate = new Date(newDate).toISOString();
                if (inputDate.length > 24) {
                    inputDate = inputDate.slice(0, 26) + offsetString;
                } else {
                    inputDate = inputDate.slice(0, 23) + offsetString;
                }
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    return parseInt(match[4]);
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMinutes',
            'minutesValue',
            'minutes',
            (inputDate, value) => {
                const offsetString = inputDate.slice(23);
                let modifyString = inputDate.slice(0, 23) + 'Z';
                const newDate = new Date(modifyString).setUTCMinutes(value);
                inputDate = new Date(newDate).toISOString();
                if (inputDate.length > 24) {
                    inputDate = inputDate.slice(0, 26) + offsetString;
                } else {
                    inputDate = inputDate.slice(0, 23) + offsetString;
                }
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    return parseInt(match[5]);
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setSeconds',
            'secondsValue',
            'seconds',
            (inputDate, value) => {
                const offsetString = inputDate.slice(23);
                let modifyString = inputDate.slice(0, 23) + 'Z';
                const newDate = new Date(modifyString).setUTCSeconds(value);
                inputDate = new Date(newDate).toISOString();
                if (inputDate.length > 24) {
                    inputDate = inputDate.slice(0, 26) + offsetString;
                } else {
                    inputDate = inputDate.slice(0, 23) + offsetString;
                }
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    return parseInt(match[6]);
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
            },
        ),

        AbstractTimeFunction.ofEntryDateAndIntegerWithOutputInteger(
            'setMilliSeconds',
            'milliSecondsValue',
            'milliSeconds',
            (inputDate, value) => {
                const offsetString = inputDate.slice(23);
                let modifyString = inputDate.slice(0, 23) + 'Z';
                const newDate = new Date(modifyString).setUTCMilliseconds(value);
                inputDate = new Date(newDate).toISOString();
                if (inputDate.length > 24) {
                    inputDate = inputDate.slice(0, 26) + offsetString;
                } else {
                    inputDate = inputDate.slice(0, 23) + offsetString;
                }
                const match = inputDate.match(iso8601Pattern);
                if (match) {
                    return parseInt(match[7].slice(1));
                }
                throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
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
