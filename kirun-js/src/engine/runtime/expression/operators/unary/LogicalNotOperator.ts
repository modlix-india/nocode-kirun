import { UnaryOperator } from './UnaryOperator';

export class LogicalNotOperator extends UnaryOperator {
    public apply(t: any): any {
        return !t && t !== '';
    }
}
