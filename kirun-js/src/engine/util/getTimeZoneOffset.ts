export default function getTimezoneOffset(input: string): number | null {
    const iso8601Pattern =
        /^(\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d+)?(Z|([+-]\d{2}:\d{2}))?$/;
    const match = input.match(iso8601Pattern);
    if (match) {
        console.log(match);
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
            return parts[0][0] === '+' ? -1 * (hours * 60 + minutes) : 1 * (hours * 60 + minutes);
        }
    }
    return null;
}
