import { KIRuntimeException } from '../../exception/KIRuntimeException';
import isLeapYear from './isLeapYear';

const iso8601Pattern =
    /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d{3})?(Z|([+-]([01]\d|2[0-3]):([0-5]\d)))?$/;

const monthArray = ['31', '28', '31', '30', '31', '30', '31', '31', '30', '31', '30', '31'];

export class AdjustTimestamp {
    public getStartWithGivenField(input: string, fieldName: string): string {
        const match = input.match(iso8601Pattern);
        if (match) {
            switch (fieldName) {
                case 'year':
                    return `${match[1]}-01-01T00:00:00.000` + match[8];

                case 'month':
                    return `${match[1]}-${match[2]}-01T00:00:00.000` + match[8];

                case 'quarter':
                    if (parseInt(match[2]) <= 3) {
                        return (
                            `${match[1]}-01-${match[3]}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }
                    if (parseInt(match[2]) <= 6) {
                        return (
                            `${match[1]}-04-${match[3]}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }
                    if (parseInt(match[2]) <= 9) {
                        return (
                            `${match[1]}-07-${match[3]}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }
                    if (parseInt(match[2]) <= 12) {
                        return (
                            `${match[1]}-10-${match[3]}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }

                case 'week':
                    const currDay = new Date(
                        parseInt(match[1]),
                        parseInt(match[2]) - 1,
                        parseInt(match[3]),
                    ).getDay();

                    const endDate = parseInt(match[3]) - currDay;

                    if (endDate < 0) {
                        return (
                            `${match[1]}-${(parseInt(match[2]) - 1).toString().padStart(2, '0')}-${(
                                parseInt(monthArray[parseInt(match[2])-2]) + endDate
                            )
                                .toString()
                                .padStart(2, '0')}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }

                    return (
                        `${match[1]}-${match[2]}-${endDate.toString().padStart(2, '0')}T${
                            match[4]
                        }:${match[5]}:${match[6]}${match[7]}` + match[8]
                    );

                case 'date':
                    return `${match[1]}-${match[2]}-${match[3]}T00:00:00.000` + match[8];

                case 'hour':
                    return `${match[1]}-${match[2]}-${match[3]}T${match[4]}:00:00.000` + match[8];

                case 'minute':
                    return (
                        `${match[1]}-${match[2]}-${match[3]}T${match[4]}:${match[5]}:00.000` +
                        match[8]
                    );

                case 'second':
                    return (
                        `${match[1]}-${match[2]}-${match[3]}T${match[4]}:${match[5]}:${match[6]}.000` +
                        match[8]
                    );

                default:
                    return input;
            }
        }
        throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
    }

    public getEndWithGivenField(input: string, fieldName: string): string {
        const match = input.match(iso8601Pattern);
        if (match) {
            if (isLeapYear(input)) {
                monthArray[1] = '29';
            } else {
                monthArray[1] = '28';
            }
            switch (fieldName) {
                case 'year':
                    return `${match[1]}-12-31T23:59:59.999` + match[8];

                case 'month':
                    let dateValue = monthArray[parseInt(match[2]) - 1];
                    return `${match[1]}-${match[2]}-${dateValue}T23:59:59.999` + match[8];

                case 'week':
                    const currDay = new Date(
                        parseInt(match[1]),
                        parseInt(match[2]) - 1,
                        parseInt(match[3]),
                    ).getDay();

                    const endDate = 6 - currDay + parseInt(match[3]);

                    if (endDate > parseInt(monthArray[(parseInt(match[2]) - 1) % 12])) {
                        const day = (endDate - parseInt(monthArray[(parseInt(match[2]) - 1) % 12]))
                            .toString()
                            .padStart(2, '0');
                        return (
                            `${
                                parseInt(match[2]) + 1 > 12
                                    ? (parseInt(match[1]) + 1).toString()
                                    : match[1]
                            }-${((parseInt(match[2]) + 1) % 12 == 0
                                ? 12
                                : (parseInt(match[2]) + 1) % 12
                            )
                                .toString()
                                .padStart(2, '0')}-${day}T${match[4]}:${match[5]}:${match[6]}${
                                match[7]
                            }` + match[8]
                        );
                    } else {
                        return (
                            `${match[1]}-${match[2]}-${endDate.toString().padStart(2, '0')}T${
                                match[4]
                            }:${match[5]}:${match[6]}${match[7]}` + match[8]
                        );
                    }

                case 'quarter':
                    if (parseInt(match[2]) <= 3) {
                        return (
                            `${match[1]}-03-${match[3]}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }
                    if (parseInt(match[2]) <= 6) {
                        return (
                            `${match[1]}-06-${match[3]}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }
                    if (parseInt(match[2]) <= 9) {
                        return (
                            `${match[1]}-09-${match[3]}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }
                    if (parseInt(match[2]) <= 12) {
                        return (
                            `${match[1]}-12-${match[3]}T${match[4]}:${match[5]}:${match[6]}${match[7]}` +
                            match[8]
                        );
                    }

                case 'date':
                    return `${match[1]}-${match[2]}-${match[3]}T23:59:59.999` + match[8];

                case 'hour':
                    return `${match[1]}-${match[2]}-${match[3]}T${match[4]}:59:59.999` + match[8];

                case 'minute':
                    return (
                        `${match[1]}-${match[2]}-${match[3]}T${match[4]}:${match[5]}:59.999` +
                        match[8]
                    );

                case 'second':
                    return (
                        `${match[1]}-${match[2]}-${match[3]}T${match[4]}:${match[5]}:${match[6]}.999` +
                        match[8]
                    );

                default:
                    return input;
            }
        }
        throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
    }
}
