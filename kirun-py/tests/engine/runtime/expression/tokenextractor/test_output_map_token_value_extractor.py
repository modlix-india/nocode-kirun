from __future__ import annotations

import pytest

from kirun_py.runtime.tokenextractor.output_map_token_value_extractor import OutputMapTokenValueExtractor


class TestOutputMapTokenValueExtractor:
    """Ported from OutputMapTokenValueExtractorTest.ts"""

    def test_basic_value_access(self):
        phone = {'phone1': '1234', 'phone2': '5678', 'phone3': '5678'}
        address = {
            'line1': 'Flat 202, PVR Estates',
            'line2': 'Nagvara',
            'city': 'Benguluru',
            'pin': '560048',
            'phone': phone,
        }
        obj = {
            'studentName': 'Kumar',
            'math': 20,
            'isStudent': True,
            'address': address,
        }

        output = {
            'step1': {
                'output': {
                    'zero': 0,
                    'name': 'Kiran',
                    'obj': obj,
                },
            },
        }

        omtv = OutputMapTokenValueExtractor(output)
        assert omtv.get_value('Steps.step1.output.zero') == 0
        assert omtv.get_value('Steps.step1.output.obj.address.phone.phone2') == '5678'

    def test_array_access(self):
        output = {
            'step1': {'output': {'arr': ['a', 'b', 'c']}},
            'step2': {'output': {}},
        }

        omtv = OutputMapTokenValueExtractor(output)
        assert omtv.get_value('Steps.step1.output.arr[1]') == 'b'
        assert omtv.get_value('Steps.step1.output.arr1[1]') is None
        assert omtv.get_value('Steps.step2.output') is not None
