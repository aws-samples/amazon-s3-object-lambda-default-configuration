import re
from response import MapperResponse

"""
 * Handles range requests by applying the range to the transformed object. Supported range headers are:
 *
 * Range: <unit>=<range-start>-
 * Range: <unit>=<range-start>-<range-end>
 * Range: <unit>=-<suffix-length>
 *
 * Amazon S3 does not support retrieving multiple ranges of data per GetObject request. Please see
 * {@link https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html#API_GetObject_RequestSyntax|GetObject Request Syntax}
 * for more information.
 *
 * The only supported unit in this implementation is `bytes`. If other units are requested, we treat this as
 * an invalid request.
"""


def split_range_str(range_str):
    """
    Split the range string to bytes, start and end.
    :param range_str: Range request string
    :return: tuple of (bytes, start, end) or None
    """
    re_matcher = re.fullmatch(r'([a-z]+)=(\d+)?-(\d+)?', range_str)
    if not re_matcher or len(re_matcher.groups()) != 3:
        return None
    unit, start, end = re_matcher.groups()
    start = int(start) if type(start) == str else None
    end = int(end) if type(end) == str else None
    return unit, start, end


def validate_range_str(range_str):
    """
    Validate the range request string
    :param range_str: Range request string
    :return: A boolean value whether if range string is valid
    """
    ranges = split_range_str(range_str)
    if ranges is None:
        return False
    unit, start, end = ranges
    if unit.lower() != 'bytes':
        return False
    if start is None and end is None:
        return False
    if start and start < 0:
        return False
    if start and end and start > end:
        return False
    return True


def map_range(transformed_object, range_str):
    """
    Map the range to an object
    :param transformed_object: Object to be mapped
    :param range_str: Range request string
    :return: MapperResponse object
    """
    if not validate_range_str(range_str):
        return get_range_error_response(range_str)
    _, start, end = split_range_str(range_str)
    if start is None:
        new_object = transformed_object[-end:]
    elif end is None:
        new_object = transformed_object[start:]
    else:
        new_object = transformed_object[start:end + 1]
    return MapperResponse(hasError=False, object=new_object,
                          error_msg=None)


def get_range_error_response(range_str):
    """Get a MapperResponse for Errors"""
    return MapperResponse(hasError=True, object=None,
                          error_msg='Cannot process specific range: {}'.format(range_str))
