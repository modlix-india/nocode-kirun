import { Schema } from '../../../json/schema/Schema';
import { SchemaType } from '../../../json/schema/type/SchemaType';
import { Parameter } from '../../../model/Parameter';
import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import { AbstractFunction } from '../../AbstractFunction';
import { Function } from '../../Function';
import { Add } from './Add';
import { GenericMathFunction } from './GenericMathFunction';
import { Hypotenuse } from './Hypotenuse';
import { Maximum } from './Maximum';
import { Minimum } from './Minimum';
import { Random } from './Random';
import { RandomAny } from './RandomAny';

const functionObjectsIndex: { [key: string]: AbstractFunction } = {
    Absolute: new GenericMathFunction(
        'Absolute',
        (v) => Math.abs(v),
        1,
        SchemaType.INTEGER,
        SchemaType.LONG,
        SchemaType.FLOAT,
        SchemaType.DOUBLE,
    ),
    ArcCosine: new GenericMathFunction('ArcCosine', (v) => Math.acos(v)),
    ArcSine: new GenericMathFunction('ArcSine', (v) => Math.asin(v)),
    ArcTangent: new GenericMathFunction('ArcTangent', (v) => Math.atan(v)),
    Ceiling: new GenericMathFunction('Ceiling', (v) => Math.ceil(v)),
    Cosine: new GenericMathFunction('Cosine', (v) => Math.cos(v)),
    HyperbolicCosine: new GenericMathFunction('HyperbolicCosine', (v) => Math.cosh(v)),
    CubeRoot: new GenericMathFunction('CubeRoot', (v) => Math.cbrt(v)),
    Exponential: new GenericMathFunction('Exponential', (v) => Math.exp(v)),
    ExponentialMinus1: new GenericMathFunction('ExponentialMinus1', (v) => Math.expm1(v)),
    Floor: new GenericMathFunction('Floor', (v) => Math.floor(v)),
    LogNatural: new GenericMathFunction('LogNatural', (v) => Math.log(v)),
    Log10: new GenericMathFunction('Log10', (v) => Math.log10(v)),
    Round: new GenericMathFunction(
        'Round',
        (v) => Math.round(v),
        1,
        SchemaType.INTEGER,
        SchemaType.LONG,
    ),
    Sine: new GenericMathFunction('Sine', (v) => Math.sin(v)),
    HyperbolicSine: new GenericMathFunction('HyperbolicSine', (v) => Math.sinh(v)),
    Tangent: new GenericMathFunction('Tangent', (v) => Math.tan(v)),
    HyperbolicTangent: new GenericMathFunction('HyperbolicTangent', (v) => Math.tanh(v)),
    ToDegrees: new GenericMathFunction('ToDegrees', (v) => v * (Math.PI / 180)),
    ToRadians: new GenericMathFunction('ToRadians', (v) => v * (180 / Math.PI)),
    SquareRoot: new GenericMathFunction('SquareRoot', (v) => Math.sqrt(v)),
    ArcTangent2: new GenericMathFunction('ArcTangent2', (v1, v2) => Math.atan2(v1, v2!), 2),
    Power: new GenericMathFunction('Power', (v1, v2) => Math.pow(v1, v2!), 2),
    Add: new Add(),
    Hypotenuse: new Hypotenuse(),
    Maximum: new Maximum(),
    Minimum: new Minimum(),
    Random: new Random(),
    RandomFloat: new RandomAny(
        'RandomFloat',
        Parameter.of(RandomAny.MIN_VALUE, Schema.ofFloat(RandomAny.MIN_VALUE).setDefaultValue(0)),
        Parameter.of(
            RandomAny.MAX_VALUE,
            Schema.ofFloat(RandomAny.MAX_VALUE).setDefaultValue(2147483647),
        ),
        Schema.ofFloat(RandomAny.VALUE),
        (min, max) => Math.random() * (max - min) + min,
    ),
    RandomInt: new RandomAny(
        'RandomInt',
        Parameter.of(RandomAny.MIN_VALUE, Schema.ofInteger(RandomAny.MIN_VALUE).setDefaultValue(0)),
        Parameter.of(
            RandomAny.MAX_VALUE,
            Schema.ofInteger(RandomAny.MAX_VALUE).setDefaultValue(2147483647),
        ),
        Schema.ofInteger(RandomAny.VALUE),
        (min, max) => Math.round(Math.random() * (max - min) + min),
    ),
    RandomLong: new RandomAny(
        'RandomLong',
        Parameter.of(RandomAny.MIN_VALUE, Schema.ofLong(RandomAny.MIN_VALUE).setDefaultValue(0)),
        Parameter.of(
            RandomAny.MAX_VALUE,
            Schema.ofLong(RandomAny.MAX_VALUE).setDefaultValue(Number.MAX_SAFE_INTEGER),
        ),
        Schema.ofLong(RandomAny.VALUE),
        (min, max) => Math.round(Math.random() * (max - min) + min),
    ),
    RandomDouble: new RandomAny(
        'RandomDouble',
        Parameter.of(RandomAny.MIN_VALUE, Schema.ofDouble(RandomAny.MIN_VALUE).setDefaultValue(0)),
        Parameter.of(
            RandomAny.MAX_VALUE,
            Schema.ofDouble(RandomAny.MAX_VALUE).setDefaultValue(Number.MAX_VALUE),
        ),
        Schema.ofDouble(RandomAny.VALUE),
        (min, max) => Math.random() * (max - min) + min,
    ),
};

const filterableNames = Object.values(functionObjectsIndex).map((e) =>
    e.getSignature().getFullName(),
);

export class MathFunctionRepository implements Repository<Function> {
    public async find(namespace: string, name: string): Promise<Function | undefined> {
        if (namespace != Namespaces.MATH) return Promise.resolve(undefined);

        return Promise.resolve(functionObjectsIndex[name]);
    }

    public async filter(name: string): Promise<string[]> {
        return Promise.resolve(
            filterableNames.filter((e) => e.toLowerCase().indexOf(name.toLowerCase()) !== -1),
        );
    }
}
