from __future__ import annotations
from typing import List, Optional, Union

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output_generator import FunctionOutputGenerator


class FunctionOutput:
    def __init__(self, arg: Union[List[EventResult], FunctionOutputGenerator]):
        if arg is None:
            raise KIRuntimeException('Function output is generating null')

        self._index: int = 0
        self._generator: Optional[FunctionOutputGenerator] = None

        if isinstance(arg, list):
            self._fo: List[EventResult] = arg
        else:
            self._fo = []
            self._generator = arg

    def next(self) -> Optional[EventResult]:
        if self._generator is None:
            if self._index < len(self._fo):
                result = self._fo[self._index]
                self._index += 1
                return result
            return None

        er = self._generator.next()
        if er is not None:
            self._fo.append(er)
        return er

    def all_results(self) -> List[EventResult]:
        return self._fo
