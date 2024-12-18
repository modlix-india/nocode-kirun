import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

const COUNT = 'count';
const VALUE = 'value';
const INDEX = 'index';

export class CountLoop extends AbstractFunction {
    private readonly signature = new FunctionSignature('CountLoop')
        .setNamespace(Namespaces.SYSTEM_LOOP)
        .setParameters(new Map([Parameter.ofEntry(COUNT, Schema.of(COUNT, SchemaType.INTEGER))]))
        .setEvents(
            new Map([
                Event.eventMapEntry(
                    Event.ITERATION,
                    new Map([[INDEX, Schema.of(INDEX, SchemaType.INTEGER)]]),
                ),
                Event.outputEventMapEntry(new Map([[VALUE, Schema.of(VALUE, SchemaType.INTEGER)]])),
            ]),
        );
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let count: number = context.getArguments()?.get(COUNT);
        let current = 0;
        let statementName = context.getStatementExecution()?.getStatement()?.getStatementName();

        return new FunctionOutput({
            next(): EventResult {
                if (
                    current >= count ||
                    (statementName && context.getExecutionContext()?.get(statementName))
                ) {
                    // check for break;
                    if (statementName) context.getExecutionContext()?.delete(statementName);
                    return EventResult.outputOf(new Map([[VALUE, current]]));
                }

                const eve = EventResult.of(Event.ITERATION, new Map([[INDEX, current]]));
                ++current;

                return eve;
            },
        });
    }
}
