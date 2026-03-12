from __future__ import annotations

from typing import Any, List, Optional

from kirun_py.dsl.lexer.dsl_lexer import DSLLexer
from kirun_py.dsl.parser.dsl_parser import DSLParser
from kirun_py.dsl.transformer.ast_to_json import ASTToJSONTransformer
from kirun_py.dsl.transformer.json_to_text import JSONToTextTransformer


class FormattedError:
    """Formatted error for user-friendly display."""

    def __init__(
        self,
        message: str,
        line: Optional[int] = None,
        column: Optional[int] = None,
        snippet: Optional[str] = None,
    ) -> None:
        self.message = message
        self.line = line
        self.column = column
        self.snippet = snippet


class ValidationResult:
    """Validation result with errors if any."""

    def __init__(self, valid: bool, errors: Optional[List[FormattedError]] = None) -> None:
        self.valid = valid
        self.errors: List[FormattedError] = errors if errors is not None else []


class DSLCompiler:
    """
    DSL Compiler - High-level API.
    Main entry point for DSL compilation and decompilation.
    """

    @staticmethod
    def compile(text: str) -> Any:
        """
        Compile DSL text to FunctionDefinition JSON.

        Args:
            text: DSL source text.

        Returns:
            FunctionDefinition JSON (dict).
        """
        # 1. Lex
        lexer = DSLLexer(text)
        tokens = lexer.tokenize()

        # 2. Parse (pass original input for exact expression extraction)
        parser = DSLParser(tokens, text)
        ast = parser.parse()

        # 3. Transform
        transformer = ASTToJSONTransformer()

        # Flatten nested blocks before transformation
        transformer.flatten_nested_blocks(ast)

        json_obj = transformer.transform(ast)

        return json_obj

    @staticmethod
    async def decompile(json_obj: Any) -> str:
        """
        Decompile FunctionDefinition JSON to DSL text.

        Args:
            json_obj: FunctionDefinition JSON (dict).

        Returns:
            DSL source text.
        """
        transformer = JSONToTextTransformer()
        return await transformer.transform(json_obj)

    @staticmethod
    def validate(text: str) -> ValidationResult:
        """
        Validate DSL syntax without full compilation.

        Args:
            text: DSL source text.

        Returns:
            ValidationResult with errors if any.
        """
        try:
            DSLCompiler.compile(text)
            return ValidationResult(valid=True)
        except Exception as error:
            return ValidationResult(
                valid=False,
                errors=[DSLCompiler._format_error(error)],
            )

    @staticmethod
    async def format(text: str) -> str:
        """
        Format DSL text (parse and regenerate with consistent formatting).

        Args:
            text: DSL source text.

        Returns:
            Formatted DSL text.
        """
        json_obj = DSLCompiler.compile(text)
        return await DSLCompiler.decompile(json_obj)

    @staticmethod
    def _format_error(error: Exception) -> FormattedError:
        """Format error for user-friendly display."""
        formatted = FormattedError(
            message=str(error) or 'Unknown error',
        )

        location = getattr(error, 'location', None)
        if location is not None:
            formatted.line = getattr(location, 'line', None)
            formatted.column = getattr(location, 'column', None)

        return formatted
