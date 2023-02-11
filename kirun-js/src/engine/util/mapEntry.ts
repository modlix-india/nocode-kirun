import { Function } from '../function/Function';

export default function mapEntry(fun: Function): [string, Function] {
    return [fun.getSignature().getName(), fun];
}
