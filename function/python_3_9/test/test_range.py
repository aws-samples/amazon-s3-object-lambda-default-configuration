import pytest
from src.response.range_mapper import *

HAS_ERROR_KEY = 'hasError'
OBJECT_KEY = 'object'

def create_buffer(string):
    return bytes(string, encoding='utf8')


def test_invalid_range_format():
    range_response = map_range(create_buffer('hello'), '123')
    assert range_response.hasError


def test_multiple_ranges_format():
    range_response = map_range(create_buffer('hello'), 'bytes=1-2-3')
    assert range_response.hasError


def test_unsupported_ranges():
    range_response = map_range(create_buffer('hello'), 'kilobytes=1-2')
    assert range_response.hasError


def test_invalid_range_value():
    range_response = map_range(create_buffer('hello'), 'bytes=-')
    assert range_response.hasError


def test_range_with_start():
    range_response = map_range(create_buffer('hello'), 'bytes=2-')
    assert range_response.object == create_buffer('llo')


def test_range_with_suffix():
    range_response = map_range(create_buffer('hello'), 'bytes=-2')
    assert range_response.object == create_buffer('lo')


def test_range_with_zero_start_and_end():
    range_response = map_range(create_buffer('hello'), 'bytes=0-3')
    assert range_response.object == create_buffer('hell')


def test_range_with__non_zero_start_and_end():
    range_response = map_range(create_buffer('hello'), 'bytes=1-3')
    assert range_response.object == create_buffer('ell')


def test_two_digits_range():
    range_response = map_range(create_buffer('amazonwebservices'), 'bytes=10-15')
    assert range_response.object == create_buffer('ervice')
