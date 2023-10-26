import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { TypeUtil } from '../../../json/schema/type/TypeUtil';
import { Event } from '../../../model/Event';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { isNullValue } from '../../../util/NullCheck';
import { AbstractFunction } from '../../AbstractFunction';
import isValidZuluDate from '../../../util/isValidISODate';

export abstract class AbstractTimeFunction extends AbstractFunction {
    private signature: FunctionSignature;
    private ISO_DATE: string = 'isodate';
    private dateFunction: (date1: string, date2?: string) => string;

    protected constructor(
        functionName: string,
        dateFunction: (date1: string, date2?: string) => string,
        outputName: string = 'result',
        ...schemaTypes: SchemaType[]
    ) {
        if (isNullValue(schemaTypes)) schemaTypes = [SchemaType.DOUBLE];

        super();
        this.dateFunction = dateFunction;
        this.signature = new FunctionSignature(functionName)
            .setNamespace(Namespaces.DATE)
            .setParameters(
                new Map([
                    [
                        this.ISO_DATE,
                        new Parameter(this.ISO_DATE).setSchema(
                            Schema.ofRef(`${Namespaces.DATE}.timeStamp`),
                        ),
                    ],
                ]),
            )
            .setEvents(
                new Map([
                    Event.outputEventMapEntry(
                        new Map([
                            [
                                outputName,
                                new Schema()
                                    .setType(TypeUtil.of(...schemaTypes))
                                    .setName(outputName),
                            ],
                        ]),
                    ),
                ]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        var inputDate = context.getArguments()?.get(this.ISO_DATE);

        if (!isValidZuluDate(inputDate))
            throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);
    }
}
