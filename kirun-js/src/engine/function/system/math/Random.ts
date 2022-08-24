import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'value';
export class Random extends AbstractFunction {
    private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('Random')
        .setNamespace(Namespaces.MATH)
        .setEvents(
            new Map<string, Event>([
                Event.outputEventMapEntry(MapUtil.of(VALUE, Schema.ofDouble(VALUE))),
            ]),
        );

    public getSignature(): FunctionSignature {
        return Random.SIGNATURE;
    }
    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, Math.random()]]))]);
    }
}
