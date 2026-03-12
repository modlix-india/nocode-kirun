from typing import Any


class StringFormatter:

    @staticmethod
    def format(format_string: str, *params: Any) -> str:
        if not params:
            return format_string

        sb = ''
        ind = 0
        prev_char = ''
        length = len(format_string)

        for i in range(length):
            ch = format_string[i]
            if ch == '$' and prev_char == '\\':
                sb = sb[:len(sb) - 1] + ch
            elif ch == '$' and ind < len(params):
                sb += str(params[ind])
                ind += 1
            else:
                sb += ch
            prev_char = ch

        return sb
