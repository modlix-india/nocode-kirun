from kirun_py.util.linked_list import LinkedList


def test_push_and_pop():
    ll = LinkedList()
    ll.push(1)
    ll.push(2)
    ll.push(3)
    assert ll.pop() == 3
    assert ll.pop() == 2
    assert ll.pop() == 1


def test_add_and_to_array():
    ll = LinkedList()
    ll.add(1)
    ll.add(2)
    ll.add(3)
    assert ll.to_array() == [1, 2, 3]


def test_is_empty():
    ll = LinkedList()
    assert ll.is_empty() is True
    ll.push(1)
    assert ll.is_empty() is False


def test_size():
    ll = LinkedList([1, 2, 3])
    assert ll.size() == 3


def test_get():
    ll = LinkedList([10, 20, 30])
    assert ll.get(0) == 10
    assert ll.get(1) == 20
    assert ll.get(2) == 30


def test_peek():
    ll = LinkedList()
    ll.push(5)
    ll.push(10)
    assert ll.peek() == 10


def test_peek_last():
    ll = LinkedList()
    ll.add(5)
    ll.add(10)
    assert ll.peek_last() == 10


def test_remove_last():
    ll = LinkedList()
    ll.add(1)
    ll.add(2)
    ll.add(3)
    assert ll.remove_last() == 3
    assert ll.size() == 2


def test_map():
    ll = LinkedList([1, 2, 3])
    mapped = ll.map(lambda v, i: v * 2)
    assert mapped.to_array() == [2, 4, 6]


def test_index_of():
    ll = LinkedList([10, 20, 30])
    assert ll.index_of(20) == 1
    assert ll.index_of(99) == -1


def test_to_string():
    ll = LinkedList([1, 2, 3])
    assert str(ll) == '[1, 2, 3]'


def test_constructor_with_list():
    ll = LinkedList([4, 5, 6])
    assert ll.to_array() == [4, 5, 6]
    assert ll.size() == 3


def test_add_all():
    ll = LinkedList()
    ll.add_all([1, 2, 3])
    assert ll.to_array() == [1, 2, 3]
