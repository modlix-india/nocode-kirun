from enum import Enum


class StringFormat(str, Enum):
    DATETIME = 'DATETIME'
    TIME = 'TIME'
    DATE = 'DATE'
    EMAIL = 'EMAIL'
    REGEX = 'REGEX'
