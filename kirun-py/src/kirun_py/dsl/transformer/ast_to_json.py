from __future__ import annotations

import uuid
from typing import Any, Dict, List, Optional

from kirun_py.dsl.parser.ast.argument_node import ArgumentNode, ArgumentValue
from kirun_py.dsl.parser.ast.complex_value_node import ComplexValueNode
from kirun_py.dsl.parser.ast.event_decl_node import EventDeclNode
from kirun_py.dsl.parser.ast.expression_node import ExpressionNode
from kirun_py.dsl.parser.ast.function_def_node import FunctionDefNode
from kirun_py.dsl.parser.ast.parameter_decl_node import ParameterDeclNode
from kirun_py.dsl.parser.ast.schema_literal_node import SchemaLiteralNode
from kirun_py.dsl.parser.ast.statement_node import StatementNode
from kirun_py.dsl.transformer.schema_transformer import SchemaTransformer


class ASTToJSONTransformer:
    """
    AST to JSON Transformer.
    Converts parsed AST to FunctionDefinition JSON.
    """

    def transform(self, ast: FunctionDefNode) -> Any:
        """
        Transform AST to FunctionDefinition JSON.
        Only includes non-empty/non-default fields.
        """
        json_obj: dict = {
            'name': ast.name,
        }

        if ast.namespace:
            json_obj['namespace'] = ast.namespace

        parameters = self._transform_parameters(ast.parameters)
        if parameters:
            json_obj['parameters'] = parameters

        events = self._transform_events(ast.events)
        if events:
            json_obj['events'] = events

        json_obj['steps'] = self._transform_steps(ast.logic)

        return json_obj

    def _transform_parameters(self, params: List[ParameterDeclNode]) -> dict:
        """Transform parameters."""
        result: dict = {}
        for param in params:
            result[param.name] = {
                'parameterName': param.name,
                'schema': SchemaTransformer.transform(param.schema.schema_spec),
                'variableArgument': False,
                'type': 'EXPRESSION',
            }
        return result

    def _transform_events(self, events: List[EventDeclNode]) -> dict:
        """Transform events."""
        result: dict = {}
        for event in events:
            result[event.name] = {
                'name': event.name,
                'parameters': self._transform_event_parameters(event.parameters),
            }
        return result

    def _transform_event_parameters(self, params: List[ParameterDeclNode]) -> dict:
        """Transform event parameters (these are schemas, not Parameter objects)."""
        result: dict = {}
        for param in params:
            result[param.name] = SchemaTransformer.transform(param.schema.schema_spec)
        return result

    def _transform_steps(self, statements: List[StatementNode]) -> dict:
        """Transform statements (top-level and nested)."""
        result: dict = {}
        for stmt in statements:
            result[stmt.statement_name] = self._transform_statement(stmt)
        return result

    def _transform_statement(self, stmt: StatementNode) -> dict:
        """
        Transform single statement.
        Only includes non-empty/non-default fields.
        """
        json_obj: dict = {
            'statementName': stmt.statement_name,
            'namespace': stmt.function_call.namespace,
            'name': stmt.function_call.name,
        }

        parameter_map = self._transform_parameter_map(stmt.function_call.arguments_map)
        if parameter_map:
            json_obj['parameterMap'] = parameter_map

        dependent_statements = self._create_dependent_statements_map(stmt)
        if dependent_statements:
            json_obj['dependentStatements'] = dependent_statements

        execute_if_true = self._create_execute_if_map(stmt.execute_if_steps)
        if execute_if_true:
            json_obj['executeIftrue'] = execute_if_true

        if stmt.comment and stmt.comment.strip():
            json_obj['comment'] = stmt.comment

        return json_obj

    def _create_dependent_statements_map(self, stmt: StatementNode) -> dict:
        """Create dependentStatements map from AFTER clause and nested blocks."""
        result: dict = {}

        for step_ref in stmt.after_steps:
            result[step_ref] = True

        return result

    def _create_execute_if_map(self, execute_if_steps: List[str]) -> dict:
        """Create executeIftrue map from IF clause."""
        result: dict = {}
        for step_ref in execute_if_steps:
            result[step_ref] = True
        return result

    def _transform_parameter_map(self, args_map: Dict[str, ArgumentNode]) -> dict:
        """
        Transform parameter map (function arguments).
        Skips parameters with empty values.
        Supports multi-value parameters.
        """
        result: dict = {}

        for param_name, arg in args_map.items():
            if self._is_empty_argument(arg):
                continue

            result[param_name] = {}

            if arg.is_multi_value():
                for i, val in enumerate(arg.values):
                    param_ref = self._transform_argument_value(val, i + 1)
                    result[param_name][param_ref['key']] = param_ref
            else:
                param_ref = self._transform_argument_value(arg.value, 1)
                result[param_name][param_ref['key']] = param_ref

        return result

    def _is_empty_argument(self, arg: ArgumentNode) -> bool:
        """
        Check if an argument is empty (no value provided).
        Only filter out parser artifacts like stray delimiters.
        """
        if isinstance(arg.value, ExpressionNode):
            expr = arg.value.expression_text.strip()
            return expr in (',', ')')
        return False

    def _transform_argument_value(self, value: ArgumentValue, order: int) -> dict:
        """
        Transform single argument value to ParameterReference.

        Args:
            value: The argument value (expression, complex value, or schema literal).
            order: The order index for multi-value parameters (1-based).
        """
        key = self._generate_uuid()

        if isinstance(value, ExpressionNode):
            return {
                'key': key,
                'type': 'EXPRESSION',
                'expression': value.expression_text,
                'value': None,
                'order': order,
            }
        elif isinstance(value, ComplexValueNode):
            return {
                'key': key,
                'type': 'VALUE',
                'value': value.value,
                'expression': None,
                'order': order,
            }
        elif isinstance(value, SchemaLiteralNode):
            schema = SchemaTransformer.transform(value.schema.schema_spec)

            if value.default_value is not None:
                return {
                    'key': key,
                    'type': 'VALUE',
                    'value': self._evaluate_default_value(value.default_value, schema),
                    'expression': None,
                    'order': order,
                }
            else:
                return {
                    'key': key,
                    'type': 'VALUE',
                    'value': schema,
                    'expression': None,
                    'order': order,
                }

        return {
            'key': key,
            'type': 'VALUE',
            'value': None,
            'expression': None,
            'order': order,
        }

    def _evaluate_default_value(self, default_value_expr: ExpressionNode, schema: Any) -> Any:
        """Evaluate default value for schema literal."""
        expr_text = default_value_expr.expression_text.strip()

        if expr_text == '[]':
            return []
        if expr_text == '{}':
            return {}
        if expr_text == 'null':
            return None
        if expr_text == 'true':
            return True
        if expr_text == 'false':
            return False

        try:
            num = float(expr_text)
            return num
        except (ValueError, TypeError):
            pass

        if (expr_text.startswith('"') and expr_text.endswith('"')) or \
           (expr_text.startswith("'") and expr_text.endswith("'")):
            return expr_text[1:-1]

        return expr_text

    @staticmethod
    def _generate_uuid() -> str:
        """Generate UUID for parameter reference keys."""
        return str(uuid.uuid4())

    def flatten_nested_blocks(self, ast: FunctionDefNode) -> None:
        """
        Flatten nested blocks into steps.
        Adds all nested statements to the top-level steps object
        and sets their dependentStatements appropriately.
        """
        all_statements: List[StatementNode] = list(ast.logic)

        for stmt in list(ast.logic):
            self._collect_nested_statements(stmt, all_statements)

        ast.logic = all_statements

    def _collect_nested_statements(
        self, stmt: StatementNode, all_statements: List[StatementNode]
    ) -> None:
        """
        Recursively collect nested statements.
        Nesting is implicit (derived from expression analysis).
        Only explicit AFTER clauses become dependentStatements.
        """
        for _block_name, nested_stmts in stmt.nested_blocks.items():
            for nested_stmt in nested_stmts:
                all_statements.append(nested_stmt)
                self._collect_nested_statements(nested_stmt, all_statements)

        stmt.nested_blocks.clear()
