import { ExecutionException } from '../../../../exception/ExecutionException';
import { StringFormatter } from '../../../../util/string/StringFormatter';
import { BinaryOperator } from './BinaryOperator';

export class ObjectOperator extends BinaryOperator {
    public apply(t: any, u: any): any {
        if (t == null || !t) {
            throw new ExecutionException('Cannot apply array operator on a null value');
        }

        if (u == null || !u) {
            throw new ExecutionException('Cannot retrive null property value');
        }

        const x: string = typeof t;

        if (!Array.isArray(t) && x != 'string' && x != 'object') {
            throw new ExecutionException(
                StringFormatter.format('Cannot retrieve value from a primitive value $', t),
            );
        }
        return t[u];
    }
}
