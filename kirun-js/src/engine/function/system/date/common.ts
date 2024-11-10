import { DateTime } from 'luxon';

export function getDateTime(isoTimestamp: string): DateTime {
    let dt = DateTime.fromISO(isoTimestamp);
    if (!dt?.isValid) {
        throw new Error('Invalid ISO timestamp');
    }
    return dt;
}
