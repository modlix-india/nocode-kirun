from enum import Enum


class ParameterReferenceType(str, Enum):
    VALUE = 'VALUE'
    EXPRESSION = 'EXPRESSION'
