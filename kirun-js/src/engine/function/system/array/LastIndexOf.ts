import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class LastIndexOf extends AbstractArrayFunction {
    public constructor() {
        super(
            'LastIndexOf',
            [
                LastIndexOf.PARAMETER_ARRAY_SOURCE,
                LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT,
                LastIndexOf.PARAMETER_INT_FIND_FROM,
            ],
            LastIndexOf.EVENT_RESULT_INTEGER,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(LastIndexOf.PARAMETER_ARRAY_SOURCE.getParameterName());

        let find = context
            ?.getArguments()
            ?.get(LastIndexOf.PARAMETER_ANY_ELEMENT_OBJECT.getParameterName());

        let len = context
            ?.getArguments()
            ?.get(LastIndexOf.PARAMETER_INT_FIND_FROM.getParameterName());

        if (source.length == 0)
            return new FunctionOutput([
                EventResult.outputOf(new Map([[LastIndexOf.EVENT_RESULT_NAME, -1]])),
            ]);

        if (len < 0 || len > source.length)
            throw new KIRuntimeException(
                "The value of length shouldn't the exceed the size of the array or shouldn't be in terms",
            );

        let index: number = -1;

        for (let i: number = source.length - 1; i >= len; i--) {
            if (PrimitiveUtil.compare(source[i], find) == 0) {
                index = i;
                break;
            }
        }

        return new FunctionOutput([
            EventResult.outputOf(new Map([[LastIndexOf.EVENT_RESULT_NAME, index]])),
        ]);
    }
}
