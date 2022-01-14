import pytest
from src.response.part_number_mapper import *

TEN_KB = 'helloworld' * 1024
HUNDRED_KB = TEN_KB * 10

def create_buffer(string):
    return bytes(string, encoding='utf8')


def test_invalid_part_number():
    part_number_response = map_part_number(create_buffer('Hello'), 'abc')
    assert part_number_response.hasError


def test_negative_part_number():
    part_number_response = map_part_number(create_buffer('Hello'), '-1')
    assert part_number_response.hasError


def test_positive_non_integeer():
    part_number_response = map_part_number(create_buffer('Hello'), '1.1')
    assert part_number_response.hasError


def test_large_part_number():
    part_number_response = map_part_number(create_buffer('Hello'), '10')
    assert part_number_response.hasError


def test_valid_part_number():
    part_number_response = map_part_number(create_buffer(HUNDRED_KB), '1')
    assert part_number_response.object == create_buffer(TEN_KB)


def test_part_number_if_length_not_divisible():
    part_number_response = map_part_number(create_buffer(HUNDRED_KB + 'hundredkilobytes'), '2')
    assert part_number_response.object == create_buffer(TEN_KB)