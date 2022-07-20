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
import { PrimitiveUtil } from '../../../util/primitive/PrimitiveUtil';
import { AbstractFunction } from '../../AbstractFunction';

const VALUE = 'value';
const VALUE1 = 'value1';
const VALUE2 = 'value2';

const paramFunctions = [
    () => {
        return new Map([
            [VALUE, new Parameter().setParameterName(VALUE).setSchema(Schema.ofNumber(VALUE))],
        ]);
    },
    () => {
        return new Map([
            [VALUE1, new Parameter().setParameterName(VALUE1).setSchema(Schema.ofNumber(VALUE1))],
            [VALUE2, new Parameter().setParameterName(VALUE2).setSchema(Schema.ofNumber(VALUE2))],
        ]);
    },
];

export class GenericMathFunction extends AbstractFunction {
    private signature: FunctionSignature;
    private parametersNumber: number;
    private mathFunction: (v1: number, v2?: number) => number;

    public constructor(
        functionName: string,
        mathFunction: (v1: number, v2?: number) => number,
        parametersNumber: number = 1,
        returnType: SchemaType = SchemaType.DOUBLE,
    ) {
        super();
        this.parametersNumber = parametersNumber;
        this.mathFunction = mathFunction;
        this.signature = new FunctionSignature()
            .setName(functionName)
            .setNamespace(Namespaces.MATH)
            .setParameters(paramFunctions[parametersNumber - 1]())
            .setEvents(
                new Map([
                    Event.outputEventMapEntry(
                        new Map([
                            [VALUE, new Schema().setType(TypeUtil.of(returnType)).setName(VALUE)],
                        ]),
                    ) as [string, Event],
                ]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected internalExecute(context: FunctionExecutionParameters): FunctionOutput {
        let v1 = PrimitiveUtil.findPrimitiveNumberType(
            context.getArguments().get(this.parametersNumber == 1 ? VALUE : VALUE1),
        ).getT2();
        let v2;
        if (this.parametersNumber == 2)
            v2 = PrimitiveUtil.findPrimitiveNumberType(context.getArguments().get(VALUE2)).getT2();

        return new FunctionOutput([
            EventResult.outputOf(new Map([[VALUE, this.mathFunction.apply(v1, v2)]])),
        ]);
    }
}
