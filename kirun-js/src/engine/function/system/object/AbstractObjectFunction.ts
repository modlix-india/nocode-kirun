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
    private SIGNATURE: FunctionSignature;

    protected constructor(functionName: string) {
        super();
        this.SIGNATURE = new FunctionSignature(functionName)
            .setNamespace(Namespaces.SYSTEM)
            .setParameters(new Map([[SOURCE, new Parameter(SOURCE, Schema.ofAny(SOURCE))]]))
            .setEvents(
                new Map([Event.outputEventMapEntry(new Map([[VALUE, Schema.ofArray(VALUE)]]))]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.SIGNATURE;
    }
}
