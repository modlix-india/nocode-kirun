import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';

import { isNullValue } from '../../../util/NullCheck';
import { EventResult } from '../../../model/EventResult';
import { AbstractObjectFunction } from './AbstractObjectFunction';
import { Schema } from '../../../json/schema/Schema';
import { duplicate } from '../../../util/duplicate';

const VALUE = 'value';

export class ObjectEntries extends AbstractObjectFunction {
    public constructor() {
        super(
            'ObjectEntries',
            Schema.ofArray(
                VALUE,
                Schema.ofArray('tuple', Schema.ofString('key'), Schema.ofAny('value')),
            ),
        );
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var source = context.getArguments()?.get('source');

        if (isNullValue(source))
            return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, []]]))]);

        let entries = Object.entries(duplicate(source)).sort((a, b) => a[0].localeCompare(b[0]));

        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, entries]]))]);
    }
}
