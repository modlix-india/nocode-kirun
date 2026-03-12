from enum import Enum


class ParameterType(str, Enum):
    CONSTANT = 'CONSTANT'
    EXPRESSION = 'EXPRESSION'
