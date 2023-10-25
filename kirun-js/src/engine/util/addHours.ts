export default function addHours(date: Date, hours: number) {
    date.setHours(date.getHours() + hours);
    return date;
}
