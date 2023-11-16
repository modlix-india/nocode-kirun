import isLeapYear from './isLeapYear';

export default function isValidISO8601DateTime(input: string): boolean {
    const iso8601Pattern =
        /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d{3})?(Z|([+-]([01]\d|2[0-3]):([0-5]\d)))?$/;

    const match = input.match(iso8601Pattern);
    if (match) {
        const month = parseInt(match[2]);
        if (match[2] == '02') {
            if (isLeapYear(input)) {
                if (match[2] == '02') {
                    if (parseInt(match[3]) <= 29) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                if (match[2] == '02') {
                    if (parseInt(match[3]) <= 28) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }
        }
        if (month >= 1 && month <= 7) {
            if (month % 2 == 0) {
                if (parseInt(match[3]) <= 30) {
                    return true;
                }
                return false;
            } else {
                return true;
            }
        }
        if (month >= 8 && month <= 12) {
            if (month % 2 == 1) {
                if (parseInt(match[3]) <= 30) {
                    return true;
                }
                return false;
            } else {
                return true;
            }
        }
    }

    return false;
}
