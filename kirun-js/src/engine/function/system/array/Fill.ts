import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Fill extends AbstractArrayFunction {
    public constructor() {
        super(
            'Fill',
            [
                Fill.PARAMETER_ARRAY_SOURCE,
                Fill.PARAMETER_INT_SOURCE_FROM,
                Fill.PARAMETER_INT_LENGTH,
                Fill.PARAMETER_ANY,
            ],
            Fill.EVENT_RESULT_EMPTY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var source = context?.getArguments()?.get(Fill.PARAMETER_ARRAY_SOURCE.getParameterName());
        var srcfrom = context
            ?.getArguments()
            ?.get(Fill.PARAMETER_INT_SOURCE_FROM.getParameterName());
        var length = context?.getArguments()?.get(Fill.PARAMETER_INT_LENGTH.getParameterName());
        var element = context?.getArguments()?.get(Fill.PARAMETER_ANY.getParameterName());

        if (srcfrom < 0)
            throw new KIRuntimeException(
                StringFormatter.format('Arrays out of bound trying to access $ index', srcfrom),
            );

        if (length == -1) length = source.length - srcfrom;

        let add = srcfrom + length - source.length;

        if (add > 0) {
            for (let i = 0; i < add; i++) source.push();
        }

        for (let i = srcfrom; i < srcfrom + length; i++) {
            source[i] = isNullValue(element) ? element : JSON.parse(JSON.stringify(element));
        }

        return new FunctionOutput([EventResult.outputOf(MapUtil.of())]);
    }
}
