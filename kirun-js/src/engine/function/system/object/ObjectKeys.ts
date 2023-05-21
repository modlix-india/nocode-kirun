import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';

import { isNullValue } from '../../../util/NullCheck';
import { EventResult } from '../../../model/EventResult';
import { AbstractObjectFunction } from './AbstractObjectFunction';
import { Schema } from '../../../json/schema/Schema';
import { duplicate } from '../../../util/duplicate';

const VALUE = 'value';

export class ObjectKeys extends AbstractObjectFunction {
    public constructor() {
        super('ObjectKeys', Schema.ofArray(VALUE, Schema.ofString(VALUE)));
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var source = context.getArguments()?.get('source');

        if (isNullValue(source))
            return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, []]]))]);

        let keys: string[] = Object.keys(duplicate(source)).sort((a, b) => a.localeCompare(b));

        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, keys]]))]);
    }
}
