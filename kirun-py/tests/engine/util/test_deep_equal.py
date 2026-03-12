from kirun_py.util.deep_equal import deep_equal


def test_primitives_equal():
    assert deep_equal(1, 1) is True
    assert deep_equal('hello', 'hello') is True
    assert deep_equal(True, True) is True


def test_primitives_not_equal():
    assert deep_equal(1, 2) is False
    assert deep_equal('a', 'b') is False


def test_arrays_equal():
    assert deep_equal([1, 2, 3], [1, 2, 3]) is True


def test_arrays_not_equal():
    assert deep_equal([1, 2], [1, 3]) is False
    assert deep_equal([1, 2], [1, 2, 3]) is False


def test_objects_equal():
    assert deep_equal({'a': 1, 'b': 2}, {'a': 1, 'b': 2}) is True


def test_objects_not_equal():
    assert deep_equal({'a': 1}, {'a': 2}) is False
    assert deep_equal({'a': 1}, {'b': 1}) is False


def test_nested_structures():
    assert deep_equal(
        {'a': [1, {'b': 2}]},
        {'a': [1, {'b': 2}]},
    ) is True

    assert deep_equal(
        {'a': [1, {'b': 2}]},
        {'a': [1, {'b': 3}]},
    ) is False


def test_same_reference():
    x = [1, 2, 3]
    assert deep_equal(x, x) is True
