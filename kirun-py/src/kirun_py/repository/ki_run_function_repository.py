from __future__ import annotations

from typing import List, Optional

from kirun_py.hybrid_repository import HybridRepository
from kirun_py.namespaces.namespaces import Namespaces


def _map_entry(func):
    sig = func.get_signature()
    return (sig.get_name(), func)


class SystemFunctionRepository:

    def __init__(self) -> None:
        from kirun_py.function.system.if_ import If
        from kirun_py.function.system.generate_event import GenerateEvent
        from kirun_py.function.system.print_ import Print
        from kirun_py.function.system.wait import Wait
        from kirun_py.function.system.make import Make
        from kirun_py.function.system.validate_schema import ValidateSchema
        from kirun_py.function.system.context.create import Create
        from kirun_py.function.system.context.get import Get
        from kirun_py.function.system.context.set_function import SetFunction
        from kirun_py.function.system.loop.range_loop import RangeLoop
        from kirun_py.function.system.loop.count_loop import CountLoop
        from kirun_py.function.system.loop.break_ import Break
        from kirun_py.function.system.loop.for_each_loop import ForEachLoop
        from kirun_py.function.system.json.json_parse import JSONParse
        from kirun_py.function.system.json.json_stringify import JSONStringify
        from kirun_py.function.system.array.join import Join

        self._map = {
            Namespaces.SYSTEM_JSON: dict([
                _map_entry(JSONParse()),
                _map_entry(JSONStringify()),
            ]),
            Namespaces.SYSTEM_CTX: dict([
                _map_entry(Create()),
                _map_entry(Get()),
                _map_entry(SetFunction()),
            ]),
            Namespaces.SYSTEM_LOOP: dict([
                _map_entry(RangeLoop()),
                _map_entry(CountLoop()),
                _map_entry(Break()),
                _map_entry(ForEachLoop()),
            ]),
            Namespaces.SYSTEM: dict([
                _map_entry(If()),
                _map_entry(GenerateEvent()),
                _map_entry(Print()),
                _map_entry(Wait()),
                _map_entry(Join()),
                _map_entry(ValidateSchema()),
                _map_entry(Make()),
            ]),
        }

        self._filterable_names: List[str] = []
        for ns_map in self._map.values():
            for func in ns_map.values():
                self._filterable_names.append(func.get_signature().get_full_name())

    async def find(self, namespace: str, name: str) -> Optional[object]:
        ns_map = self._map.get(namespace)
        if ns_map is None:
            return None
        return ns_map.get(name)

    async def filter(self, name: str) -> List[str]:
        lower = name.lower()
        return [n for n in self._filterable_names if lower in n.lower()]


class KIRunFunctionRepository(HybridRepository):

    def __init__(self) -> None:
        from kirun_py.function.system.math.math_function_repository import MathFunctionRepository
        from kirun_py.function.system.string.string_function_repository import StringFunctionRepository
        from kirun_py.function.system.array.array_function_repository import ArrayFunctionRepository
        from kirun_py.function.system.object.object_function_repository import ObjectFunctionRepository
        from kirun_py.function.system.date.date_function_repository import DateFunctionRepository

        super().__init__(
            SystemFunctionRepository(),
            MathFunctionRepository(),
            StringFunctionRepository(),
            ArrayFunctionRepository(),
            ObjectFunctionRepository(),
            DateFunctionRepository(),
        )
