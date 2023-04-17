import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';

import { isNullValue } from '../../../util/NullCheck';
import { EventResult } from '../../../model/EventResult';
import { AbstractObjectFunction } from './AbstractObjectFunction';

const VALUE = 'value';

export class ObjectValues extends AbstractObjectFunction {
    public constructor() {
        super('ObjectValues');
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var source = context.getArguments()?.get('source');

        if (isNullValue(source))
            return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, []]]))]);

        let objectValues: String[] = Object.values(source);

        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, objectValues]]))]);
    }
}
