from __future__ import annotations

from typing import List, Optional

from kirun_py.json.schema.schema import AdditionalType, Schema
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.util.map_util import MapUtil


class KIRunSchemaRepository:

    def __init__(self) -> None:
        self._map: dict[str, Schema] = {
            'any': Schema.of_any('any').set_namespace(Namespaces.SYSTEM),
            'boolean': Schema.of_boolean('boolean').set_namespace(Namespaces.SYSTEM),
            'double': Schema.of_double('double').set_namespace(Namespaces.SYSTEM),
            'float': Schema.of_float('float').set_namespace(Namespaces.SYSTEM),
            'integer': Schema.of_integer('integer').set_namespace(Namespaces.SYSTEM),
            'long': Schema.of_long('long').set_namespace(Namespaces.SYSTEM),
            'number': Schema.of_number('number').set_namespace(Namespaces.SYSTEM),
            'string': Schema.of_string('string').set_namespace(Namespaces.SYSTEM),
            'Timestamp': Schema.of_string('Timestamp').set_namespace(Namespaces.DATE),
            'Timeunit': Schema.of_string('Timeunit')
                .set_namespace(Namespaces.DATE)
                .set_enums([
                    'YEARS', 'QUARTERS', 'MONTHS', 'WEEKS', 'DAYS',
                    'HOURS', 'MINUTES', 'SECONDS', 'MILLISECONDS',
                ]),
            'Duration': Schema.of_object('Duration')
                .set_namespace(Namespaces.DATE)
                .set_properties(MapUtil.of_array_entries(
                    ('years', Schema.of_number('years')),
                    ('quarters', Schema.of_number('quarters')),
                    ('months', Schema.of_number('months')),
                    ('weeks', Schema.of_number('weeks')),
                    ('days', Schema.of_number('days')),
                    ('hours', Schema.of_number('hours')),
                    ('minutes', Schema.of_number('minutes')),
                    ('seconds', Schema.of_number('seconds')),
                    ('milliseconds', Schema.of_number('milliseconds')),
                ))
                .set_additional_items(AdditionalType().set_boolean_value(False)),
            'TimeObject': Schema.of_object('TimeObject')
                .set_namespace(Namespaces.DATE)
                .set_properties(MapUtil.of_array_entries(
                    ('year', Schema.of_number('year')),
                    ('month', Schema.of_number('month')),
                    ('day', Schema.of_number('day')),
                    ('hour', Schema.of_number('hour')),
                    ('minute', Schema.of_number('minute')),
                    ('second', Schema.of_number('second')),
                    ('millisecond', Schema.of_number('millisecond')),
                ))
                .set_additional_items(AdditionalType().set_boolean_value(False)),
            Parameter.EXPRESSION.get_name(): Parameter.EXPRESSION,
            Schema.NULL.get_name(): Schema.NULL,
            Schema.SCHEMA.get_name(): Schema.SCHEMA,
        }

        self._filterable_names: List[str] = [
            s.get_full_name() for s in self._map.values()
        ]

    async def find(self, namespace: str, name: str) -> Optional[Schema]:
        if namespace != Namespaces.SYSTEM and namespace != Namespaces.DATE:
            return None
        return self._map.get(name)

    async def filter(self, name: str) -> List[str]:
        lower = name.lower()
        return [n for n in self._filterable_names if lower in n.lower()]
