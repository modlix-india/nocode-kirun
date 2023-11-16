import { KIRuntimeException } from '../../exception/KIRuntimeException';
import { isNullValue } from '../NullCheck';

const nonLeap = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
const leap = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

function checkLeapYear(year: number): boolean {
    if (year % 4 !== 0) {
        return false;
    } else if (year % 100 !== 0) {
        return true;
    } else return year % 400 === 0;
}

function updateDateString(dateArr: string[]) {
    let outputDate =
        dateArr[1] +
        '-' +
        dateArr[2] +
        '-' +
        dateArr[3] +
        'T' +
        dateArr[4] +
        ':' +
        dateArr[5] +
        ':' +
        dateArr[6] +
        dateArr[7];

    return outputDate;
}

function splitIntoDateAndOffset(inputDate: string): string[] {
    let offsetString;
    let modifyString = inputDate;
    if (inputDate.length == 29 || inputDate.length == 24) {
        offsetString = inputDate.slice(23);
        modifyString = inputDate.slice(0, 23) + 'Z';
    } else {
        offsetString = inputDate.slice(26);
        modifyString = inputDate.slice(0, 26) + 'Z';
    }

    return [modifyString, offsetString];
}

function updateDateStringFromDateParts(parts: string[], dateParts: string[]): string {
    parts[1] = dateParts[0];
    parts[2] = dateParts[1];
    parts[3] = dateParts[2];
    return updateDateString(parts);
}

function getParts(inputDate: string): string[] | null {
    const dateRegex =
        /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d{3})?(Z|([+-]([01]\d|2[0-3]):([0-5]\d)))?$/;

    return inputDate.match(dateRegex);
}

export function setFullYear(inputDate: string, year: number): string {
    const offsetDate = splitIntoDateAndOffset(inputDate);

    if (year >= 275761 || year <= -271821)
        throw new KIRuntimeException('Given year cannot be set to year as it out of bounds');

    const parts = getParts(offsetDate[0]);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide valid date for processing.');

    if (+parts[2] === 2 && +parts[3] === 29 && checkLeapYear(parseInt(parts[1]))) {
        parts[2] = '03';
        parts[3] = '01';
        parts[1] = year + '';
    } else
        parts[1] =
            year >= 0 && year <= 9999
                ? (year + '').padStart(4, '0')
                : year > 9999
                ? `+${(year + '').padStart(6, '0')}`
                : `-${(year * -1 + '').padStart(6, '0')}`;

    return updateDateString(parts) + offsetDate[1];
}

export function setMonth(inputDate: string, addMonth: number): string {
    const parts = getParts(inputDate);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide valid date for processing.');

    let convertedMonths: number = absMonthValue(addMonth % 12) + 1;
    let extraYears: number = absFloor(addMonth / 12);
    let year: number = parseInt(parts[1]);
    let years: number = year + extraYears;
    let date: number = parseInt(parts[3]);
    let updateMonth: number = convertedMonths;

    switch (convertedMonths) {
        case 2:
            if (date >= 28) {
                date = checkLeapYear(year) ? date - 29 : date - 28;
                updateMonth = convertedMonths + 1;
            }

        case 4:
        case 6:
        case 9:
        case 11:
            if (date === 31) {
                updateMonth = convertedMonths + 1;
                date = 1;
            }
    }
    //check month with possible dates
    let updateTimeStamp = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp =
            inputDate.substring(0, 8) +
            convertToString(updateMonth) +
            '-' +
            convertToString(date) +
            inputDate.substring(13);
    else
        updateTimeStamp =
            inputDate.substring(0, 5) +
            convertToString(updateMonth) +
            '-' +
            convertToString(date) +
            inputDate.substring(10);

    return setFullYear(updateTimeStamp, years);
}

