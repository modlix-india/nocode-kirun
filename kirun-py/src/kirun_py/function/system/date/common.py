from __future__ import annotations

from datetime import datetime, timezone
from typing import Optional

from dateutil import parser as dateutil_parser

try:
    from zoneinfo import ZoneInfo
except ImportError:
    from backports.zoneinfo import ZoneInfo  # type: ignore[no-redef]


def get_datetime(iso_timestamp: str) -> datetime:
    """Parse an ISO timestamp string into a timezone-aware datetime.

    If no timezone info is present, UTC is assumed.
    """
    try:
        dt = dateutil_parser.isoparse(iso_timestamp)
    except (ValueError, TypeError) as e:
        raise ValueError('Invalid ISO timestamp') from e

    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)

    return dt


def datetime_to_iso(dt: datetime) -> str:
    """Convert a datetime to ISO 8601 string with timezone offset."""
    return dt.isoformat()
