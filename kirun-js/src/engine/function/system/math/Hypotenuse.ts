import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { TypeUtil } from '../../../json/schema/type/TypeUtil';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'value';

export class Hypotenuse extends AbstractFunction {
    private readonly signature: FunctionSignature = new FunctionSignature('Hypotenuse')
        .setNamespace(Namespaces.MATH)
        .setParameters(
            new Map([
                [VALUE, new Parameter(VALUE, Schema.ofNumber(VALUE)).setVariableArgument(true)],
            ]),
        )
        .setEvents(
            new Map([
                Event.outputEventMapEntry(
                    new Map([
                        [
                            VALUE,
                            new Schema().setType(TypeUtil.of(SchemaType.DOUBLE)).setName(VALUE),
                        ],
                    ]),
                ) as [string, Event],
            ]),
        );

    public constructor() {
        super();
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let nums: number[] = context.getArguments()?.get(VALUE);

        return new FunctionOutput([
            EventResult.outputOf(
                new Map([[VALUE, Math.sqrt(nums.reduce((a, c) => (a += c * c), 0))]]),
            ),
        ]);
    }
}
