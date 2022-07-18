import { ExecutionException } from '../../../../exception/ExecutionException';
import { StringFormatter } from '../../../../util/string/StringFormatter';
import { Operation } from '../../Operation';

export abstract class BinaryOperator {
    public abstract apply(t: any, u: any): any;

    public nullCheck(e1: any, e2: any, op: Operation): void {
        if (e1 == null || !e1 || e2 == null || !e2)
            throw new ExecutionException(
                StringFormatter.format('$ cannot be applied to a null value', op.getOperatorName()),
            );
    }
}
