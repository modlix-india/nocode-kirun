import { KIRuntimeException } from '../exception/KIRuntimeException';
import { isNullValue } from '../util/NullCheck';
import { EventResult } from './EventResult';
import { FunctionOutputGenerator } from './FunctionOutputGenerator';

export class FunctionOutput {
    private fo: EventResult[];

    private index: number = 0;
    private generator?: FunctionOutputGenerator;

    public constructor(arg: EventResult[] | FunctionOutputGenerator) {
        if (isNullValue(arg)) throw new KIRuntimeException('Function output is generating null');

        if (Array.isArray(arg) && arg.length && arg[0] instanceof EventResult) {
            this.fo = arg as EventResult[];
        } else {
            this.fo = [];
            if (!Array.isArray(arg)) this.generator = arg as FunctionOutputGenerator;
        }
    }

    public next(): EventResult | undefined {
        if (!this.generator) {
            if (this.index < this.fo.length) return this.fo[this.index++];
            return undefined;
        }

        const er: EventResult | undefined = this.generator.next();
        if (er) this.fo.push(er);
        return er;
    }

    public allResults(): EventResult[] {
        return this.fo;
    }
}
