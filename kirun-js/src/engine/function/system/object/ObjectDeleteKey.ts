import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';

import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { isNullValue } from '../../../util/NullCheck';
import { duplicate } from '../../../util/duplicate';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'value';
const SOURCE = 'source';
const KEY = 'key';

export class ObjectDeleteKey extends AbstractFunction {
    private signature: FunctionSignature;

    public constructor() {
        super();
        this.signature = new FunctionSignature('ObjectDeleteKey')
            .setNamespace(Namespaces.SYSTEM_OBJECT)
            .setParameters(
                new Map([
                    [SOURCE, new Parameter(SOURCE, Schema.ofAny(SOURCE))],
                    [KEY, new Parameter(KEY, Schema.ofString(KEY))],
                ]),
            )
            .setEvents(
                new Map([Event.outputEventMapEntry(new Map([[VALUE, Schema.ofAny(VALUE)]]))]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source = context.getArguments()?.get(SOURCE);
        let key = context.getArguments()?.get(KEY);

        if (isNullValue(source))
            return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, undefined]]))]);

        source = duplicate(source);
        delete source[key];

        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, source]]))]);
    }
}
