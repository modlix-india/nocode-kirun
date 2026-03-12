from __future__ import annotations

from typing import Any, Dict, List

from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor


class OutputMapTokenValueExtractor(TokenValueExtractor):

    PREFIX: str = 'Steps.'

    def __init__(self, output: Dict[str, Dict[str, Dict[str, Any]]]) -> None:
        super().__init__()
        self._output: Dict[str, Dict[str, Dict[str, Any]]] = output

    def get_value_internal(self, token: str) -> Any:
        parts: List[str] = TokenValueExtractor.split_path(token)

        ind: int = 1

        # Get the step name
        if ind >= len(parts):
            return None
        events = self._output.get(parts[ind])
        ind += 1

        if events is None or ind >= len(parts):
            return None

        # Get the event name
        each_event = events.get(parts[ind])
        ind += 1

        if each_event is None or ind > len(parts):
            return None

        if ind == len(parts):
            return each_event

        bracket = parts[ind].find('[')

        if bracket == -1:
            element = each_event.get(parts[ind])
            ind += 1
            return self.retrieve_element_from(token, parts, ind, element)

        ev_param_name = parts[ind][:bracket]
        element = each_event.get(ev_param_name)
        return self.retrieve_element_from(token, parts, ind, {ev_param_name: element})

    def get_prefix(self) -> str:
        return OutputMapTokenValueExtractor.PREFIX

    def get_store(self) -> Any:
        return self._convert_map_to_obj(self._output)

    def _convert_map_to_obj(self, m: Any) -> Any:
        if not isinstance(m, dict):
            return m

        if len(m) == 0:
            return {}

        result: Dict[str, Any] = {}
        for key, value in m.items():
            if isinstance(value, dict):
                result[key] = self._convert_map_to_obj(value)
            else:
                result[key] = value
        return result
