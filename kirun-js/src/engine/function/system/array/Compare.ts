import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { ArrayUtil } from '../../../util/ArrayUtil';
import { MapUtil } from '../../../util/MapUtil';
import { isNullValue } from '../../../util/NullCheck';
import { StringFormatter } from '../../../util/string/StringFormatter';
import { AbstractArrayFunction } from './AbstractArrayFunction';

export class Compare extends AbstractArrayFunction {
    public constructor() {
        super(
            'Compare',
            ArrayUtil.of(
                Compare.PARAMETER_ARRAY_SOURCE,
                Compare.PARAMETER_INT_SOURCE_FROM,
                Compare.PARAMETER_ARRAY_FIND,
                Compare.PARAMETER_INT_FIND_FROM,
                Compare.PARAMETER_INT_LENGTH,
            ),
            Compare.EVENT_RESULT_INTEGER,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        var source = context
            ?.getArguments()
            ?.get(Compare.PARAMETER_ARRAY_SOURCE.getParameterName());
        var srcfrom = context
            ?.getArguments()
            ?.get(Compare.PARAMETER_INT_SOURCE_FROM.getParameterName());
        var find = context?.getArguments()?.get(Compare.PARAMETER_ARRAY_FIND.getParameterName());
        var findfrom = context
            ?.getArguments()
            ?.get(Compare.PARAMETER_INT_FIND_FROM.getParameterName());
        var length = context?.getArguments()?.get(Compare.PARAMETER_INT_LENGTH.getParameterName());

        if (source.length == 0) {
            throw new KIRuntimeException('Compare source array cannot be empty');
        }

        if (find.length == 0) {
            throw new KIRuntimeException('Compare find array cannot be empty');
        }

        if (length == -1) length = source.length - srcfrom;

        if (srcfrom + length > source.length)
            throw new KIRuntimeException(
                StringFormatter.format(
                    'Source array size $ is less than comparing size $',
                    source.length,
                    srcfrom + length,
                ),
            );

        if (findfrom + length > find.length)
            throw new KIRuntimeException(
                StringFormatter.format(
                    'Find array size $ is less than comparing size $',
                    find.length,
                    findfrom + length,
                ),
            );

        return new FunctionOutput(
            ArrayUtil.of(
                EventResult.outputOf(
                    MapUtil.of(
                        Compare.EVENT_RESULT_NAME,
                        this.compare(
                            source,
                            srcfrom,
                            srcfrom + length,
                            find,
                            findfrom,
                            findfrom + length,
                        ),
                    ),
                ),
            ),
        );
    }

    public compare(
        source: any[],
        srcfrom: number,
        srcto: number,
        find: any[],
        findfrom: number,
        findto: number,
    ): number {
        if (srcto < srcfrom) {
            let x: number = srcfrom;
            srcfrom = srcto;
            srcto = x;
        }

        if (findto < findfrom) {
            let x: number = findfrom;
            findfrom = findto;
            findto = x;
        }

        if (srcto - srcfrom != findto - findfrom) {
            throw new KIRuntimeException(
                StringFormatter.format(
                    'Cannot compare uneven arrays from $ to $ in source array with $ to $ in find array',
                    srcto,
                    srcfrom,
                    findto,
                    findfrom,
                ),
            );
        }

        for (let i = srcfrom, j = findfrom; i < srcto; i++, j++) {
            let x: number = 1;

            if (isNullValue(source[i]) || isNullValue(find[j])) {
                let s: boolean = isNullValue(source[i]);
                let f: boolean = isNullValue(find[j]);

                if (s == f) x = 0;
                else if (s) x = -1;
            } else {
                let typs: string = typeof source[i];
                let typf: string = typeof find[j];

                if (typs === 'object' || typf === 'object') {
                    x = 1;
                } else if (typs === 'string' || typf === 'string') {
                    let s = '' + source[i];
                    let f = '' + find[j];
                    if (s === f) x = 0;
                    else if (s < f) x = -1;
                } else if (typs === 'boolean' || typf === 'boolean') {
                    x = typs == typf ? 0 : 1;
                } else if (typs === 'number' && typf === 'number') {
                    x = source[i] - find[j];
                }
            }

            if (x != 0) return x;
        }

        return 0;
    }
}
