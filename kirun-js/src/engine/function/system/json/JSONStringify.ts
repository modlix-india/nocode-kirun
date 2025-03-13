import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';

import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'value';

const SOURCE = 'source';

export class JSONStringify extends AbstractFunction {
    private readonly signature;

    public constructor() {
        super();
        this.signature = new FunctionSignature('JSONStringify')
            .setNamespace(Namespaces.SYSTEM_JSON)
            .setParameters(new Map([Parameter.ofEntry(SOURCE, Schema.ofAny(SOURCE))]))
            .setEvents(
                new Map([Event.outputEventMapEntry(new Map([[VALUE, Schema.ofString(VALUE)]]))]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source = context.getArguments()?.get('source');

        return new FunctionOutput([
            EventResult.outputOf(new Map([[VALUE, JSON.stringify(source ?? null)]])),
        ]);
    }
}
