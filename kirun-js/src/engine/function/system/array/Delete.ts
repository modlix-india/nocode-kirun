import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';

export class Delete extends AbstractArrayFunction {
    public constructor() {
        super(
            'Delete',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_ANY_VAR_ARGS,
            ],
            AbstractArrayFunction.EVENT_RESULT_EMPTY,
        );
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let source: any[] = context
            ?.getArguments()
            ?.get(Delete.PARAMETER_ARRAY_SOURCE.getParameterName());

        let receivedArgs: any[] = context
            ?.getArguments()
            ?.get(Delete.PARAMETER_ANY_VAR_ARGS.getParameterName());

        if (receivedArgs === null || typeof receivedArgs === 'undefined')
            throw new KIRuntimeException(
                'The deletable var args are empty. So cannot be proceeded further.',
            );

        if (source.length == 0 || receivedArgs.length == 0)
            throw new KIRuntimeException(
                'Expected a source or deletable for an array but not found any',
            );

        let indexes = new Set<number>();
        let duplicateResource: any[] = [];

        for (let i: number = source.length - 1; i >= 0; i--) {
            for (let j: number = 0; j < receivedArgs.length; j++) {
                if (!indexes.has(i) && PrimitiveUtil.compare(source[i], receivedArgs[j]) == 0)
                    indexes.add(i);
            }
        }

        console.log(indexes);

        for (let i: number = 0; i <= source.length - 1; i++) {
            if (!indexes.has(i)) duplicateResource.push(source[i]);
        }
        console.log(duplicateResource);

        source.splice(0, source.length, ...duplicateResource);

        return new FunctionOutput([EventResult.outputOf(new Map([]))]);
    }
}
