import { KIRuntimeException } from '../../exception/KIRuntimeException';
import { isNullValue } from '../NullCheck';

const dateRegex =
    /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d{3})?(Z|([+-]([01]\d|2[0-3]):([0-5]\d)))?$/;

const nonLeap = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
const leap = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

function checkLeapYear(year: number): boolean {
    if (year % 4 !== 0) {
        return false;
    } else if (year % 100 !== 0) {
        return true;
    } else return year % 400 === 0;
}

export function setYear(inputDate: string, year: number) {
    if (year >= 275761 || year <= -271821) return;

    const parts = inputDate.match(dateRegex);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

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

    return parts;
}

export function setMonth(inputDate: string, addMonth: number) {
    const parts = inputDate.match(dateRegex);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    // let months: number  = parseInt(parts[2]);

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
            }
            updateMonth = convertedMonths + 1;
            break;

        case 4:
        case 6:
        case 9:
        case 11:
            updateMonth = date === 31 ? convertedMonths + 1 : convertedMonths;
            date = 1;
    }

    parts[2] = updateMonth < 10 ? 0 + updateMonth.toString() : updateMonth.toString();
    parts[3] = date < 10 ? '' + 0 + updateMonth.toString() : date.toString();

    //check month with possible dates
    let updateTimeStamp = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp =
            inputDate.substring(0, 8) + updateMonth + '-' + date + inputDate.substring(13);
    else
        updateTimeStamp =
            inputDate.substring(0, 5) + updateMonth + '-' + date + inputDate.substring(10);

    return setYear(updateTimeStamp, years);
}

export function setDate(inputDate: string, addDays: number) {
    const parts = inputDate.match(dateRegex);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

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
    if (addDays === 0) return [day, month, year];

    // for neagtive within the previous month
    if (addDays < 0) flag = true;

    if (flag && daysInMonth[month - 1] >= addDays * -1) {
        console.log(day);
        return [day + addDays, month, year];
    }

    //positive within the month
    if (!flag && addDays <= daysInMonth[month - 1]) return [addDays, month, year];

    if (!flag) {
        while (addDays > daysInMonth[month - 1]) {
            console.log(`${addDays - daysInMonth[month - 1]}, ${year}, ${month}`);
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
            //         console.log(`${addDays - daysInMonth[month-1]}, ${year}, ${month}`);
            addDays += daysInMonth[month - 1];
            month--;
            if (month - 1 < 0) {
                month = 12;
                year--;
            }
            daysInMonth = checkLeapYear(year) ? leap : nonLeap;
        }
    }

    return [addDays < 0 ? daysInMonth[month - 1] + addDays : addDays, month, year];
}

export function setHours(inputDate: string, addHours: number) {
    const parts = inputDate.match(dateRegex);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    if (addHours < 0 || addHours > 24)
        throw new KIRuntimeException('Hours should be in the range of 0 to 23');

    let hours: string = convertToString(addHours);

    let updateTimeStamp: string = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp = inputDate.substring(0, 14) + hours + inputDate.substring(16);
    else updateTimeStamp = inputDate.substring(0, 11) + hours + inputDate.substring(13);
}

export function setMinutes(inputDate: string, addMinutes: number) {
    const parts = inputDate.match(dateRegex);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    if (addMinutes < 0 || addMinutes > 59)
        throw new KIRuntimeException('Minutes should be in the range of 0 to 59');

    let minutes: string = convertToString(addMinutes);

    let updateTimeStamp: string = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp = inputDate.substring(0, 17) + minutes + inputDate.substring(19);
    else updateTimeStamp = inputDate.substring(0, 13) + minutes + inputDate.substring(16);
}

export function setSeconds(inputDate: string, addSeconds: number) {
    const parts = inputDate.match(dateRegex);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    if (addSeconds < 0 || addSeconds > 59)
        throw new KIRuntimeException('Seconds should be in the range of 0 to 59');

    let seconds: string = convertToString(addSeconds);

    let updateTimeStamp: string = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp = inputDate.substring(0, 20) + seconds + inputDate.substring(22);
    else updateTimeStamp = inputDate.substring(0, 17) + seconds + inputDate.substring(19);
}

export function setMilliSeconds(inputDate: string, addMillis: number) {
    const parts = inputDate.match(dateRegex);

    if (parts === null || isNullValue(parts))
        throw new KIRuntimeException('Please provide date for processing.');

    if (addMillis < 0 || addMillis > 999)
        throw new KIRuntimeException('Milliseconds should be in the range of 0 to 999');

    let millis: string = convertToString(addMillis);

    let updateTimeStamp: string = parts[0];

    if (updateTimeStamp.charAt(0) === '+' || updateTimeStamp.charAt(0) === '-')
        updateTimeStamp = inputDate.substring(0, 22) + millis + inputDate.substring(25);
    else updateTimeStamp = inputDate.substring(0, 17) + millis + inputDate.substring(20);
}

function convertToString(val: number) {
    return val < 10 ? 0 + val.toString() : val.toString();
}

function absMonthValue(val: number) {
    return val < 0 ? 12 - Math.abs(val) : val;
}

function absFloor(val: number) {
    return Math.floor(val);
}
