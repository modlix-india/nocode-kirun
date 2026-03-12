from __future__ import annotations
from typing import TypeVar, Generic, Callable, Any

from kirun_py.util.deep_equal import deep_equal

_T = TypeVar('_T')
_U = TypeVar('_U')


class _Node(Generic[_T]):
    __slots__ = ('value', 'next', 'previous')

    def __init__(self, value: _T, previous: _Node[_T] | None = None, next_: _Node[_T] | None = None):
        self.value = value
        self.next = next_
        self.previous = previous

    def __str__(self) -> str:
        return str(self.value)


class LinkedList(Generic[_T]):
    def __init__(self, items: list[_T] | None = None):
        self._head: _Node[_T] | None = None
        self._tail: _Node[_T] | None = None
        self.length: int = 0

        if items:
            for t in items:
                if self._head is None:
                    self._tail = self._head = _Node(t)
                else:
                    node = _Node(t, self._tail)
                    self._tail.next = node
                    self._tail = node
            self.length = len(items)

    def push(self, value: _T) -> None:
        node = _Node(value, next_=self._head)
        if self._head is None:
            self._tail = self._head = node
        else:
            self._head.previous = node
            self._head = node
        self.length += 1

    def pop(self) -> _T:
        if self._head is None:
            raise RuntimeError('List is empty and cannot pop further.')
        value = self._head.value
        self.length -= 1

        if self._head is self._tail:
            self._head = self._tail = None
            return value

        node = self._head
        self._head = node.next
        node.next = None
        node.previous = None
        if self._head is not None:
            self._head.previous = None
        return value

    def is_empty(self) -> bool:
        return self.length == 0

    def size(self) -> int:
        return self.length

    def get(self, index: int) -> _T:
        if index < 0 or index >= self.length:
            raise IndexError(f'{index} is out of bounds [0,{self.length}]')
        x = self._head
        while index > 0:
            x = x.next
            index -= 1
        return x.value

    def set(self, index: int, value: _T) -> LinkedList[_T]:
        if index < 0 or index >= self.length:
            from kirun_py.exception import KIRuntimeException
            from kirun_py.util.string.string_formatter import StringFormatter
            raise KIRuntimeException(
                StringFormatter.format('Index $ out of bound to set the value in linked list.', index)
            )
        x = self._head
        while index > 0:
            x = x.next
            index -= 1
        x.value = value
        return self

    def __str__(self) -> str:
        parts: list[str] = []
        x = self._head
        while x is not None:
            parts.append(str(x.value))
            x = x.next
        return f'[{", ".join(parts)}]'

    def to_array(self) -> list[_T]:
        arr: list[_T] = []
        x = self._head
        while x is not None:
            arr.append(x.value)
            x = x.next
        return arr

    def peek(self) -> _T:
        if self._head is None:
            raise RuntimeError('List is empty so cannot peek')
        return self._head.value

    def peek_last(self) -> _T:
        if self._tail is None:
            raise RuntimeError('List is empty so cannot peek')
        return self._tail.value

    def get_first(self) -> _T:
        if self._head is None:
            raise RuntimeError('List is empty so cannot get first')
        return self._head.value

    def remove_first(self) -> _T:
        return self.pop()

    def remove_last(self) -> _T:
        if self._tail is None:
            raise RuntimeError('List is empty so cannot remove')
        self.length -= 1
        v = self._tail.value
        if self.length == 0:
            self._head = self._tail = None
        else:
            n = self._tail.previous
            n.next = None
            self._tail.previous = None
            self._tail = n
        return v

    def add_all(self, items: list[_T]) -> LinkedList[_T]:
        if not items:
            return self
        for item in items:
            self.add(item)
        return self

    def add(self, t: _T) -> LinkedList[_T]:
        self.length += 1
        if self._tail is None and self._head is None:
            self._head = self._tail = _Node(t)
        elif self._head is self._tail:
            self._tail = _Node(t, self._head)
            self._head.next = self._tail
        else:
            self._tail = _Node(t, self._tail)
            self._tail.previous.next = self._tail
        return self

    def map(self, callback: Callable[[_T, int], _U]) -> LinkedList[_U]:
        new_list: LinkedList[_U] = LinkedList()
        x = self._head
        index = 0
        while x is not None:
            new_list.add(callback(x.value, index))
            x = x.next
            index += 1
        return new_list

    def index_of(self, value: _T) -> int:
        x = self._head
        index = 0
        while x is not None:
            if deep_equal(x.value, value):
                return index
            x = x.next
            index += 1
        return -1

    def for_each(self, callback: Callable[[_T, int], None]) -> None:
        x = self._head
        index = 0
        while x is not None:
            callback(x.value, index)
            x = x.next
            index += 1
