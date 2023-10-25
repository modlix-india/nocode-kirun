export default function addMilliSeconds(date: Date, millis: number): Date {
    date.setMilliseconds(millis);
    return date;
}
