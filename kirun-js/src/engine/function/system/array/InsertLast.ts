import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { isNullValue } from '../../../util/NullCheck';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class InsertLast extends AbstractArrayFunction {
    public constructor() {
        super(
            'InsertLast',
            [InsertLast.PARAMETER_ARRAY_SOURCE, InsertLast.PARAMETER_ANY],
            InsertLast.EVENT_RESULT_ARRAY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(InsertLast.PARAMETER_ARRAY_SOURCE.getParameterName());

        var output = context?.getArguments()?.get(InsertLast.PARAMETER_ANY.getParameterName());

        source = [...source];

        source.push(output);
        return new FunctionOutput([
            EventResult.outputOf(new Map([[AbstractArrayFunction.EVENT_RESULT_NAME, source]])),
        ]);
    }
}
