from __future__ import annotations

from kirun_py.function.system.string.abstract_string_function import AbstractStringFunction
from kirun_py.function.system.string.concatenate import Concatenate
from kirun_py.function.system.string.delete_for_given_length import DeleteForGivenLength
from kirun_py.function.system.string.insert_at_given_position import InsertAtGivenPosition
from kirun_py.function.system.string.matches import Matches
from kirun_py.function.system.string.post_pad import PostPad
from kirun_py.function.system.string.pre_pad import PrePad
from kirun_py.function.system.string.region_matches import RegionMatches
from kirun_py.function.system.string.replace_at_given_position import ReplaceAtGivenPosition
from kirun_py.function.system.string.reverse import Reverse
from kirun_py.function.system.string.split import Split
from kirun_py.function.system.string.string_function_repository import StringFunctionRepository
from kirun_py.function.system.string.to_string import ToString
from kirun_py.function.system.string.trim_to import TrimTo

__all__ = [
    'AbstractStringFunction',
    'Concatenate',
    'DeleteForGivenLength',
    'InsertAtGivenPosition',
    'Matches',
    'PostPad',
    'PrePad',
    'RegionMatches',
    'ReplaceAtGivenPosition',
    'Reverse',
    'Split',
    'StringFunctionRepository',
    'ToString',
    'TrimTo',
]
