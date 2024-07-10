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

const VALUE = 'source';
const DELIMITTER = 'delimiter';
const OUTPUT = 'result';

const SIGNATURE = new FunctionSignature('Join')
    .setNamespace(Namespaces.SYSTEM_ARRAY)
    .setParameters(
        new Map([
            [
                VALUE,
                new Parameter(
                    VALUE,
                    Schema.ofArray(
                        VALUE,
                        Schema.of(
                            'each',
                            SchemaType.STRING,
                            SchemaType.INTEGER,
                            SchemaType.LONG,
                            SchemaType.DOUBLE,
                            SchemaType.FLOAT,
                            SchemaType.NULL,
                        ),
                    ),
                ),
            ],
            [
                DELIMITTER,
                new Parameter(DELIMITTER, Schema.ofString(DELIMITTER).setDefaultValue('')),
            ],
        ]),
    )
    .setEvents(new Map([Event.outputEventMapEntry(new Map([[OUTPUT, Schema.ofString(OUTPUT)]]))]));

export class Join extends AbstractFunction {
    public getSignature(): FunctionSignature {
        return SIGNATURE;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let source: any[] = context?.getArguments()?.get(VALUE);
        let delimitter: string = context?.getArguments()?.get(DELIMITTER);

        return new FunctionOutput([
            EventResult.outputOf(new Map([[OUTPUT, source.join(delimitter)]])),
        ]);
    }
}
