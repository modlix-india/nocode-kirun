export class StringFormatter {
    public static format(formatString: string, ...params: any[]): string {
        if (!params || params.length == 0) return formatString;

        let sb: string = '';
        let ind: number = 0;
        let chr: string = '';
        let prevchar: string = chr;
        let length: number = formatString.length;

        for (let i = 0; i < length; i++) {
            chr = formatString.charAt(i);

            if (chr == '$' && prevchar == '\\') sb = sb.substring(0, i - 1) + chr;
            else if (chr == '$' && ind < params.length) sb += params[ind++];
            else sb += chr;

            prevchar = chr;
        }

        return sb.toString();
    }

    private constructor() {}
}
