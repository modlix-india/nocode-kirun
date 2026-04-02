from __future__ import annotations

from typing import Any, Dict, List, Protocol

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature


class Function(Protocol):

    def get_signature(self) -> FunctionSignature:
        ...

    def get_probable_event_signature(
        self, probable_parameters: Dict[str, List[Schema]]
    ) -> Dict[str, Event]:
        ...

    async def execute(self, context: Any) -> FunctionOutput:
        ...
