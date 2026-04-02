from __future__ import annotations

import re
from typing import Dict, List, Optional, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.function.system.string.abstract_string_function import AbstractStringFunction
from kirun_py.function.system.string.concatenate import Concatenate
from kirun_py.function.system.string.delete_for_given_length import DeleteForGivenLength
from kirun_py.function.system.string.insert_at_given_position import InsertAtGivenPosition
from kirun_py.function.system.string.matches import Matches
from kirun_py.function.system.string.post_pad import PostPad
from kirun_py.function.system.string.pre_pad import PrePad
from kirun_py.function.system.string.region_matches import RegionMatches
from kirun_py.function.system.string.replace_at_given_position import ReplaceAtGivenPosition
from kirun_py.function.system.string.reverse import Reverse
from kirun_py.function.system.string.split import Split
from kirun_py.function.system.string.to_string import ToString
from kirun_py.function.system.string.trim_to import TrimTo
from kirun_py.namespaces.namespaces import Namespaces


def _frequency(a: str, b: str) -> int:
    count = 0
    index = a.find(b)
    while index != -1:
        count += 1
        index = a.find(b, index + 1)
    return count


def _map_entry(func: AbstractFunction) -> tuple:
    sig = func.get_signature()
    return (sig.get_name(), func)


class StringFunctionRepository:

    def __init__(self) -> None:
        self._repo_map: Dict[str, AbstractFunction] = dict([
            # Single string -> string
            AbstractStringFunction.of_entry_string_and_string_output(
                'Trim', lambda e: e.strip()
            ),
            AbstractStringFunction.of_entry_string_and_string_output(
                'TrimStart', lambda e: e.lstrip()
            ),
            AbstractStringFunction.of_entry_string_and_string_output(
                'TrimEnd', lambda e: e.rstrip()
            ),

            # Single string -> integer
            AbstractStringFunction.of_entry_string_and_integer_output(
                'Length', lambda e: len(e)
            ),

            # String, string -> integer
            AbstractStringFunction.of_entry_string_string_and_integer_output(
                'Frequency', _frequency
            ),

            # Single string -> string (case)
            AbstractStringFunction.of_entry_string_and_string_output(
                'LowerCase', lambda e: e.lower()
            ),
            AbstractStringFunction.of_entry_string_and_string_output(
                'UpperCase', lambda e: e.upper()
            ),

            # Single string -> boolean
            AbstractStringFunction.of_entry_string_and_boolean_output(
                'IsBlank', lambda e: e.strip() == ''
            ),
            AbstractStringFunction.of_entry_string_and_boolean_output(
                'IsEmpty', lambda e: e == ''
            ),

            # String, string -> boolean
            AbstractStringFunction.of_entry_string_string_and_boolean_output(
                'Contains', lambda a, b: b in a
            ),
            AbstractStringFunction.of_entry_string_string_and_boolean_output(
                'EndsWith', lambda a, b: a.endswith(b)
            ),
            AbstractStringFunction.of_entry_string_string_and_boolean_output(
                'StartsWith', lambda a, b: a.startswith(b)
            ),
            AbstractStringFunction.of_entry_string_string_and_boolean_output(
                'EqualsIgnoreCase', lambda a, b: a.upper() == b.upper()
            ),
            AbstractStringFunction.of_entry_string_string_and_boolean_output(
                'Matches', lambda a, b: bool(re.search(b, a))
            ),

            # String, string -> integer
            AbstractStringFunction.of_entry_string_string_and_integer_output(
                'IndexOf', lambda a, b: a.find(b)
            ),
            AbstractStringFunction.of_entry_string_string_and_integer_output(
                'LastIndexOf', lambda a, b: a.rfind(b)
            ),

            # String, integer -> string
            AbstractStringFunction.of_entry_string_integer_and_string_output(
                'Repeat', lambda a, b: a * b
            ),

            # String, string, integer -> integer
            AbstractStringFunction.of_entry_string_string_integer_and_integer_output(
                'IndexOfWithStartPoint', lambda a, b, c: a.find(b, c)
            ),
            AbstractStringFunction.of_entry_string_string_integer_and_integer_output(
                'LastIndexOfWithStartPoint', lambda a, b, c: a.rfind(b, 0, c + 1)
            ),

            # String, string, string -> string
            AbstractStringFunction.of_entry_string_string_string_and_string_output(
                'Replace', lambda a, b, c: a.replace(b, c)
            ),
            AbstractStringFunction.of_entry_string_string_string_and_string_output(
                'ReplaceFirst', lambda a, b, c: a.replace(b, c, 1)
            ),

            # String, integer, integer -> string
            AbstractStringFunction.of_entry_string_integer_integer_and_string_output(
                'SubString', lambda a, b, c: a[b:c]
            ),

            # Class-based functions
            _map_entry(Concatenate()),
            _map_entry(DeleteForGivenLength()),
            _map_entry(InsertAtGivenPosition()),
            _map_entry(PostPad()),
            _map_entry(PrePad()),
            _map_entry(RegionMatches()),
            _map_entry(ReplaceAtGivenPosition()),
            _map_entry(Reverse()),
            _map_entry(Split()),
            _map_entry(ToString()),
            _map_entry(TrimTo()),
            _map_entry(Matches()),
        ])

        self._filterable_names: List[str] = [
            func.get_signature().get_full_name()
            for func in self._repo_map.values()
        ]

    async def find(
        self, namespace: str, name: str
    ) -> Optional[AbstractFunction]:
        if namespace != Namespaces.STRING:
            return None
        return self._repo_map.get(name)

    async def filter(self, name: str) -> List[str]:
        lower_name = name.lower()
        return [
            n for n in self._filterable_names
            if lower_name in n.lower()
        ]
