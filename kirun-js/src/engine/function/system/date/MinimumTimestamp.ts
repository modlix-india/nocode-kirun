import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

const iso8601Pattern =
    /^([+-]?\d{6}|\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2]\d|3[0-1])T([0-1]\d|2[0-3]):([0-5]\d):([0-5]\d)(\.\d{3})?(Z|([+-]([01]\d|2[0-3]):([0-5]\d)))?$/;

const VALUE = 'isodates';
const OUTPUT = 'result';
const SIGNATURE = new FunctionSignature('minimumTimestamp')
    .setNamespace(Namespaces.DATE)
    .setParameters(
        new Map([[VALUE, new Parameter(VALUE, Schema.ofAny(`${Namespaces.DATE}.timeStamp`))]]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofString(OUTPUT)]]))]));

export class MinimumTimestamp extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let date = context.getArguments()?.get(VALUE);

        let minimumTimeMap = new Map<string, number>();

        date?.forEach((currValue: string) => {
            const length = currValue.length;
            const sliceEnd = length === 24 || length === 29 ? 23 : 26;
            minimumTimeMap.set(currValue, new Date(currValue.slice(0, sliceEnd)).getTime());
        });

        const entriesArray: [string, number][] = Array.from(minimumTimeMap.entries());
        const [minimumTimeStamp, minimumTime] = entriesArray.reduce(
            (min, entry) => (entry[1] < min[1] ? entry : min),
            ['', Infinity],
        );

        return new FunctionOutput([EventResult.outputOf(new Map([[OUTPUT, minimumTimeStamp]]))]);
    }
}
