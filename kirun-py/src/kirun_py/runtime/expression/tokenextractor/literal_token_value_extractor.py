from __future__ import annotations

from typing import Any, Dict, Optional

from kirun_py.runtime.expression.exception.expression_evaluation_exception import (
    ExpressionEvaluationException,
)
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.util.string.string_util import StringUtil


KEYWORDS: Dict[str, Any] = {
    'true': True,
    'false': False,
    'null': None,
    'undefined': None,
}


class LiteralTokenValueExtractor(TokenValueExtractor):

    INSTANCE: LiteralTokenValueExtractor

    def get_value_internal(self, token: str) -> Any:
        if StringUtil.is_null_or_blank(token):
            return None

        token = token.strip()

        if token in KEYWORDS:
            return KEYWORDS[token]

        if token.startswith('"'):
            return self._process_string(token)

        return self._process_numbers(token)

    def _process_numbers(self, token: str) -> Any:
        try:
            v = float(token)
            # Return int if the value is a whole number
            if v == int(v) and '.' not in token and 'e' not in token.lower():
                return int(v)
            return v
        except (ValueError, TypeError) as err:
            raise ExpressionEvaluationException(
                token,
                StringFormatter.format('Unable to parse the literal or expression $', token),
                err,
            )

    def _process_string(self, token: str) -> Any:
        if not token.endswith('"'):
            raise ExpressionEvaluationException(
                token,
                StringFormatter.format('String literal $ is not closed properly', token),
            )

        return token[1:-1]

    def get_prefix(self) -> str:
        return ''

    def get_store(self) -> Any:
        return None

    def get_value_from_extractors(
        self, token: str, maps: Dict[str, TokenValueExtractor]
    ) -> Any:
        if (token + '.') in maps:
            extractor = maps.get(token + '.')
            if extractor is not None:
                return extractor.get_store()
        return self.get_value(token)


# Singleton instance
LiteralTokenValueExtractor.INSTANCE = LiteralTokenValueExtractor()
