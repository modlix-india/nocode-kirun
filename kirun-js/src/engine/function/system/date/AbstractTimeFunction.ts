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
import { EventResult } from '../../../model/EventResult';
import { MapUtil } from '../../../util/MapUtil';
import { Function } from '../../Function';

export abstract class AbstractTimeFunction extends AbstractFunction {
    private signature: FunctionSignature;
    private VALUE: string = 'isodate';
    private OUTPUT: string = 'result';

    protected constructor(
        namespace: string,
        functionName: string,
        output: string = 'result',
        secondName?: string,
        ...schemaType: SchemaType[]
    ) {
        super();

        if (secondName) {
            if (secondName && isNullValue(schemaType)) {
                this.signature = new FunctionSignature(functionName)
                    .setNamespace(namespace)
                    .setParameters(
                        new Map([
                            [
                                this.VALUE,
                                new Parameter(
                                    this.VALUE,
                                    Schema.ofRef(`${Namespaces.DATE}.timeStamp`),
                                ),
                            ],
                            [
                                secondName,
                                new Parameter(secondName).setSchema(Schema.ofBoolean(secondName)),
                            ],
                        ]),
                    )
                    .setEvents(
                        new Map([
                            Event.outputEventMapEntry(
                                new Map([[this.OUTPUT, Schema.ofString(this.OUTPUT)]]),
                            ),
                        ]),
                    );
            } else {
                if (isNullValue(schemaType) || schemaType.length === 0)
                    schemaType = [SchemaType.INTEGER, SchemaType.INTEGER];

                this.signature = new FunctionSignature(functionName)
                    .setNamespace(namespace)
                    .setParameters(
                        new Map([
                            [
                                this.VALUE,
                                new Parameter(
                                    this.VALUE,
                                    Schema.ofRef(`${Namespaces.DATE}.timeStamp`),
                                ),
                            ],
                            [
                                secondName,
                                new Parameter(
                                    secondName,
                                    new Schema().setType(TypeUtil.of(schemaType[0])),
                                ),
                            ],
                        ]),
                    )
                    .setEvents(
                        new Map([
                            Event.outputEventMapEntry(
                                new Map([
                                    [
                                        output,
                                        new Schema()
                                            .setType(TypeUtil.of(schemaType[1]))
                                            .setName(output),
                                    ],
                                ]),
                            ),
                        ]),
                    );
            }
        } else {
            if (isNullValue(schemaType) || schemaType.length === 0)
                schemaType = [SchemaType.DOUBLE];

            this.signature = new FunctionSignature(functionName)
                .setNamespace(namespace)
                .setParameters(
                    new Map([
                        [
                            this.VALUE,
                            new Parameter(this.VALUE, Schema.ofRef(`${Namespaces.DATE}.timeStamp`)),
                        ],
                    ]),
                )
                .setEvents(
                    new Map([
                        Event.outputEventMapEntry(
                            new Map([
                                [
                                    output,
                                    new Schema()
                                        .setType(TypeUtil.of(schemaType[0]))
                                        .setName(output),
                                ],
                            ]),
                        ),
                    ]),
                );
        }
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    public static ofEntryDateAndStringWithOutputName(
        name: string,
        output: string,
        ufunction: (a: string) => number,
        ...schemaType: SchemaType[]
    ): [string, Function] {
        return [
            name,
            new (class extends AbstractTimeFunction {
                constructor(
                    namespace: string,
                    name: string,
                    output: string,
                    ...schemaType: SchemaType[]
                ) {
                    super(namespace, name, output, ...schemaType);
                }

                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    let date: string = context?.getArguments()?.get(this.VALUE);

                    if (!isValidZuluDate(date))
                        throw new KIRuntimeException(`Invalid ISO 8601 Date format.`);

                    return new FunctionOutput([
                        EventResult.outputOf(MapUtil.of(output, ufunction(date))),
                    ]);
                }
            })(Namespaces.DATE, name, output, ...schemaType),
        ];
    }

    public static ofEntryDateAndIntegerWithOutputDate(
        functionName: string,
        secondName: string,
        output: string,
        bifunction: (a: string, b: number) => string,
        ...schemaType: SchemaType[]
    ): [string, Function] {
        return [
            functionName,
            new (class extends AbstractTimeFunction {
                constructor(
                    secondName: string,
                    namespace: string,
                    name: string,
                    output: string,
                    ...schemaType: SchemaType[]
                ) {
                    super(namespace, name, output, secondName, ...schemaType);
                }

                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    let inputDate = context?.getArguments()?.get(this.VALUE);

                    if (!isValidZuluDate(inputDate))
                        throw new KIRuntimeException(`Please provide the valid iso date.`);

                    let addValue: number = context?.getArguments()?.get(secondName);

                    return new FunctionOutput([
                        EventResult.outputOf(MapUtil.of(output, bifunction(inputDate, addValue))),
                    ]);
                }
            })(secondName, Namespaces.DATE, functionName, output, ...schemaType),
        ];
    }

    public static ofEntryDateAndIntegerWithOutputInteger(
        functionName: string,
        secondName: string,
        output: string,
        bifunction: (a: string, b: number) => number,
        ...schemaType: SchemaType[]
    ): [string, Function] {
        return [
            functionName,
            new (class extends AbstractTimeFunction {
                constructor(
                    secondName: string,
                    namespace: string,
                    name: string,
                    output: string,
                    ...schemaType: SchemaType[]
                ) {
                    super(namespace, name, output, secondName, ...schemaType);
                }

                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    let inputDate = context?.getArguments()?.get(this.VALUE);

                    if (!isValidZuluDate(inputDate))
                        throw new KIRuntimeException(`Please provide the valid iso date.`);

                    let addValue = context?.getArguments()?.get(secondName);

                    return new FunctionOutput([
                        EventResult.outputOf(MapUtil.of(output, bifunction(inputDate, addValue))),
                    ]);
                }
            })(secondName, Namespaces.DATE, functionName, output, ...schemaType),
        ];
    }
}
