from enum import Enum


class ConversionMode(str, Enum):
    STRICT = 'STRICT'
    LENIENT = 'LENIENT'
    USE_DEFAULT = 'USE_DEFAULT'
    SKIP = 'SKIP'
