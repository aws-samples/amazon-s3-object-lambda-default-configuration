import json
from src.request.utils import *


def test_get_part_number():
    user_request = {
        'url': 'https://s3.amazonaws.com?partNumber=1',
        'headers': {
            'h1': 'v1'
        }
    }
    assert get_part_number(user_request) == '1'


def test_get_part_number_case_insensitive():
    user_request = {
        'url': 'https://s3.amazonaws.com?hello=world&PARTnumber=1',
        'headers': {
            'h1': 'v1'
        }
    }
    assert get_part_number(user_request) == '1'


def test_get_part_number_not_exist():
    user_request = {
        'url': 'https://s3.amazonaws.com?hello=world&Range=1',
        'headers': {
            'h1': 'v1'
        }
    }
    assert get_part_number(user_request) is None


def test_get_range_from_query_param():
    user_request = {
        'url': 'https://s3.amazonaws.com?range=bytes=1',
        'headers': {
            'h1': 'v1'
        }
    }
    assert get_range(user_request) == 'bytes=1'


def test_get_range_from_query_param_case_insensitive():
    user_request = {
        'url': 'https://s3.amazonaws.com?raNGe=bytes=1',
        'headers': {
            'h1': 'v1'
        }
    }
    assert get_range(user_request) == 'bytes=1'


def test_get_range_from_header():
    user_request = {
        'url': 'https://s3.amazonaws.com',
        'headers': {
            'Range': 'bytes=3-'
        }
    }
    assert get_range(user_request) == 'bytes=3-'


def test_get_range_from_header_case_insensitive():
    user_request = {
        'url': 'https://s3.amazonaws.com',
        'headers': {
            'RANge': 'bytes=3-'
        }
    }
    assert get_range(user_request) == 'bytes=3-'
