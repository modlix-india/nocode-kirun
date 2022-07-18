import { ExecutionException } from '../../../../exception/ExecutionException';
import { StringFormatter } from '../../../../util/string/StringFormatter';
import { BinaryOperator } from './BinaryOperator';

export class ArrayOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        if (!t) {
            throw new ExecutionException('Cannot apply array operator on a null value');
        }

        if (!u) {
            throw new ExecutionException('Cannot retrive null index value');
        }

        if (!Array.isArray(t) && typeof t != 'string') {
            throw new ExecutionException(
                StringFormatter.format('Cannot retrieve value from a primitive value $', t),
            );
        }
        if (u >= t.length)
            throw new ExecutionException(
                StringFormatter.format(
                    'Cannot retrieve index $ from the array of length $',
                    u,
                    t.length,
                ),
            );

        return t[u];
    }
}
