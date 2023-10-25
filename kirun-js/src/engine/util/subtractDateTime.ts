export default function subtractDateTime(date: Date, value: number, unit: string): string | null {
    switch (unit) {
        case 'YEARS':
            date.setFullYear(date.getFullYear() - value);
            break;
        case 'MONTHS':
            date.setMonth(date.getMonth() - value);
            break;
        case 'DAYS':
            date.setDate(date.getDate() - value);
            break;
        case 'HOURS':
            date.setHours(date.getHours() - value);
            break;
        case 'MINUTES':
            date.setMinutes(date.getMinutes() - value);
            break;
        case 'SECONDS':
            date.setSeconds(date.getSeconds() - value);
            break;
        case 'MILLIS':
            date.setMilliseconds(date.getMilliseconds() - value);
            break;
        default:
            return null;
    }
    return date.toISOString();
}
