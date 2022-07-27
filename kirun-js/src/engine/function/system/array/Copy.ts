import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { ArrayUtil } from '../../../util/ArrayUtil';
import { MapUtil } from '../../../util/MapUtil';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Copy extends AbstractArrayFunction {
    public constructor() {
        super(
            'Copy',
            [
                Copy.PARAMETER_ARRAY_SOURCE,
                Copy.PARAMETER_INT_SOURCE_FROM,
                Copy.PARAMETER_INT_LENGTH,
                Copy.PARAMETER_BOOLEAN_DEEP_COPY,
            ],
            Copy.EVENT_RESULT_ARRAY,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        var source = context.getArguments().get(Copy.PARAMETER_ARRAY_SOURCE.getParameterName());
        var srcfrom = context.getArguments().get(Copy.PARAMETER_INT_SOURCE_FROM.getParameterName());
        var length = context.getArguments().get(Copy.PARAMETER_INT_LENGTH.getParameterName());

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

        var deep = context.getArguments().get(Copy.PARAMETER_BOOLEAN_DEEP_COPY.getParameterName());

        const ja: any[] = new Array(length);

        for (let i = srcfrom; i < srcfrom + length; i++) {
            if (!isNullValue(source[i]))
                ja[i - srcfrom] = deep ? JSON.parse(JSON.stringify(source[i])) : source[i];
        }

        return new FunctionOutput([EventResult.outputOf(MapUtil.of(Copy.EVENT_RESULT_NAME, ja))]);
    }
}
