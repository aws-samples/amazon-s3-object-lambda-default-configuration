from request import utils
from response import range_mapper
from collections import namedtuple

RequestsValidation = namedtuple('RequestsValidation', ['is_valid', 'error_msg'])


def validate_request(user_request):
    """
    Validate the user request
    :param user_request: User request received from AWS Object Lambda
    :return: RequestsValidation Object
    """
    range_request = utils.get_range(user_request)
    part_number_request = utils.get_part_number(user_request)

    if range_request and part_number_request:
        return RequestsValidation(False, 'Cannot specify both Range and Part Number in Query')
    if range_request and not range_mapper.validate_range_str(range_request):
        return RequestsValidation(False, 'Cannot process specific range: {}'.format(range_request))
    return RequestsValidation(True, '')
