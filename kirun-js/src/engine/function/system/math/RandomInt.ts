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

export class RandomInt extends AbstractFunction {
    public static readonly MIN_VALUE = 'minValue';

    public static readonly MAX_VALUE = 'maxValue';

    public static readonly VALUE = 'value';

    private static readonly SIGNATURE: FunctionSignature = new FunctionSignature('Random')
        .setParameters(
            MapUtil.of(
                RandomInt.MIN_VALUE,
                Parameter.of(
                    RandomInt.MIN_VALUE,
                    Schema.ofInteger(RandomInt.MIN_VALUE).setDefaultValue(0),
                ),
                RandomInt.MAX_VALUE,
                Parameter.of(
                    RandomInt.MAX_VALUE,
                    Schema.ofInteger(RandomInt.MAX_VALUE).setDefaultValue(2147483647),
                ),
            ),
        )
        .setNamespace(Namespaces.MATH)
        .setEvents(
            new Map<string, Event>([
                Event.outputEventMapEntry(
                    MapUtil.of(RandomInt.VALUE, Schema.ofInteger(RandomInt.VALUE)),
                ),
            ]),
        );

    public getSignature(): FunctionSignature {
        return RandomInt.SIGNATURE;
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let min: number = context.getArguments()?.get(RandomInt.MIN_VALUE);

        let max: number = context.getArguments()?.get(RandomInt.MAX_VALUE);

        let num: number = Math.floor(Math.random() * (max - min) + min);

        return new FunctionOutput([EventResult.outputOf(new Map([[RandomInt.VALUE, num]]))]);
    }
}
