from urllib.parse import urlparse
from urllib.parse import parse_qsl

PART_NUMBER = 'partNumber'
RANGE = 'Range'


def get_part_number(user_request):
    """
    Get the part number from user request
    :param user_request: User request
    :return: part number string or None
    """
    return get_query_param(user_request['url'], PART_NUMBER)


def get_range(user_request):
    """
    Get range from user request which can be in headers or url query
    :param user_request: User request
    :return: range string or None
    """
    request_header = {k.lower(): v for k, v in user_request["headers"].items()}
    if RANGE.lower() in request_header:
        return request_header[RANGE.lower()]
    return get_query_param(user_request['url'], RANGE)


def get_query_param(url, name):
    """Get a specific query parameter from url"""
    url = url.lower()
    name = name.lower()
    parse_query = dict(parse_qsl(urlparse(url).query))
    if name in parse_query:
        return parse_query[name]
    return None