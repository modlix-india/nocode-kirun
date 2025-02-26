import { createHash } from 'crypto';
import { AbstractFunction } from '../AbstractFunction';
import { FunctionSignature } from '../../model/FunctionSignature';
import { Parameter } from '../../model/Parameter';
import { Schema } from '../../json/schema/Schema';
import { Event } from '../../model/Event';
import { Namespaces } from '../../namespaces/Namespaces';
import { FunctionExecutionParameters } from '../../runtime/FunctionExecutionParameters';
import { FunctionOutput } from '../../model/FunctionOutput';
import { EventResult } from '../../model/EventResult';
import { MapUtil } from '../../util/MapUtil';

export class HashData extends AbstractFunction {
    private static readonly DEFAULT_ALGORITHM = 'sha256';
    public static readonly PARAMETER_DATA = 'data';
    public static readonly PARAMETER_ALGORITHM = 'algorithm';
    public static readonly PARAMETER_PRIMITIVE_LEVEL = 'primitiveLevel';
    public static readonly EVENT_RESULT_NAME = 'result';

    private readonly signature: FunctionSignature;

    public constructor() {
        super();
        this.signature = new FunctionSignature('Hash')
            .setNamespace(Namespaces.SYSTEM)
            .setParameters(
                new Map([
                    Parameter.ofEntry(
                        HashData.PARAMETER_DATA,
                        Schema.ofAny(HashData.PARAMETER_DATA)
                    ),
                    Parameter.ofEntry(
                        HashData.PARAMETER_ALGORITHM,
                        Schema.ofString(HashData.PARAMETER_ALGORITHM)
                            .setEnums([
                               'sha256',
                               'sha384',
                               'sha512',
                               'md5',
                               'md2',
                               'md4',
                               'sha1'])
                            .setDefaultValue(HashData.DEFAULT_ALGORITHM)
                    ),
                    Parameter.ofEntry(
                        HashData.PARAMETER_PRIMITIVE_LEVEL,
                        Schema.ofBoolean(HashData.PARAMETER_PRIMITIVE_LEVEL)
                            .setDefaultValue(false)
                    )
                ])
            )
            .setEvents(
                new Map([
                    [
                        Event.OUTPUT,
                        new Event(
                            Event.OUTPUT,
                            MapUtil.of(
                                HashData.EVENT_RESULT_NAME,
                                Schema.ofString(HashData.EVENT_RESULT_NAME)
                            )
                        )
                    ]
                ])
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        const data = context.getArguments()?.get(HashData.PARAMETER_DATA);
        const algorithm = context.getArguments()?.get(HashData.PARAMETER_ALGORITHM) || HashData.DEFAULT_ALGORITHM;
        const primitiveLevel = context.getArguments()?.get(HashData.PARAMETER_PRIMITIVE_LEVEL);

        const result = primitiveLevel ? 
            this.processPrimitiveLevelData(data, algorithm) : 
            (data === null || data === undefined) ? 
                "null" : 
                createHash(algorithm).update(JSON.stringify(data)).digest('hex');

        return new FunctionOutput([
            EventResult.outputOf(new Map([[HashData.EVENT_RESULT_NAME, result]]))
        ]);
    }

    private processPrimitiveLevelData(value: any, algorithm: string): any {
        if (value === null || value === undefined) return "null";
        
        if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
            return createHash(algorithm).update(JSON.stringify(value)).digest('hex');
        }

        if (Array.isArray(value)) {
            return value.map(item => this.processPrimitiveLevelData(item, algorithm));
        }

        if (typeof value === 'object') {
            const result: any = {};
            Object.entries(value).forEach(([key, val]) => {
                result[createHash(algorithm).update(JSON.stringify(key)).digest('hex')] = 
                    this.processPrimitiveLevelData(val, algorithm);
            });
            return result;
        }

        return createHash(algorithm).update(String(value)).digest('hex');
    }
}
