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

const SOURCE = 'source';
const EACH = 'each';
const INDEX = 'index';
const VALUE = 'value';

const SIGNATURE = new FunctionSignature('CountLoop')
    .setNamespace(Namespaces.SYSTEM_LOOP)
    .setParameters(
        new Map([Parameter.ofEntry(SOURCE, Schema.ofArray(SOURCE, Schema.ofAny(SOURCE)))]),
    )
    .setEvents(
        new Map([
            Event.eventMapEntry(
                Event.ITERATION,
                new Map([
                    [INDEX, Schema.of(INDEX, SchemaType.INTEGER)],
                    [EACH, Schema.ofAny(EACH)],
                ]),
            ),
            Event.outputEventMapEntry(new Map([[VALUE, Schema.of(VALUE, SchemaType.INTEGER)]])),
        ]),
    );

export class ForEachLoop extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context.getArguments()?.get(SOURCE);

        let current = 0;

        let statementName = context.getStatementExecution()?.getStatement()?.getStatementName();

        return new FunctionOutput({
            next(): EventResult {
                if (
                    current >= source.length ||
                    (statementName && context.getExecutionContext()?.get(statementName)) //check for breaks;
                ) {
                    if (statementName) context.getExecutionContext()?.delete(statementName);
                    return EventResult.outputOf(new Map([[VALUE, source.length]]));
                }

                const eve = EventResult.of(
                    Event.ITERATION,
                    new Map([
                        [INDEX, current],
                        [EACH, source[current]],
                    ]),
                );
                ++current;

                return eve;
            },
        });
    }
}
