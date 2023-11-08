import { KIRuntimeException } from '../../exception/KIRuntimeException';

const iso8601Pattern =
    /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d+)?(Z|([+-]\d{2}:\d{2}))?$/;

export default function isLeapYear(timeStamp: string): boolean {
    const match = timeStamp.match(iso8601Pattern);
    if (match) {
        let year = parseInt(match[1]);
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
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
    throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
}
