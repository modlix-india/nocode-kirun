import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Rotate extends AbstractArrayFunction {
    public constructor() {
        super(
            'Rotate',
            [Rotate.PARAMETER_ARRAY_SOURCE, Rotate.PARAMETER_ROTATE_LENGTH],
            Rotate.EVENT_RESULT_EMPTY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(Rotate.PARAMETER_ARRAY_SOURCE.getParameterName());

        let rotLen: number = context
            ?.getArguments()
            ?.get(Rotate.PARAMETER_ROTATE_LENGTH.getParameterName());

        if (source.length == 0) return new FunctionOutput([EventResult.outputOf(new Map([]))]);

        let size: number = source.length;
        rotLen = rotLen % size;

        this.rotate(source, 0, rotLen - 1);
        this.rotate(source, rotLen, size - 1);
        this.rotate(source, 0, size - 1);

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }

    private rotate(elements: any[], start: number, end: number): void {
        while (start < end) {
            let temp: any = elements[start];
            elements[start++] = elements[end];
            elements[end--] = temp;
        }
    }
}
