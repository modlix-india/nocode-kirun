import uuid as _uuid


def uuid() -> str:
    return str(_uuid.uuid4())
