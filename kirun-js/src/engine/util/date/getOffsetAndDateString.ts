export default function getOffsetAndDateString(input: string) {
    if (input.length == 29 || input.length == 24) {
        return [input.slice(0, 23), input.slice(23)];
    } else {
        return [input.slice(0, 26), input.slice(26)];
    }
}
