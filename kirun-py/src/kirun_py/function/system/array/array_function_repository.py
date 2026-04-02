from __future__ import annotations

from typing import Dict, List, Optional

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.namespaces.namespaces import Namespaces


def _map_entry(func: AbstractFunction):
    sig = func.get_signature()
    return (sig.get_name(), func)


class ArrayFunctionRepository:

    def __init__(self) -> None:
        from kirun_py.function.system.array.add_first import AddFirst
        from kirun_py.function.system.array.array_to_array_of_objects import ArrayToArrayOfObjects
        from kirun_py.function.system.array.array_to_object import ArrayToObject
        from kirun_py.function.system.array.binary_search import BinarySearch
        from kirun_py.function.system.array.compare import Compare
        from kirun_py.function.system.array.concatenate import Concatenate
        from kirun_py.function.system.array.copy import Copy
        from kirun_py.function.system.array.delete import Delete
        from kirun_py.function.system.array.delete_first import DeleteFirst
        from kirun_py.function.system.array.delete_from import DeleteFrom
        from kirun_py.function.system.array.delete_last import DeleteLast
        from kirun_py.function.system.array.disjoint import Disjoint
        from kirun_py.function.system.array.equals import Equals
        from kirun_py.function.system.array.fill import Fill
        from kirun_py.function.system.array.frequency import Frequency
        from kirun_py.function.system.array.index_of import IndexOf
        from kirun_py.function.system.array.index_of_array import IndexOfArray
        from kirun_py.function.system.array.insert import Insert
        from kirun_py.function.system.array.insert_last import InsertLast
        from kirun_py.function.system.array.join import Join
        from kirun_py.function.system.array.last_index_of import LastIndexOf
        from kirun_py.function.system.array.last_index_of_array import LastIndexOfArray
        from kirun_py.function.system.array.max_ import Max
        from kirun_py.function.system.array.min_ import Min
        from kirun_py.function.system.array.mis_match import MisMatch
        from kirun_py.function.system.array.remove_duplicates import RemoveDuplicates
        from kirun_py.function.system.array.reverse import Reverse
        from kirun_py.function.system.array.rotate import Rotate
        from kirun_py.function.system.array.shuffle import Shuffle
        from kirun_py.function.system.array.sort import Sort
        from kirun_py.function.system.array.sub_array import SubArray

        self._repo_map: Dict[str, AbstractFunction] = dict([
            _map_entry(AddFirst()),
            _map_entry(ArrayToArrayOfObjects()),
            _map_entry(ArrayToObject()),
            _map_entry(BinarySearch()),
            _map_entry(Compare()),
            _map_entry(Concatenate()),
            _map_entry(Copy()),
            _map_entry(Delete()),
            _map_entry(DeleteFirst()),
            _map_entry(DeleteFrom()),
            _map_entry(DeleteLast()),
            _map_entry(Disjoint()),
            _map_entry(Equals()),
            _map_entry(Fill()),
            _map_entry(Frequency()),
            _map_entry(IndexOf()),
            _map_entry(IndexOfArray()),
            _map_entry(Insert()),
            _map_entry(InsertLast()),
            _map_entry(Join()),
            _map_entry(LastIndexOf()),
            _map_entry(LastIndexOfArray()),
            _map_entry(Max()),
            _map_entry(Min()),
            _map_entry(MisMatch()),
            _map_entry(RemoveDuplicates()),
            _map_entry(Reverse()),
            _map_entry(Rotate()),
            _map_entry(Shuffle()),
            _map_entry(Sort()),
            _map_entry(SubArray()),
        ])

        self._filterable_names: List[str] = [
            func.get_signature().get_full_name()
            for func in self._repo_map.values()
        ]

    async def find(self, namespace: str, name: str) -> Optional[AbstractFunction]:
        if namespace != Namespaces.SYSTEM_ARRAY:
            return None
        return self._repo_map.get(name)

    async def filter(self, name: str) -> List[str]:
        lower = name.lower()
        return [n for n in self._filterable_names if lower in n.lower()]
