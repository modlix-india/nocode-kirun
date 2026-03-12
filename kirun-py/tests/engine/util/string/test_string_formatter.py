from kirun_py.util.string.string_formatter import StringFormatter


def test_format_no_params():
    assert StringFormatter.format('hello world') == 'hello world'


def test_format_single_param():
    assert StringFormatter.format('hello $', 'world') == 'hello world'


def test_format_multiple_params():
    assert StringFormatter.format('$ and $', 'a', 'b') == 'a and b'


def test_format_escaped_dollar():
    assert StringFormatter.format('price is \\$', 100) == 'price is $'


def test_format_more_placeholders_than_params():
    assert StringFormatter.format('$ $ $', 'a', 'b') == 'a b $'
