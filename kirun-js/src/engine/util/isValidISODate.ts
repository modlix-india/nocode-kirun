export default function isValidISO8601DateTime(input: string): boolean {
    const iso8601Pattern =
        /^(\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d+)?(Z|([+-]([01]\d|2[0-3]):([0-5]\d)))?$/;
    return iso8601Pattern.test(input);
}
