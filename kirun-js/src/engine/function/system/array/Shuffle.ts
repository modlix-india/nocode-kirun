import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Shuffle extends AbstractArrayFunction {
    public constructor() {
        super('Shuffle', [Shuffle.PARAMETER_ARRAY_SOURCE], Shuffle.EVENT_RESULT_EMPTY);
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            ?.getArguments()
            ?.get(Shuffle.PARAMETER_ARRAY_SOURCE.getParameterName());

        if (source.length <= 1) return new FunctionOutput([EventResult.outputOf(new Map([]))]);

        let x: number = 0;
        let size: number = source.length;

        for (let i: number = 0; i < size; i++) {
            let y: number = Math.floor(Math.random() * size) % size;
            let temp: any = source[x];
            source[x] = source[y];
            source[y] = temp;
            x = y;
        }

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
