export default function addMonths(date: Date, months: number): Date {
    date.setMonth(date.getMonth() + months);
    return date;
}