export function setDate(inputDate: string, addDays: number): string {
    const offsetDate = splitIntoDateAndOffset(inputDate);
    const parts = getParts(offsetDate[0]);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide valid date for processing.');

    let year: number = parseInt(parts[1]);
    let month: number = parseInt(parts[2]);
    let day: number = parseInt(parts[3]);
    let flag = false;
    let daysInMonth = checkLeapYear(year) ? leap : nonLeap;

    if (addDays <= 0) {
        day = daysInMonth[month - 2 < 0 ? 11 : month - 2];
        month = month - 1 < 1 ? 12 : month - 1;
        year = month - 1 < 1 ? year - 1 : year;
    }

    //for zero value
    if (addDays === 0) {
        return (
            updateDateStringFromDateParts(parts, [
                convertYearToString(year),
                convertToString(month),
                convertToString(day),
            ]) + offsetDate[1]
        );
    }

    // for neagtive within the previous month
    if (addDays < 0) flag = true;

    if (flag && daysInMonth[month - 1] >= addDays * -1) {
        return (
            updateDateStringFromDateParts(parts, [
                convertYearToString(year),
                convertToString(month),
                convertToString(day + addDays),
            ]) + offsetDate[1]
        );
    }

    //positive within the month
    if (!flag && addDays <= daysInMonth[month - 1]) {
        updateDateStringFromDateParts(parts, [
            convertYearToString(year),
            convertToString(month),
            convertToString(addDays),
        ]);
    }

    if (!flag) {
        while (addDays > daysInMonth[month - 1]) {
            addDays -= daysInMonth[month - 1];
            month++;
            if (month - 1 > 11) {
                month = 1;
                year++;
            }
            daysInMonth = checkLeapYear(year) ? leap : nonLeap;
        }
    } else {
        while (addDays * -1 > daysInMonth[month - 1]) {
            addDays += daysInMonth[month - 1];
            month--;
            if (month - 1 < 0) {
                month = 12;
                year--;
            }
            daysInMonth = checkLeapYear(year) ? leap : nonLeap;
        }
    }

    if (addDays < 0)
        return (
            updateDateStringFromDateParts(parts, [
                convertYearToString(year),
                convertToString(month),
                convertToString(daysInMonth[month - 1] + addDays),
            ]) + offsetDate[1]
        );
    else
        return (
            updateDateStringFromDateParts(parts, [
                convertYearToString(year),
                convertToString(month),
                convertToString(addDays),
            ]) + offsetDate[1]
        );
}

export function setHours(inputDate: string, addHours: number): string {
    const parts = getParts(inputDate);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    if (addHours < 0 || addHours > 24)
        throw new KIRuntimeException('Hours should be in the range of 0 and 23');

    let hours: string = convertToString(addHours);

    let updateTimeStamp: string = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp = inputDate.substring(0, 14) + hours + inputDate.substring(16);
    else updateTimeStamp = inputDate.substring(0, 11) + hours + inputDate.substring(13);

    return updateTimeStamp;
}

export function setMinutes(inputDate: string, addMinutes: number): string {
    const parts = getParts(inputDate);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    if (addMinutes < 0 || addMinutes > 59)
        throw new KIRuntimeException('Minutes should be in the range of 0 and 59');

    let minutes: string = convertToString(addMinutes);

    let updateTimeStamp: string = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp = inputDate.substring(0, 17) + minutes + inputDate.substring(19);
    else updateTimeStamp = inputDate.substring(0, 14) + minutes + inputDate.substring(16);

    return updateTimeStamp;
}

export function setSeconds(inputDate: string, addSeconds: number): string {
    const parts = getParts(inputDate);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    if (addSeconds < 0 || addSeconds > 59)
        throw new KIRuntimeException('Seconds should be in the range of 0 and 59');

    let seconds: string = convertToString(addSeconds);

    let updateTimeStamp: string = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp = inputDate.substring(0, 20) + seconds + inputDate.substring(22);
    else updateTimeStamp = inputDate.substring(0, 17) + seconds + inputDate.substring(19);

    return updateTimeStamp;
}

export function setMilliSeconds(inputDate: string, addMillis: number): string {
    const parts = getParts(inputDate);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    if (addMillis < 0 || addMillis > 999)
        throw new KIRuntimeException('Milliseconds should be in the range of 0 and 999');

    let millis: string = convertMillisToString(addMillis);

    let updateTimeStamp: string = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp = inputDate.substring(0, 23) + millis + inputDate.substring(26);
    else updateTimeStamp = inputDate.substring(0, 20) + millis + inputDate.substring(23);

    return updateTimeStamp;
}

function convertYearToString(year: number): string {
    if (year >= 0 && year <= 9999) return (year + '').padStart(4, '0');
    else if (year > 9999) return `+${(year + '').padStart(6, '0')}`;

    return `-${(year * -1 + '').padStart(6, '0')}`;
}

function convertMillisToString(millis: number): string {
    if (millis < 10) return millis.toString().padStart(3, '0');
    else if (millis < 100) return millis.toString().padStart(2, '0');
    return millis.toString();
}

function convertToString(val: number): string {
    return val < 10 ? val.toString().padStart(2, '0') : val.toString();
}

function absMonthValue(val: number): number {
    return val < 0 ? 12 - Math.abs(val) : val;
}

function absFloor(val: number): number {
    return Math.floor(val);
}
