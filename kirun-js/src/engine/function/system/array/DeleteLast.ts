import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class DeleteLast extends AbstractArrayFunction {
    public constructor() {
        super('DeleteLast', [DeleteLast.PARAMETER_ARRAY_SOURCE], DeleteLast.EVENT_RESULT_EMPTY);
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            ?.getArguments()
            ?.get(DeleteLast.PARAMETER_ARRAY_SOURCE.getParameterName());

        if (source.length == 0) throw new KIRuntimeException('Given source array is empty');

        source.pop();

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
