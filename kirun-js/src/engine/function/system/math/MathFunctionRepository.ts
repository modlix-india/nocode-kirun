import { SchemaType } from '../../../json/schema/type/SchemaType';
import { Namespaces } from '../../../namespaces/Namespaces';
import { Repository } from '../../../Repository';
import { Function } from '../../Function';
import { Add } from './Add';
import { GenericMathFunction } from './GenericMathFunction';
import { Hypotenuse } from './Hypotenuse';
import { Maximum } from './Maximum';
import { Minimum } from './Minimum';
import { Random } from './Random';

const functionObjectsIndex: any = {
    Absolute: new GenericMathFunction(
        'Absolute',
        (v) => Math.abs(v),
        1,
        SchemaType.INTEGER,
        SchemaType.LONG,
        SchemaType.FLOAT,
        SchemaType.DOUBLE,
    ),
    ACosine: new GenericMathFunction('ArcCosine', (v) => Math.acos(v)),
    ASine: new GenericMathFunction('ArcSine', (v) => Math.asin(v)),
    ATangent: new GenericMathFunction('ArcTangent', (v) => Math.atan(v)),
    Ceiling: new GenericMathFunction('Ceiling', (v) => Math.ceil(v)),
    Cosine: new GenericMathFunction('Cosine', (v) => Math.cos(v)),
    CosineH: new GenericMathFunction('HyperbolicCosine', (v) => Math.cosh(v)),
    CubeRoot: new GenericMathFunction('CubeRoot', (v) => Math.cbrt(v)),
    Exponential: new GenericMathFunction('Exponential', (v) => Math.exp(v)),
    Expm1: new GenericMathFunction('ExponentialMinus1', (v) => Math.expm1(v)),
    Floor: new GenericMathFunction('Floor', (v) => Math.floor(v)),
    Log: new GenericMathFunction('LogNatural', (v) => Math.log(v)),
    Log10: new GenericMathFunction('Log10', (v) => Math.log10(v)),
    Round: new GenericMathFunction(
        'Round',
        (v) => Math.round(v),
        1,
        SchemaType.INTEGER,
        SchemaType.LONG,
    ),
    Sine: new GenericMathFunction('Sine', (v) => Math.sin(v)),
    SineH: new GenericMathFunction('HyperbolicSine', (v) => Math.sinh(v)),
    Tangent: new GenericMathFunction('Tangent', (v) => Math.tan(v)),
    TangentH: new GenericMathFunction('HyperbolicTangent', (v) => Math.tanh(v)),
    ToDegrees: new GenericMathFunction('ToDegrees', (v) => v * (Math.PI / 180)),
    ToRadians: new GenericMathFunction('ToRadians', (v) => v * (180 / Math.PI)),
    SquareRoot: new GenericMathFunction('SquareRoot', (v) => Math.sqrt(v)),
    ArcTangent: new GenericMathFunction('ArcTangent2', (v1, v2) => Math.atan2(v1, v2), 2),
    Power: new GenericMathFunction('Power', (v1, v2) => Math.pow(v1, v2), 2),
    Add: new Add(),
    Hypotenuse: new Hypotenuse(),
    Maximum: new Maximum(),
    Minimum: new Minimum(),
    Random: new Random(),
};

export class MathFunctionRepository implements Repository<Function> {
    find(namespace: string, name: string): Function {
        if (namespace != Namespaces.MATH) return null;

        return functionObjectsIndex[name];
    }
}
