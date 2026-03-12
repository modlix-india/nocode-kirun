from __future__ import annotations

from typing import Dict, List, Optional

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.function.system.object.object_convert import ObjectConvert
from kirun_py.function.system.object.object_delete_key import ObjectDeleteKey
from kirun_py.function.system.object.object_entries import ObjectEntries
from kirun_py.function.system.object.object_keys import ObjectKeys
from kirun_py.function.system.object.object_put_value import ObjectPutValue
from kirun_py.function.system.object.object_values import ObjectValues
from kirun_py.namespaces.namespaces import Namespaces


class ObjectFunctionRepository:

    def __init__(self) -> None:
        self._function_objects_index: Dict[str, AbstractFunction] = {
            'ObjectValues': ObjectValues(),
            'ObjectKeys': ObjectKeys(),
            'ObjectEntries': ObjectEntries(),
            'ObjectDeleteKey': ObjectDeleteKey(),
            'ObjectPutValue': ObjectPutValue(),
            'ObjectConvert': ObjectConvert(),
        }
        self._filterable_names: List[str] = [
            f.get_signature().get_full_name()
            for f in self._function_objects_index.values()
        ]

    async def find(self, namespace: str, name: str) -> Optional[AbstractFunction]:
        if namespace != Namespaces.SYSTEM_OBJECT:
            return None
        return self._function_objects_index.get(name)

    async def filter(self, name: str) -> List[str]:
        lower_name = name.lower()
        return [e for e in self._filterable_names if lower_name in e.lower()]
