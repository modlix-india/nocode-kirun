from enum import Enum


class SchemaType(str, Enum):
    INTEGER = 'Integer'
    LONG = 'Long'
    FLOAT = 'Float'
    DOUBLE = 'Double'
    STRING = 'String'
    OBJECT = 'Object'
    ARRAY = 'Array'
    BOOLEAN = 'Boolean'
    NULL = 'Null'
