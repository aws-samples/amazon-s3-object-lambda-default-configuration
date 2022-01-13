import math
from response import MapperResponse


DEFAULT_SIZE = 5242880


def get_part_error_response(number, total):
    """Get a MapperResponse for Errors"""
    return MapperResponse(hasError=True, object=None,
                          error_msg='Cannot specify part number: {}. Use part numbers 1 to {}.'.format(number, total))


def map_part_number(transformed_object, part_number):
    """
    Map the part number of an object
    :param transformed_object: transformed object
    :param part_number: part number request string
    :return: MapperResponse
    """
    object_length = len(transformed_object)
    total_part = math.ceil(object_length / DEFAULT_SIZE)
    try:
        part_number = int(part_number)
    except ValueError:
        return get_part_error_response(part_number, total_part)
    if part_number > total_part or part_number < 0:
        return get_part_error_response(part_number, total_part)

    start = (part_number - 1) * DEFAULT_SIZE
    end = min(start + DEFAULT_SIZE, object_length)
    return MapperResponse(hasError=False, object=transformed_object[start:end],
                          error_msg=None)
