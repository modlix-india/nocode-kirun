import { FunctionOutput } from '../../../model/FunctionOutput';
import { FunctionExecutionParameters } from '../../../runtime/FunctionExecutionParameters';

import { Schema } from '../../../json/schema/Schema';
import { Event } from '../../../model/Event';
import { EventResult } from '../../../model/EventResult';
import { FunctionSignature } from '../../../model/FunctionSignature';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { duplicate } from '../../../util/duplicate';
import { AbstractFunction } from '../../AbstractFunction';
import { isNullValue } from '../../../util/NullCheck';
import { ObjectValueSetterExtractor } from '../../../runtime/expression/tokenextractor/ObjectValueSetterExtractor';

const VALUE = 'value';
const SOURCE = 'source';
const KEY = 'key';
const OVERWRITE = 'overwrite';
const DELETE_KEY_ON_NULL = 'deleteKeyOnNull';

export class ObjectPutValue extends AbstractFunction {
    private signature: FunctionSignature;

    public constructor() {
        super();
        this.signature = new FunctionSignature('ObjectPutValue')
            .setNamespace(Namespaces.SYSTEM_OBJECT)
            .setParameters(
                new Map([
                    Parameter.ofEntry(SOURCE, Schema.ofObject(SOURCE)),
                    Parameter.ofEntry(KEY, Schema.ofString(KEY)),
                    Parameter.ofEntry(VALUE, Schema.ofAny(VALUE)),
                    Parameter.ofEntry(OVERWRITE, Schema.ofBoolean(OVERWRITE).setDefaultValue(true)),
                    Parameter.ofEntry(
                        DELETE_KEY_ON_NULL,
                        Schema.ofBoolean(DELETE_KEY_ON_NULL).setDefaultValue(false),
                    ),
                ]),
            )
            .setEvents(
                new Map([Event.outputEventMapEntry(new Map([[VALUE, Schema.ofObject(VALUE)]]))]),
            );
    }

    public getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(context: FunctionExecutionParameters): Promise<FunctionOutput> {
        console.log(context);
        let source = context.getArguments()?.get(SOURCE);
        let key = context.getArguments()?.get(KEY);
        let value = context.getArguments()?.get(VALUE);
        let overwrite = context.getArguments()?.get(OVERWRITE);
        let deleteKeyOnNull = context.getArguments()?.get(DELETE_KEY_ON_NULL);

        const ove = new ObjectValueSetterExtractor(source, 'Data.');
        ove.setValue("Data."+key, value, overwrite, deleteKeyOnNull);
        
        return new FunctionOutput([EventResult.outputOf(new Map([[VALUE, ove.getStore()]]))]);
    }
}
