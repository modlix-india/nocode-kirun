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
const ERROR = 'error';
const ERROR_MESSAGE = 'errorMessage';
const SOURCE = 'source';

export class JSONParse extends AbstractFunction {
    private readonly signature;

    public constructor() {
        super();
        this.signature = new FunctionSignature('JSONParse')
            .setNamespace(Namespaces.SYSTEM_JSON)
            .setParameters(new Map([Parameter.ofEntry(SOURCE, Schema.ofString(SOURCE))]))
            .setEvents(
                new Map([
                    Event.eventMapEntry(
                        ERROR,
                        new Map([[ERROR_MESSAGE, Schema.ofString(ERROR_MESSAGE)]]),
                    ),
                    Event.outputEventMapEntry(new Map([[VALUE, Schema.ofAny(VALUE)]])),
                ]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source = context.getArguments()?.get('source');

        let value;
        try {
            value = source ? JSON.parse(source) : null;
        } catch (err: any) {
            return new FunctionOutput([
                EventResult.of(
                    ERROR,
                    new Map([[ERROR_MESSAGE, err?.message ?? 'Unknown Error parsing JSON']]),
                ),
                EventResult.outputOf(new Map([[VALUE, null]])),
            ]);
        }

        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, value]]))]);
    }
}
