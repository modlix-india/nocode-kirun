import { KIRuntimeException } from '../../../exception/KIRuntimeException';
import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';
import { MapUtil } from '../../../util/MapUtil';
import { DateCompareUtil } from '../../../util/date/DateCompareUtil';
import isValidISO8601DateTime from '../../../util/date/isValidISODate';
import { AbstractFunction } from '../../AbstractFunction';
import { Function } from '../../Function';

export abstract class AbstractCompareDateFunction extends AbstractFunction {
    private signature: FunctionSignature;
    private ISO_DATE1: string = 'dateone';
    private ISO_DATE2: string = 'datetwo';
    private TIME_UNIT = 'unit';
    private OUTPUT: string = 'result';

    protected constructor(functionName: string, paramName?: string) {
        super();
        if (!paramName) {
            this.signature = new FunctionSignature(functionName)
                .setNamespace(Namespaces.DATE)
                .setParameters(
                    new Map([
                        [
                            this.ISO_DATE1,
                            new Parameter(
                                this.ISO_DATE1,
                                Schema.ofRef(`${Namespaces.DATE}.timeStamp`),
                            ),
                        ],
                        [
                            this.ISO_DATE2,
                            new Parameter(
                                this.ISO_DATE2,
                                Schema.ofRef(`${Namespaces.DATE}.timeStamp`),
                            ),
                        ],
                        [
                            this.TIME_UNIT,
                            new Parameter(
                                this.TIME_UNIT,
                                new Schema().setEnums([
                                    DateCompareUtil.YEAR,
                                    DateCompareUtil.MONTH,
                                    DateCompareUtil.DAY,
                                    DateCompareUtil.HOUR,
                                    DateCompareUtil.MINUTE,
                                    DateCompareUtil.SECOND,
                                ]),
                            ),
                        ],
                    ]),
                )
                .setEvents(
                    new Map([
                        Event.outputEventMapEntry(
                            new Map([[this.OUTPUT, Schema.ofBoolean(this.OUTPUT)]]),
                        ),
                    ]),
                );
        } else {
            this.signature = new FunctionSignature(functionName)
                .setNamespace(Namespaces.DATE)
                .setParameters(
                    new Map([
                        [
                            this.ISO_DATE1,
                            new Parameter(
                                this.ISO_DATE1,
                                Schema.ofRef(`${Namespaces.DATE}.timeStamp`),
                            ),
                        ],
                        [
                            this.ISO_DATE2,
                            new Parameter(
                                this.ISO_DATE2,
                                Schema.ofRef(`${Namespaces.DATE}.timeStamp`),
                            ),
                        ],
                        [
                            paramName,
                            new Parameter(paramName, Schema.ofRef(`${Namespaces.DATE}.timeStamp`)),
                        ],
                        [
                            this.TIME_UNIT,
                            new Parameter(
                                this.TIME_UNIT,
                                new Schema().setEnums([
                                    DateCompareUtil.YEAR,
                                    DateCompareUtil.MONTH,
                                    DateCompareUtil.DAY,
                                    DateCompareUtil.HOUR,
                                    DateCompareUtil.MINUTE,
                                    DateCompareUtil.SECOND,
                                ]),
                            ),
                        ],
                    ]),
                )
                .setEvents(
                    new Map([
                        Event.outputEventMapEntry(
                            new Map([[this.OUTPUT, Schema.ofBoolean(this.OUTPUT)]]),
                        ),
                    ]),
                );
        }
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    public static ofEntryTwoDateAndBooleanOutput(
        functionName: string,
        trifunction: (a: string, b: string, c: []) => boolean,
    ): [string, Function] {
        return [
            functionName,
            new (class extends AbstractCompareDateFunction {
                constructor(functionName: string) {
                    super(functionName);
                }

                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    let firstDate = context?.getArguments()?.get(this.ISO_DATE1);

                    let secondDate = context?.getArguments()?.get(this.ISO_DATE2);

                    if (!isValidISO8601DateTime(firstDate))
                        throw new KIRuntimeException(
                            `Please provide the valid ISO date for ` + this.ISO_DATE1,
                        );

                    if (!isValidISO8601DateTime(secondDate))
                        throw new KIRuntimeException(
                            `Please provide the valid ISO date for ` + this.ISO_DATE2,
                        );

                    let arr: [] = context?.getArguments()?.get(this.TIME_UNIT);

                    if (arr.length == 0) {
                        throw new KIRuntimeException(`Please provide a unit for checking`);
                    }

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(this.OUTPUT, trifunction(firstDate, secondDate, arr)),
                        ),
                    ]);
                }
            })(functionName),
        ];
    }

    public static ofEntryThreeDateAndBooleanOutput(
        functionName: string,
        paramName: string,
        quadfunction: (a: string, b: string, c: string, d: []) => boolean,
    ): [string, Function] {
        return [
            functionName,
            new (class extends AbstractCompareDateFunction {
                constructor(functionName: string, paramName: string) {
                    super(functionName, paramName);
                }

                protected async internalExecute(
                    context: FunctionExecutionParameters,
                ): Promise<FunctionOutput> {
                    let firstDate = context?.getArguments()?.get(this.ISO_DATE1);

                    let secondDate = context?.getArguments()?.get(this.ISO_DATE2);

                    let thirdDate = context?.getArguments()?.get(paramName);

                    if (!isValidISO8601DateTime(firstDate))
                        throw new KIRuntimeException(
                            `Please provide the valid ISO date for ` + this.ISO_DATE1,
                        );

                    if (!isValidISO8601DateTime(secondDate))
                        throw new KIRuntimeException(
                            `Please provide the valid ISO date for ` + this.ISO_DATE2,
                        );
                    if (!isValidISO8601DateTime(thirdDate))
                        throw new KIRuntimeException(
                            `Please provide the valid ISO date for ` + paramName,
                        );

                    let arr: [] = context?.getArguments()?.get(this.TIME_UNIT);

                    if (arr.length == 0) {
                        throw new KIRuntimeException(`Please provide a unit for checking`);
                    }

                    return new FunctionOutput([
                        EventResult.outputOf(
                            MapUtil.of(
                                this.OUTPUT,
                                quadfunction(firstDate, secondDate, thirdDate, arr),
                            ),
                        ),
                    ]);
                }
            })(functionName, paramName),
        ];
    }
}
