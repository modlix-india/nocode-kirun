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

const FROM = 'from';
const TO = 'to';
const STEP = 'step';
const VALUE = 'value';
const INDEX = 'index';

export class RangeLoop extends AbstractFunction {
    private readonly signature = new FunctionSignature('RangeLoop')
        .setNamespace(Namespaces.SYSTEM_LOOP)
        .setParameters(
            new Map([
                Parameter.ofEntry(
                    FROM,
                    Schema.of(
                        FROM,
                        SchemaType.INTEGER,
                        SchemaType.LONG,
                        SchemaType.FLOAT,
                        SchemaType.DOUBLE,
                    ).setDefaultValue(0),
                ),
                Parameter.ofEntry(
                    TO,
                    Schema.of(
                        TO,
                        SchemaType.INTEGER,
                        SchemaType.LONG,
                        SchemaType.FLOAT,
                        SchemaType.DOUBLE,
                    ).setDefaultValue(1),
                ),
                Parameter.ofEntry(
                    STEP,
                    Schema.of(
                        STEP,
                        SchemaType.INTEGER,
                        SchemaType.LONG,
                        SchemaType.FLOAT,
                        SchemaType.DOUBLE,
                    )
                        .setDefaultValue(1)
                        .setNot(new Schema().setConstant(0)),
                ),
            ]),
        )
        .setEvents(
            new Map([
                Event.eventMapEntry(
                    Event.ITERATION,
                    new Map([
                        [
                            INDEX,
                            Schema.of(
                                INDEX,
                                SchemaType.INTEGER,
                                SchemaType.LONG,
                                SchemaType.FLOAT,
                                SchemaType.DOUBLE,
                            ),
                        ],
                    ]),
                ),
                Event.outputEventMapEntry(
                    new Map([
                        [
                            VALUE,
                            Schema.of(
                                VALUE,
                                SchemaType.INTEGER,
                                SchemaType.LONG,
                                SchemaType.FLOAT,
                                SchemaType.DOUBLE,
                            ),
                        ],
                    ]),
                ),
            ]),
        );
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let from: number = context.getArguments()?.get(FROM);
        let to: number = context.getArguments()?.get(TO);
        let step: number = context.getArguments()?.get(STEP);

        const forward = step > 0;
        let current: number = from;
        let done: boolean = false;
        let statementName = context.getStatementExecution()?.getStatement()?.getStatementName();

        return new FunctionOutput({
            next(): EventResult | undefined {
                if (done) return undefined;

                if (
                    (forward && current >= to) ||
                    (!forward && current <= to) ||
                    (statementName && context.getExecutionContext()?.get(statementName)) //Check for break;
                ) {
                    done = true;
                    if (statementName) context.getExecutionContext()?.delete(statementName);
                    return EventResult.outputOf(new Map([[VALUE, current]]));
                }

                const eve = EventResult.of(Event.ITERATION, new Map([[INDEX, current]]));
                current += step;

                return eve;
            },
        });
    }
}
