import { TernaryOperator } from './TernaryOperator';

export class ConditionalTernaryOperator extends TernaryOperator {
    public apply(t: any, u: any, v: any): any {
        return t ? u : v;
    }
}
