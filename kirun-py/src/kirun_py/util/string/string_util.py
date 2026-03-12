from __future__ import annotations

from kirun_py.exception import KIRuntimeException
from kirun_py.util.string.string_formatter import StringFormatter


class StringUtil:

    @staticmethod
    def nth_index(s: str, c: str, from_: int = 0, occurrence: int = 1) -> int:
        if not s:
            raise KIRuntimeException('String cannot be null')

        if from_ < 0 or from_ >= len(s):
            raise KIRuntimeException(
                StringFormatter.format('Cannot search from index : $', from_)
            )

        if occurrence <= 0 or occurrence > len(s):
            raise KIRuntimeException(
                StringFormatter.format('Cannot search for occurance : $', occurrence)
            )

        while from_ < len(s):
            if s[from_] == c:
                occurrence -= 1
                if occurrence == 0:
                    return from_
            from_ += 1

        return -1

    @staticmethod
    def split_at_first_occurrence(s: str, c: str) -> tuple[str | None, str | None]:
        if not s:
            return (None, None)

        index = s.find(c)
        if index == -1:
            return (s, None)

        return (s[:index], s[index + 1:])

    @staticmethod
    def split_at_last_occurrence(s: str, c: str) -> tuple[str | None, str | None]:
        if not s:
            return (None, None)

        index = s.rfind(c)
        if index == -1:
            return (s, None)

        return (s[:index], s[index + 1:])

    @staticmethod
    def is_null_or_blank(s: str | None) -> bool:
        return s is None or s.strip() == ''
