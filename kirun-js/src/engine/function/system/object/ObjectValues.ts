import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';

import { isNullValue } from '../../../util/NullCheck';
import { EventResult } from '../../../model/EventResult';
import { AbstractObjectFunction } from './AbstractObjectFunction';
import { Schema } from '../../../json/schema/Schema';
import { duplicate } from '../../../util/duplicate';

const VALUE = 'value';

export class ObjectValues extends AbstractObjectFunction {
    public constructor() {
        super('ObjectValues', Schema.ofArray(VALUE, Schema.ofAny(VALUE)));
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var source = context.getArguments()?.get('source');

        if (isNullValue(source))
            return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, []]]))]);

        let objectValues: any[] = Object.entries(duplicate(source))
            .sort((a, b) => a[0].localeCompare(b[0]))
            .map((e) => e[1]);

        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, objectValues]]))]);
    }
}
