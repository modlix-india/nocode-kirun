import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

import { Namespaces } from '../../../namespaces/Namespaces';
import { Event } from '../../../model/Event';
import { Schema } from '../../../json/schema/Schema';
import { Parameter } from '../../../model/Parameter';

const VALUE = 'value';

const SOURCE = 'source';

export abstract class AbstractObjectFunction extends AbstractFunction {
    private signature: FunctionSignature;

    protected constructor(functionName: string, valueSchema: Schema) {
        super();
        this.signature = new FunctionSignature(functionName)
            .setNamespace(Namespaces.SYSTEM_OBJECT)
            .setParameters(new Map([Parameter.ofEntry(SOURCE, Schema.ofAny(SOURCE))]))
            .setEvents(new Map([Event.outputEventMapEntry(new Map([[VALUE, valueSchema]]))]));
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }
}
