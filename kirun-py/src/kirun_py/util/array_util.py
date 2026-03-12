from typing import Any


class ArrayUtil:

    @staticmethod
    def remove_a_list_from(source: list, remove_list: list) -> None:
        if not remove_list or not source:
            return
        to_remove = set()
        for item in remove_list:
            try:
                to_remove.add(item)
            except TypeError:
                pass

        i = 0
        while i < len(source):
            try:
                if source[i] in to_remove:
                    source.pop(i)
                    continue
            except TypeError:
                pass
            i += 1

    @staticmethod
    def of(*items: Any) -> list:
        return list(items)
