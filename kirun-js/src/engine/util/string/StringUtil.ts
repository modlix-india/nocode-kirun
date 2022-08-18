import { KIRuntimeException } from '../../exception/KIRuntimeException';
import { StringFormatter } from './StringFormatter';

export class StringUtil {
    private constructor() {}

    public static nthIndex(str: string, c: string, from: number = 0, occurance: number): number {
        if (!str) throw new KIRuntimeException('String cannot be null');

        if (from < 0 || from >= str.length)
            throw new KIRuntimeException(
                StringFormatter.format('Cannot search from index : $', from),
            );

        if (occurance <= 0 || occurance > str.length)
            throw new KIRuntimeException(
                StringFormatter.format('Cannot search for occurance : $', occurance),
            );

        while (from < str.length) {
            if (str.charAt(from) == c) {
                --occurance;
                if (occurance == 0) return from;
            }

            ++from;
        }

        return -1;
    }

    public static splitAtFirstOccurance(str: string, c: string): Array<string | undefined> {
        if (!str) return new Array(2);

        let index: number = str.indexOf(c);

        if (index == -1) return [str, undefined];

        return [str.substring(0, index), str.substring(index + 1)];
    }

    public static isNullOrBlank(str: string | undefined): boolean {
        return !str || str.trim() == '';
    }
}
