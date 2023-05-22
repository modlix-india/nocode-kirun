import { Schema } from '../../../json/schema/Schema';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Parameter } from '../../../model/Parameter';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractArrayFunction } from './AbstractArrayFunction';

const KEY_NAME = 'keyName';

export class ArrayToArrayOfObjects extends AbstractArrayFunction {
    public constructor() {
        super(
            'ArrayToObjects',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                Parameter.of(KEY_NAME, Schema.ofString(KEY_NAME), true),
            ],
            AbstractArrayFunction.EVENT_RESULT_ARRAY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(ArrayToArrayOfObjects.PARAMETER_ARRAY_SOURCE.getParameterName());

        let keys: string[] = context?.getArguments()?.get(KEY_NAME);

        if (!source?.length) {
            return new FunctionOutput([
                EventResult.outputOf(
                    new Map([[AbstractArrayFunction.EVENT_RESULT_ARRAY.getName(), []]]),
                ),
            ]);
        }

        let result: any[] = source.map((e) => {
            const obj: any = {};
            if (Array.isArray(e)) {
                if (keys.length) {
                    keys.forEach((key, index) => {
                        obj[key] = e[index];
                    });
                } else {
                    for (let i = 0; i < e.length; i++) {
                        obj[`value${i + 1}`] = e[i];
                    }
                }
            } else {
                obj[keys.length ? keys[0] : 'value'] = e;
            }

            return obj;
        });

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([[AbstractArrayFunction.EVENT_RESULT_ARRAY.getName(), result]]),
            ),
        ]);
    }
}
