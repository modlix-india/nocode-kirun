from __future__ import annotations

import json
import re
from typing import Any, Callable, Dict, List, Optional, Set, Tuple

from kirun_py.dsl.transformer.expression_handler import ExpressionHandler
from kirun_py.dsl.transformer.schema_transformer import SchemaTransformer


class _NestedStructure:
    __slots__ = ('block_name', 'parent')

    def __init__(self, block_name: str, parent: str) -> None:
        self.block_name = block_name
        self.parent = parent


class JSONToTextTransformer:
    """
    JSON to Text Transformer.
    Converts FunctionDefinition JSON back to DSL text.
    Extracts implicit dependencies from expressions using same logic as KIRuntime.
    """

    def __init__(self) -> None:
        self._indent_char = '    '  # 4 spaces

    # Same regex pattern as KIRuntime uses for extracting step dependencies
    _STEP_REGEX_PATTERN = re.compile(r'Steps\.([a-zA-Z0-9_-]+)\.([a-zA-Z0-9_-]+)')

    async def transform(self, json_obj: Any) -> str:
        """Transform JSON to DSL text."""
        lines: List[str] = []

        # Function header
        lines.append(f"FUNCTION {json_obj['name']}")

        # Namespace
        namespace = json_obj.get('namespace')
        if namespace and namespace != '_':
            lines.append(f"{self._indent(1)}NAMESPACE {namespace}")

        # Parameters
        parameters = json_obj.get('parameters')
        if parameters and len(parameters) > 0:
            lines.append(f"{self._indent(1)}PARAMETERS")
            for name, param in parameters.items():
                schema_text = self._schema_to_text(param.get('schema'))
                lines.append(f"{self._indent(2)}{name} AS {schema_text}")

        # Events
        events = json_obj.get('events')
        if events and len(events) > 0:
            lines.append(f"{self._indent(1)}EVENTS")
            for name, event in events.items():
                lines.append(f"{self._indent(2)}{name}")
                event_params = event.get('parameters')
                if event_params and len(event_params) > 0:
                    for param_name, schema in event_params.items():
                        schema_text = self._schema_to_text(schema)
                        lines.append(f"{self._indent(3)}{param_name} AS {schema_text}")

        # Logic
        lines.append(f"{self._indent(1)}LOGIC")

        nested_structure = await self._build_nested_structure_from_runtime(json_obj)
        stmt_lines = await self._steps_to_text(json_obj.get('steps', {}), nested_structure, 2)
        lines.extend(stmt_lines)

        return '\n'.join(lines)

    async def _build_nested_structure_from_runtime(
        self, json_obj: Any
    ) -> Dict[str, _NestedStructure]:
        """Build nested structure by extracting dependencies from expressions."""
        structure: Dict[str, _NestedStructure] = {}
        steps = json_obj.get('steps', {})
        step_names: Set[str] = set(steps.keys())

        all_edges: Dict[str, List[Dict[str, str]]] = {}

        for step_name, step in steps.items():
            dependencies = self._extract_all_dependencies(step, step_names)
            all_edges[step_name] = dependencies

        def get_nesting_depth(sn: str, visited: Optional[Set[str]] = None) -> int:
            if visited is None:
                visited = set()
            if sn in visited:
                return 0
            visited.add(sn)
            nest_info = structure.get(sn)
            if not nest_info:
                return 0
            return 1 + get_nesting_depth(nest_info.parent, visited)

        sorted_steps = self._topological_sort_for_nesting(list(all_edges.keys()), all_edges)

        changed = True
        while changed:
            changed = False

            for step_name in sorted_steps:
                if step_name in structure:
                    continue

                incoming_edges = all_edges.get(step_name)
                if not incoming_edges or len(incoming_edges) == 0:
                    continue

                if len(incoming_edges) == 1:
                    edge = incoming_edges[0]
                    structure[step_name] = _NestedStructure(
                        block_name=edge['blockName'],
                        parent=edge['parent'],
                    )
                    changed = True
                else:
                    deepest_edge = incoming_edges[0]
                    max_depth = get_nesting_depth(deepest_edge['parent'])

                    for i in range(1, len(incoming_edges)):
                        edge = incoming_edges[i]
                        depth = get_nesting_depth(edge['parent'])
                        if depth > max_depth:
                            max_depth = depth
                            deepest_edge = edge

                    structure[step_name] = _NestedStructure(
                        block_name=deepest_edge['blockName'],
                        parent=deepest_edge['parent'],
                    )
                    changed = True

        # Detect and break circular dependencies
        steps_in_cycles = self._detect_circular_dependencies(structure)
        for step_name in steps_in_cycles:
            del structure[step_name]

        return structure

    def _extract_all_dependencies(
        self, step: Any, valid_step_names: Set[str]
    ) -> List[Dict[str, str]]:
        """Extract all step dependencies from a step (same logic as KIRuntime)."""
        deps: List[Dict[str, str]] = []
        seen_deps: Set[str] = set()

        def add_dep(text: str) -> None:
            for match in self._STEP_REGEX_PATTERN.finditer(text):
                parent_step = match.group(1)
                block_name = match.group(2)
                key = f"{parent_step}.{block_name}"

                if parent_step in valid_step_names and key not in seen_deps:
                    seen_deps.add(key)
                    deps.append({'parent': parent_step, 'blockName': block_name})

        # Extract from parameterMap
        parameter_map = step.get('parameterMap')
        if parameter_map:
            for param_refs in parameter_map.values():
                if not isinstance(param_refs, dict):
                    continue
                for ref in param_refs.values():
                    if not isinstance(ref, dict):
                        continue
                    if ref.get('type') == 'EXPRESSION' and ref.get('expression'):
                        add_dep(ref['expression'])
                    if ref.get('type') == 'VALUE' and ref.get('value') is not None:
                        self._extract_expressions_from_value(ref['value'], add_dep)

        # Extract from dependentStatements
        dependent_stmts = step.get('dependentStatements')
        if dependent_stmts:
            for dep_key, dep_value in dependent_stmts.items():
                if dep_value is not True:
                    continue
                decoded_key = self._decode_dots(dep_key)
                add_dep(decoded_key)

        # Extract from executeIftrue
        execute_if_true = step.get('executeIftrue')
        if execute_if_true:
            for condition in execute_if_true.keys():
                add_dep(condition)

        return deps

    def _extract_expressions_from_value(
        self, value: Any, add_dep: Callable[[str], None]
    ) -> None:
        """Recursively extract expressions from a VALUE."""
        if value is None:
            return

        if isinstance(value, str):
            add_dep(value)
        elif isinstance(value, list):
            for item in value:
                self._extract_expressions_from_value(item, add_dep)
        elif isinstance(value, dict):
            if value.get('isExpression') is True and isinstance(value.get('value'), str):
                add_dep(value['value'])
            elif value.get('type') == 'EXPRESSION' and isinstance(value.get('expression'), str):
                add_dep(value['expression'])
            else:
                for prop_value in value.values():
                    self._extract_expressions_from_value(prop_value, add_dep)

    def _topological_sort_for_nesting(
        self,
        step_names: List[str],
        all_edges: Dict[str, List[Dict[str, str]]],
    ) -> List[str]:
        """Topological sort for nesting."""
        step_set = set(step_names)
        dependencies: Dict[str, Set[str]] = {}

        for step_name in step_names:
            dependencies[step_name] = set()
            edges = all_edges.get(step_name, [])
            for edge in edges:
                if edge['parent'] in step_set:
                    dependencies[step_name].add(edge['parent'])

        # Kahn's algorithm
        sorted_list: List[str] = []
        in_degree: Dict[str, int] = {}

        for step_name in step_names:
            in_degree[step_name] = len(dependencies[step_name])

        queue: List[str] = [sn for sn in step_names if in_degree[sn] == 0]

        while queue:
            current = queue.pop(0)
            sorted_list.append(current)

            for step_name, deps in dependencies.items():
                if current in deps:
                    in_degree[step_name] -= 1
                    if in_degree[step_name] == 0:
                        queue.append(step_name)

        for step_name in step_names:
            if step_name not in sorted_list:
                sorted_list.append(step_name)

        return sorted_list

    def _detect_circular_dependencies(
        self, structure: Dict[str, _NestedStructure]
    ) -> Set[str]:
        """Detect circular dependencies in the nested structure."""
        in_cycle: Set[str] = set()

        for step_name in structure:
            visited: Set[str] = set()
            current: Optional[str] = step_name

            while current and current in structure:
                if current in visited:
                    in_cycle.update(visited)
                    break
                visited.add(current)
                current = structure[current].parent

        return in_cycle

    def _extract_step_references(self, expr: str) -> List[str]:
        """Extract step references from an expression string."""
        refs: List[str] = []
        for match in re.finditer(r'Steps\.([a-zA-Z_][a-zA-Z0-9_]*)', expr):
            refs.append(match.group(1))
        return refs

    def _extract_dependencies_from_params(self, parameter_map: Any) -> List[str]:
        """Extract all step dependencies from a step's parameterMap."""
        deps: List[str] = []

        if not parameter_map:
            return deps

        for param_refs in parameter_map.values():
            if not isinstance(param_refs, dict):
                continue
            for ref in param_refs.values():
                if not isinstance(ref, dict):
                    continue
                if ref.get('type') == 'EXPRESSION' and ref.get('expression'):
                    deps.extend(self._extract_step_references(ref['expression']))
                if ref.get('type') == 'VALUE' and ref.get('value'):
                    value_str = json.dumps(ref['value'])
                    deps.extend(self._extract_step_references(value_str))

        return deps

    def _topological_sort(self, step_names: List[str], steps: Any) -> List[str]:
        """Topologically sort steps based on their dependencies."""
        dependencies: Dict[str, Set[str]] = {}
        step_set = set(step_names)

        for step_name in step_names:
            dependencies[step_name] = set()
            step = steps[step_name]

            explicit_deps = step.get('dependentStatements', {})
            for dep_key in explicit_deps:
                decoded_key = self._decode_dots(dep_key)
                match = re.match(r'^Steps\.([^.]+)', decoded_key)
                if match:
                    dep_step_name = match.group(1)
                    if dep_step_name in step_set:
                        dependencies[step_name].add(dep_step_name)

            implicit_deps = self._extract_dependencies_from_params(step.get('parameterMap'))
            for dep_step_name in implicit_deps:
                if dep_step_name in step_set and dep_step_name != step_name:
                    dependencies[step_name].add(dep_step_name)

        # Kahn's algorithm
        sorted_list: List[str] = []
        in_degree: Dict[str, int] = {sn: len(dependencies[sn]) for sn in step_names}

        queue: List[str] = [sn for sn in step_names if in_degree[sn] == 0]

        while queue:
            current = queue.pop(0)
            sorted_list.append(current)

            for step_name, deps in dependencies.items():
                if current in deps:
                    in_degree[step_name] -= 1
                    if in_degree[step_name] == 0:
                        queue.append(step_name)

        for step_name in step_names:
            if step_name not in sorted_list:
                sorted_list.append(step_name)

        return sorted_list

    async def _steps_to_text(
        self,
        steps: Any,
        nested_structure: Dict[str, _NestedStructure],
        base_indent: int,
    ) -> List[str]:
        """Convert steps to DSL text with nesting."""
        lines: List[str] = []

        if not steps:
            return lines

        # Group statements by parent
        top_level: List[str] = []
        by_parent: Dict[str, Dict[str, List[str]]] = {}

        for step_name in steps:
            nest_info = nested_structure.get(step_name)
            if nest_info and nest_info.parent in steps:
                if nest_info.parent not in by_parent:
                    by_parent[nest_info.parent] = {}
                parent_blocks = by_parent[nest_info.parent]
                if nest_info.block_name not in parent_blocks:
                    parent_blocks[nest_info.block_name] = []
                parent_blocks[nest_info.block_name].append(step_name)
            else:
                top_level.append(step_name)

        sorted_top_level = self._topological_sort(top_level, steps)

        for blocks in by_parent.values():
            for block_name, nested_steps in list(blocks.items()):
                sorted_nested = self._topological_sort(nested_steps, steps)
                blocks[block_name] = sorted_nested

        block_order_map = {
            'error': 0,
            'iteration': 1,
            'true': 2,
            'false': 3,
            'output': 4,
        }

        def block_order(bn: str) -> int:
            return block_order_map.get(bn, 3)

        def render_step_with_nested_blocks(step_name: str, step: Any, indent: int) -> None:
            stmt_lines = self._step_to_text(step, step_name, indent)
            lines.extend(stmt_lines)

            blocks = by_parent.get(step_name)
            if blocks:
                sorted_block_names = sorted(blocks.keys(), key=block_order)

                for bn in sorted_block_names:
                    nested_steps = blocks[bn]
                    lines.append(f"{self._indent(indent + 1)}{bn}")
                    for nested_step_name in nested_steps:
                        nested_step = steps[nested_step_name]
                        render_step_with_nested_blocks(nested_step_name, nested_step, indent + 2)

        for step_name in sorted_top_level:
            step = steps[step_name]
            render_step_with_nested_blocks(step_name, step, base_indent)

        return lines

    def _step_to_text(self, step: Any, step_key: str, indent: int) -> List[str]:
        """Convert single step to DSL text."""
        lines: List[str] = []
        ind = self._indent(indent)

        step_name = step.get('statementName', step_key)
        func_call = f"{step['namespace']}.{step['name']}({self._args_to_text(step.get('parameterMap'))})"
        line = f"{ind}{step_name}: {func_call}"

        # Add AFTER clause
        after_steps: List[str] = []
        deps = step.get('dependentStatements', {})

        for dep_key, dep_value in deps.items():
            if dep_value is not True:
                continue
            decoded_key = self._decode_dots(dep_key)
            after_steps.append(decoded_key)

        if after_steps:
            line += f" AFTER {', '.join(after_steps)}"

        # Add IF clause
        if_steps = list((step.get('executeIftrue') or {}).keys())
        if if_steps:
            line += f" IF {', '.join(if_steps)}"

        # Add comment
        comment = step.get('comment', '')
        if comment and comment.strip():
            escaped_comment = comment.replace('*/', '*\\/')
            line += f" /* {escaped_comment} */"

        lines.append(line)
        return lines

    def _args_to_text(self, parameter_map: Any) -> str:
        """Convert parameter map to argument list string."""
        args: List[str] = []

        if not parameter_map:
            return ''

        for param_name, param_refs in parameter_map.items():
            if not isinstance(param_refs, dict):
                continue
            ref_entries = list(param_refs.values())
            if not ref_entries:
                continue

            sorted_refs = sorted(
                [r for r in ref_entries if isinstance(r, dict)],
                key=lambda r: r.get('order', 0),
            )

            for ref in sorted_refs:
                value = self._param_ref_to_text(ref)
                args.append(f"{param_name} = {value}")

        return ', '.join(args)

    def _param_ref_to_text(self, ref: Any) -> str:
        """Convert parameter reference to text."""
        if ref.get('type') == 'EXPRESSION':
            expr = ref.get('expression')
            if not expr and expr != 0:
                return '``'
            if self._expression_needs_backticks(expr):
                escaped = expr.replace('\\', '\\\\').replace('`', '\\`')
                return f'`{escaped}`'
            return expr
        else:
            return self._value_to_text_preserve_type(ref.get('value'))

    def _expression_needs_backticks(self, expr: str) -> bool:
        """Check if an expression needs to be wrapped in backticks."""
        if not expr:
            return False
        trimmed = expr.strip()

        if '"' in expr:
            return True
        if "'" in expr:
            return True
        if trimmed in ('true', 'false'):
            return True
        if trimmed.startswith('true ') or trimmed.startswith('false '):
            return True
        if trimmed in ('null', 'undefined'):
            return True
        if re.match(r'^-?\d+(\.\d+)?$', trimmed):
            return True
        if re.match(r'^-?\d+(\.\d+)?\s*[+\-*/%]', trimmed):
            return True
        if trimmed in ('[]', '{}'):
            return True

        return False

    def _value_to_text_preserve_type(self, value: Any) -> str:
        """Convert value to text representation."""
        if value is None:
            return 'null'

        if isinstance(value, bool):
            return 'true' if value else 'false'

        if isinstance(value, str):
            escaped = value.replace('\\', '\\\\').replace('"', '\\"')
            return f'"{escaped}"'

        if isinstance(value, (int, float)):
            return str(value)

        if isinstance(value, list):
            if len(value) == 0:
                return '[]'
            items = [self._value_to_text_preserve_type(v) for v in value]
            return f"[{', '.join(items)}]"

        if isinstance(value, dict):
            if ExpressionHandler.is_expression(value):
                return value['value']
            return json.dumps(value, indent=4)

        return str(value)

    def _schema_to_text(self, schema: Any) -> str:
        """Convert schema to text."""
        return SchemaTransformer.to_text(schema)

    def _indent(self, level: int) -> str:
        """Generate indentation."""
        return self._indent_char * level

    @staticmethod
    def _decode_dots(s: str) -> str:
        """Decode MongoDB dot encoding."""
        return s.replace('__d-o-t__', '.')
