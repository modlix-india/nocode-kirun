from __future__ import annotations
from typing import Generic, TypeVar

F = TypeVar('F')
S = TypeVar('S')
T = TypeVar('T')
FR = TypeVar('FR')


class Tuple2(Generic[F, S]):
    def __init__(self, f: F, s: S):
        self._f = f
        self._s = s

    def get_t1(self) -> F:
        return self._f

    def get_t2(self) -> S:
        return self._s

    def set_t1(self, f: F) -> Tuple2[F, S]:
        self._f = f
        return self

    def set_t2(self, s: S) -> Tuple2[F, S]:
        self._s = s
        return self


class Tuple3(Tuple2[F, S], Generic[F, S, T]):
    def __init__(self, f: F, s: S, t: T):
        super().__init__(f, s)
        self._t = t

    def get_t3(self) -> T:
        return self._t

    def set_t3(self, t: T) -> Tuple3[F, S, T]:
        self._t = t
        return self


class Tuple4(Tuple3[F, S, T], Generic[F, S, T, FR]):
    def __init__(self, f: F, s: S, t: T, fr: FR):
        super().__init__(f, s, t)
        self._fr = fr

    def get_t4(self) -> FR:
        return self._fr

    def set_t4(self, fr: FR) -> Tuple4[F, S, T, FR]:
        self._fr = fr
        return self
