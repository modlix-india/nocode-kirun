import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { isNullValue } from '../../../util/NullCheck';
import { deepEqual } from '../../../util/deepEqual';
import { duplicate } from '../../../util/duplicate';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class RemoveDuplicates extends AbstractArrayFunction {
    public constructor() {
        super(
            'RemoveDuplicates',
            [
                RemoveDuplicates.PARAMETER_ARRAY_SOURCE,
                RemoveDuplicates.PARAMETER_INT_SOURCE_FROM,
                RemoveDuplicates.PARAMETER_INT_LENGTH,
            ],
            RemoveDuplicates.EVENT_RESULT_ARRAY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var source = context
            ?.getArguments()
            ?.get(RemoveDuplicates.PARAMETER_ARRAY_SOURCE.getParameterName());
        var srcfrom = context
            ?.getArguments()
            ?.get(RemoveDuplicates.PARAMETER_INT_SOURCE_FROM.getParameterName());
        var length = context
            ?.getArguments()
            ?.get(RemoveDuplicates.PARAMETER_INT_LENGTH.getParameterName());

        if (length == -1) length = source.length - srcfrom;

        if (srcfrom + length > source.length)
            throw new KIRuntimeException(
                StringFormatter.format(
                    'Array has no elements from $ to $ as the array size is $',
                    srcfrom,
                    srcfrom + length,
                    source.length,
                ),
            );

        const ja: any[] = [...source];
        const to = srcfrom + length;

        for (let i = to - 1; i >= srcfrom; i--) {
            for (let j = i - 1; j >= srcfrom; j--) {
                if (deepEqual(ja[i], ja[j])) {
                    ja.splice(i, 1);
                    break;
                }
            }
        }

        return new FunctionOutput([
            EventResult.outputOf(MapUtil.of(RemoveDuplicates.EVENT_RESULT_NAME, ja)),
        ]);
    }
}
