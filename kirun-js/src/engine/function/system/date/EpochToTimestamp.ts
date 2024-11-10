import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { TypeUtil } from '../../../json/schema/type/TypeUtil';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { AbstractFunction } from '../../AbstractFunction';
import { AbstractDateFunction } from './AbstractDateFunction';

export class EpochToTimestamp extends AbstractFunction {
    private readonly signature: FunctionSignature;
    private readonly isSeconds: boolean;
    private readonly paramName: string;

    constructor(name: string, isSeconds: boolean) {
        super();
        this.paramName = `epoch${isSeconds ? 'Seconds' : 'Milliseconds'}`;
        this.isSeconds = isSeconds;
        this.signature = new FunctionSignature(name)
            .setNamespace(Namespaces.DATE)
            .setParameters(
                new Map([
                    [
                        this.paramName,
                        Parameter.of(
                            this.paramName,
                            new Schema()
                                .setName(this.paramName)
                                .setType(
                                    TypeUtil.of(
                                        SchemaType.LONG,
                                        SchemaType.INTEGER,
                                        SchemaType.STRING,
                                    ),
                                ),
                        ),
                    ],
                ]),
            )
            .setEvents(
                new Map([
                    [
                        AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                        AbstractDateFunction.EVENT_TIMESTAMP,
                    ],
                ]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const epoch = parseInt(context.getArguments()?.get(this.paramName));
        const timestamp = this.isSeconds ? epoch * 1000 : epoch;
        if (isNaN(timestamp)) {
            throw new Error(`Please provide a valid value for ${this.paramName}.`);
        }
        return Promise.resolve(
            new FunctionOutput([
                EventResult.outputOf(
                    MapUtil.of(
                        AbstractDateFunction.EVENT_TIMESTAMP_NAME,
                        new Date(timestamp).toISOString(),
                    ),
                ),
            ]),
        );
    }
}
