import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { AbstractFunction } from '../../AbstractFunction';
import { MapUtil } from '../../../util/MapUtil';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { TypeUtil } from '../../../json/schema/type/TypeUtil';
import { SchemaType } from '../../../json/schema/type/SchemaType';

export class Reverse extends AbstractFunction {
    protected readonly VALUE: string = 'value';

    private readonly signature: FunctionSignature = new FunctionSignature('Reverse')
        .setNamespace(Namespaces.STRING)
        .setParameters(
            new Map([
                [
                    this.VALUE,
                    new Parameter(this.VALUE, Schema.ofString(this.VALUE)).setVariableArgument(
                        false,
                    ),
                ],
            ]),
        )
        .setEvents(
            new Map([
                Event.outputEventMapEntry(
                    new Map([
                        [
                            this.VALUE,
                            new Schema()
                                .setType(TypeUtil.of(SchemaType.STRING))
                                .setName(this.VALUE),
                        ],
                    ]),
                ),
            ]),
        );

    public constructor() {
        super();
    }

    private readonly signature = this.signature;
    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let acutalString: string = context.getArguments()?.get(this.VALUE);
        let stringLength: number = acutalString.length - 1;
        let reversedString: string = '';

        while (stringLength >= 0) {
            reversedString += acutalString.charAt(stringLength--);
        }

        return new FunctionOutput([EventResult.outputOf(MapUtil.of(this.VALUE, reversedString))]);
    }
}
