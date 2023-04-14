import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

import { Namespaces } from '../../../namespaces/Namespaces';
import { Event } from '../../../model/Event';
import { Schema } from '../../../json/schema/Schema';
import { Parameter } from '../../../model/Parameter';
import { isNullValue } from '../../../util/NullCheck';
import { EventResult } from '../../../model/EventResult';

const VALUE = 'value';

const SOURCE = 'source';

export class ObjectValues extends AbstractFunction {
    private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('ObjectValues')
        .setNamespace(Namespaces.SYSTEM)
        .setParameters(new Map([[SOURCE, new Parameter(SOURCE, Schema.ofAny(SOURCE))]]))
        .setEvents(new Map([Event.outputEventMapEntry(new Map([[VALUE, Schema.ofArray(VALUE)]]))]));

    public getSignature(): FunctionSignature {
        return ObjectValues.SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var source = context.getArguments()?.get('source');

        if (isNullValue(source))
            return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, []]]))]);

        let objectValues: String[] = Object.values(source);

        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, objectValues]]))]);
    }
}
