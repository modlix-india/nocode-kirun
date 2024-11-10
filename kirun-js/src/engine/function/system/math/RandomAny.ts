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

export class RandomAny extends AbstractFunction {
    public static readonly MIN_VALUE = 'minValue';

    public static readonly MAX_VALUE = 'maxValue';

    public static readonly VALUE = 'value';

    private readonly signature: FunctionSignature;

    private readonly randomFunction: (min: number, max: number) => number;

    public constructor(
        name: string,
        minParameter: Parameter,
        maxParameter: Parameter,
        outputSchema: Schema,
        randomFunction: (min: number, max: number) => number,
    ) {
        super();
        this.signature = new FunctionSignature(name)
            .setParameters(
                MapUtil.of(RandomAny.MIN_VALUE, minParameter, RandomAny.MAX_VALUE, maxParameter),
            )
            .setNamespace(Namespaces.MATH)
            .setEvents(
                new Map<string, Event>([
                    Event.outputEventMapEntry(MapUtil.of(RandomAny.VALUE, outputSchema)),
                ]),
            );
        this.randomFunction = randomFunction;
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        let min: number = context.getArguments()?.get(RandomAny.MIN_VALUE);

        let max: number = context.getArguments()?.get(RandomAny.MAX_VALUE);

        let num: number = this.randomFunction(min, max);

        return new FunctionOutput([EventResult.outputOf(new Map([[RandomAny.VALUE, num]]))]);
    }
}
