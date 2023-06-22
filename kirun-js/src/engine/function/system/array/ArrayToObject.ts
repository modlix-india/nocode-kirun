import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { Parameter } from '../../../model/Parameter';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { ObjectValueSetterExtractor } from '../../../runtime/expression/tokenextractor/ObjectValueSetterExtractor';
import { isNullValue } from '../../../util/NullCheck';
import { AbstractArrayFunction } from './AbstractArrayFunction';

const KEY_PATH = 'keyPath';
const VALUE_PATH = 'valuePath';
const IGNORE_NULL_VALUES = 'ignoreNullValues';
const IGNORE_NULL_KEYS = 'ignoreNullKeys';
const IGNORE_DUPLICATE_KEYS = 'ignoreDuplicateKeys';

export class ArrayToObject extends AbstractArrayFunction {
    public constructor() {
        super(
            'ArrayToObjects',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                Parameter.of(KEY_PATH, Schema.ofString(KEY_PATH)),
                Parameter.of(VALUE_PATH, Schema.of(VALUE_PATH, SchemaType.STRING, SchemaType.NULL)),
                Parameter.of(
                    IGNORE_NULL_VALUES,
                    Schema.ofBoolean(IGNORE_NULL_VALUES).setDefaultValue(false),
                ),
                Parameter.of(
                    IGNORE_NULL_KEYS,
                    Schema.ofBoolean(IGNORE_NULL_KEYS).setDefaultValue(true),
                ),
                Parameter.of(
                    IGNORE_DUPLICATE_KEYS,
                    Schema.ofBoolean(IGNORE_DUPLICATE_KEYS).setDefaultValue(false),
                ),
            ],
            AbstractArrayFunction.EVENT_RESULT_ANY,
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context
            ?.getArguments()
            ?.get(AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.getParameterName());

        let keyPath: string = context?.getArguments()?.get(KEY_PATH);
        let valuePath: string = context?.getArguments()?.get(VALUE_PATH) ?? '';

        let ignoreNullValues: boolean = context?.getArguments()?.get(IGNORE_NULL_VALUES);
        let ignoreNullKeys: boolean = context?.getArguments()?.get(IGNORE_NULL_KEYS);
        let ignoreDuplicateKeys: boolean = context?.getArguments()?.get(IGNORE_DUPLICATE_KEYS);

        const ove: ObjectValueSetterExtractor = new ObjectValueSetterExtractor({}, 'Data.');

        const result: any = source
            .filter((e) => !isNullValue(e))
            .reduce((acc: any, cur: any) => {
                ove.setStore(cur);

                const key = ove.getValue('Data.' + keyPath);
                if (ignoreNullKeys && isNullValue(key)) return acc;

                const value = valuePath ? ove.getValue('Data.' + valuePath) : cur;
                if (ignoreNullValues && isNullValue(value)) return acc;

                if (ignoreDuplicateKeys && acc.hasOwnProperty(key)) return acc;

                acc[key] = value;
                return acc;
            }, {});

        return new FunctionOutput([
            EventResult.outputOf(new Map([[AbstractArrayFunction.EVENT_RESULT_NAME, result]])),
        ]);
    }
}
