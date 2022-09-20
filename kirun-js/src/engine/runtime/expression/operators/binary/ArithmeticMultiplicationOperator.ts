import { Operation } from '../../Operation';
import { BinaryOperator } from './BinaryOperator';

export class ArithmeticMultiplicationOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        this.nullCheck(t, u, Operation.MULTIPLICATION);

        const isTTypeString = typeof t === 'string';
        const uType = typeof u;

        if (isTTypeString || uType === 'string') {
            let str: string = isTTypeString ? t : u;
            let num: number = isTTypeString ? u : t;

            let sb: string = '';

            let reverse: boolean = num < 0;
            num = Math.abs(num);

            let times = Math.floor(num);
            while (times-- > 0) sb += str;

            let chrs = Math.floor(str.length * (num - Math.floor(num)));
            if (chrs < 0) chrs = -chrs;

            if (chrs != 0) sb += str.substring(0, chrs);

            if (reverse) {
                let rev = '';
                for (let i = sb.length - 1; i >= 0; i--) {
                    rev += sb[i];
                }
                return rev;
            }

            return sb;
        }

        return t * u;
    }
}
