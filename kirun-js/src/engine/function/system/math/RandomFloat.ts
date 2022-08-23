import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';

export class RandomFloat extends AbstractFunction {
    public static readonly MIN_VALUE = 'minValue';

    public static readonly MAX_VALUE = 'maxValue';

    public static readonly VALUE = 'value';

    private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('Random')
        .setParameters(
            MapUtil.of(
                RandomFloat.MIN_VALUE,
                Parameter.of(
                    RandomFloat.MIN_VALUE,
                    Schema.ofFloat(RandomFloat.MIN_VALUE).setDefaultValue(0),
                ),
                RandomFloat.MAX_VALUE,
                Parameter.of(
                    RandomFloat.MAX_VALUE,
                    Schema.ofFloat(RandomFloat.MAX_VALUE).setDefaultValue(2147483647),
                ),
            ),
        )
        .setNamespace(Namespaces.MATH)
        .setEvents(
            new Map<string, Event>([
                Event.outputEventMapEntry(
                    MapUtil.of(RandomFloat.VALUE, Schema.ofFloat(RandomFloat.VALUE)),
                ),
            ]),
        );

    public getSignature(): FunctionSignature {
        return RandomFloat.SIGNATURE;
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let min: number = context.getArguments()?.get(RandomFloat.MIN_VALUE);

        let max: number = context.getArguments()?.get(RandomFloat.MAX_VALUE);

        let num: number = Math.floor(Math.random() * (max - min) + min);

        return new FunctionOutput([EventResult.outputOf(new Map([[RandomFloat.VALUE, num]]))]);
    }
}
