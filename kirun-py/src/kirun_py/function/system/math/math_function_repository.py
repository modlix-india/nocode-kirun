from __future__ import annotations

import math
import random
import sys
from typing import Dict, List, Optional

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.function.system.math.add import Add
from kirun_py.function.system.math.generic_math_function import GenericMathFunction
from kirun_py.function.system.math.hypotenuse import Hypotenuse
from kirun_py.function.system.math.maximum import Maximum
from kirun_py.function.system.math.minimum import Minimum
from kirun_py.function.system.math.random_ import Random
from kirun_py.function.system.math.random_any import RandomAny
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces


class MathFunctionRepository:

    def __init__(self) -> None:
        self._function_objects_index: Dict[str, AbstractFunction] = {
            'Absolute': GenericMathFunction(
                'Absolute',
                lambda v: abs(v),
                1,
                [SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT, SchemaType.DOUBLE],
            ),
            'ArcCosine': GenericMathFunction('ArcCosine', math.acos),
            'ArcSine': GenericMathFunction('ArcSine', math.asin),
            'ArcTangent': GenericMathFunction('ArcTangent', math.atan),
            'Ceiling': GenericMathFunction('Ceiling', math.ceil),
            'Cosine': GenericMathFunction('Cosine', math.cos),
            'HyperbolicCosine': GenericMathFunction('HyperbolicCosine', math.cosh),
            'CubeRoot': GenericMathFunction('CubeRoot', lambda v: math.copysign(abs(v) ** (1.0 / 3.0), v)),
            'Exponential': GenericMathFunction('Exponential', math.exp),
            'ExponentialMinus1': GenericMathFunction('ExponentialMinus1', math.expm1),
            'Floor': GenericMathFunction('Floor', math.floor),
            'LogNatural': GenericMathFunction('LogNatural', math.log),
            'Log10': GenericMathFunction('Log10', math.log10),
            'Round': GenericMathFunction(
                'Round',
                lambda v: round(v),
                1,
                [SchemaType.INTEGER, SchemaType.LONG],
            ),
            'Sine': GenericMathFunction('Sine', math.sin),
            'HyperbolicSine': GenericMathFunction('HyperbolicSine', math.sinh),
            'Tangent': GenericMathFunction('Tangent', math.tan),
            'HyperbolicTangent': GenericMathFunction('HyperbolicTangent', math.tanh),
            'ToDegrees': GenericMathFunction('ToDegrees', math.degrees),
            'ToRadians': GenericMathFunction('ToRadians', math.radians),
            'SquareRoot': GenericMathFunction('SquareRoot', math.sqrt),
            'ArcTangent2': GenericMathFunction('ArcTangent2', math.atan2, 2),
            'Power': GenericMathFunction('Power', math.pow, 2),
            'Add': Add(),
            'Hypotenuse': Hypotenuse(),
            'Maximum': Maximum(),
            'Minimum': Minimum(),
            'Random': Random(),
            'RandomFloat': RandomAny(
                'RandomFloat',
                Parameter.of(
                    'minValue',
                    Schema.of_float('minValue').set_default_value(0),
                ),
                Parameter.of(
                    'maxValue',
                    Schema.of_float('maxValue').set_default_value(2147483647),
                ),
                Schema.of_float('value'),
                lambda mn, mx: random.random() * (mx - mn) + mn,
            ),
            'RandomInt': RandomAny(
                'RandomInt',
                Parameter.of(
                    'minValue',
                    Schema.of_integer('minValue').set_default_value(0),
                ),
                Parameter.of(
                    'maxValue',
                    Schema.of_integer('maxValue').set_default_value(2147483647),
                ),
                Schema.of_integer('value'),
                lambda mn, mx: round(random.random() * (mx - mn) + mn),
            ),
            'RandomLong': RandomAny(
                'RandomLong',
                Parameter.of(
                    'minValue',
                    Schema.of_long('minValue').set_default_value(0),
                ),
                Parameter.of(
                    'maxValue',
                    Schema.of_long('maxValue').set_default_value(sys.maxsize),
                ),
                Schema.of_long('value'),
                lambda mn, mx: round(random.random() * (mx - mn) + mn),
            ),
            'RandomDouble': RandomAny(
                'RandomDouble',
                Parameter.of(
                    'minValue',
                    Schema.of_double('minValue').set_default_value(0),
                ),
                Parameter.of(
                    'maxValue',
                    Schema.of_double('maxValue').set_default_value(sys.float_info.max),
                ),
                Schema.of_double('value'),
                lambda mn, mx: random.random() * (mx - mn) + mn,
            ),
        }

        self._filterable_names: List[str] = [
            fn.get_signature().get_full_name()
            for fn in self._function_objects_index.values()
        ]

    async def find(self, namespace: str, name: str) -> Optional[AbstractFunction]:
        if namespace != Namespaces.MATH:
            return None
        return self._function_objects_index.get(name)

    async def filter(self, name: str) -> List[str]:
        lower = name.lower()
        return [n for n in self._filterable_names if lower in n.lower()]
