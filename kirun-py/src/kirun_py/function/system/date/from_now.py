from __future__ import annotations

from datetime import datetime, timezone
from typing import Any, Dict, List, Optional, TYPE_CHECKING

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


def _relative_time(given: datetime, base: datetime, units: Optional[List[str]] = None, rounding: bool = True) -> str:
    """Compute a human-readable relative time string (e.g. '3 days ago', 'in 2 hours')."""
    from dateutil.relativedelta import relativedelta

    if given >= base:
        delta = relativedelta(given, base)
        suffix = 'from now'
    else:
        delta = relativedelta(base, given)
        suffix = 'ago'

    # Build parts from largest to smallest
    _unit_order = [
        ('years', 'year'),
        ('months', 'month'),
        ('days', 'day'),
        ('hours', 'hour'),
        ('minutes', 'minute'),
        ('seconds', 'second'),
    ]

    if units:
        lower_units = [u.lower() for u in units]
        _unit_order = [(plural, singular) for (plural, singular) in _unit_order if plural in lower_units or singular in lower_units]

    parts: List[str] = []
    for plural_name, singular_name in _unit_order:
        value = getattr(delta, plural_name, 0)
        if rounding:
            value = int(value)
        if value == 0:
            continue
        if value == 1:
            parts.append('1 {}'.format(singular_name))
        else:
            parts.append('{} {}'.format(value, plural_name))

    if not parts:
        # Find the smallest unit we are tracking
        if _unit_order:
            _, singular = _unit_order[-1]
            return '0 {}s {}'.format(singular, suffix)
        return '0 seconds {}'.format(suffix)

    return '{} {}'.format(', '.join(parts), suffix)


class FromNow(AbstractDateFunction):

    PARAMETER_BASE_NAME: str = 'base'
    PARAMETER_BASE: Parameter = Parameter(
        PARAMETER_BASE_NAME,
        Schema.of_ref(Namespaces.DATE + '.Timestamp').set_default_value(''),
    )

    PARAMETER_LOCALE_NAME: str = 'locale'
    PARAMETER_LOCALE: Parameter = Parameter(
        PARAMETER_LOCALE_NAME,
        Schema.of_string(PARAMETER_LOCALE_NAME).set_default_value('system'),
    )

    PARAMETER_FORMAT_NAME: str = 'format'
    PARAMETER_FORMAT: Parameter = Parameter(
        PARAMETER_FORMAT_NAME,
        Schema.of_string(PARAMETER_FORMAT_NAME)
        .set_enums(['LONG', 'SHORT', 'NARROW'])
        .set_default_value('LONG'),
    )

    PARAMETER_ROUND_NAME: str = 'round'
    PARAMETER_ROUND: Parameter = Parameter(
        PARAMETER_ROUND_NAME,
        Schema.of_boolean(PARAMETER_ROUND_NAME).set_default_value(True),
    )

    def __init__(self) -> None:
        super().__init__(
            'FromNow',
            AbstractDateFunction.EVENT_STRING,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            FromNow.PARAMETER_FORMAT,
            FromNow.PARAMETER_BASE,
            AbstractDateFunction.PARAMETER_VARIABLE_UNIT,
            FromNow.PARAMETER_ROUND,
            FromNow.PARAMETER_LOCALE,
        )

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}

        base_str = args.get(FromNow.PARAMETER_BASE_NAME, '')
        if not base_str:
            base_dt = datetime.now(timezone.utc)
        else:
            base_dt = get_datetime(base_str)

        given_str = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
        given_dt = get_datetime(given_str)

        units = args.get(AbstractDateFunction.PARAMETER_UNIT_NAME)
        rounding = args.get(FromNow.PARAMETER_ROUND_NAME, True)

        result = _relative_time(
            given_dt,
            base_dt,
            units=units if units else None,
            rounding=bool(rounding),
        )

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_RESULT_NAME: result}
            ),
        ])
