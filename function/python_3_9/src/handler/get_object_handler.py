import checksum
import error
import requests
import transform
from request import utils, validator
from response import MapperResponse, range_mapper, part_number_mapper


def get_request_header(headers):
    """
    Get all headers that should be included in the pre-signed S3 URL. We do not add headers that will be
     applied after transformation, such as Range.
    :param headers: Headers from the GetObject request
    :return: Headers to be sent with pre-signed-url
    """
    new_headers = dict()
    headers_to_be_presigned = ['x-amz-expected-bucket-owner', 'x-amz-request-payer', 'If-Match',
                               'If-Modified-Since', 'If-None-Match', 'If-Unmodified-Since']
    for key, value in headers.items():
        if key in headers_to_be_presigned:
            new_headers[key] = value
    return new_headers


def get_object_handler(s3_client, request_context, user_request):
    """
    Handler for the GetObject Operation
    :param s3_client: s3 client
    :param request_context: GetObject request context
    :param user_request:  user request
    :return: WriteGetObjectResponse
    """
    # Validate user request and return error if invalid
    requests_validation = validator.validate_request(user_request)
    if not requests_validation.is_valid:
        return error.write_error_response(s3_client, request_context, requests.codes.bad_request,
                                        'InvalidRequest', requests_validation.error_msg)

    # Get the original object from Amazon S3
    s3_url = request_context["inputS3Url"]
    request_header = get_request_header(user_request["headers"])

    object_response = requests.get(s3_url, headers=request_header)

    # Check if the get original object request from S3 is successful
    if object_response.status_code != requests.codes.ok:
        # For 304 Not Modified, Error Message dont need to be send
        if object_response.status_code == requests.codes.not_modified:
            return s3_client.write_get_object_response(
                RequestRoute=request_context["outputRoute"],
                RequestToken=request_context["outputToken"],
                StatusCode=object_response.status_code,
            )
        return error.write_error_response_for_s3(s3_client,
                                                 request_context,
                                                 object_response)
    # Transform the object
    original_object = object_response.content

    transformed_whole_object = transform.transform_object(original_object)

    # Handle range or partNumber if present in the request
    partial_object_response = apply_range_or_part_number(transformed_whole_object, user_request)
    if partial_object_response.hasError:
        return error.write_error_response(s3_client, request_context, requests.codes.bad_request,
                                        'InvalidRequest', partial_object_response.error_msg)

    transformed_object = partial_object_response.object

    # Send the transformed object back to Amazon S3 Object Lambda
    transformed_object_checksum = checksum.get_checksum(transformed_object)
    return s3_client.write_get_object_response(RequestRoute=request_context["outputRoute"],
                                               RequestToken=request_context["outputToken"],
                                               Body=transformed_object,
                                               Metadata={
                                                   'body-checksum-algorithm': transformed_object_checksum.algorithm,
                                                   'body-checksum-digest': transformed_object_checksum.digest
                                               })


def apply_range_or_part_number(transformed_object, user_request):
    """
    Apply range or part number request to transformed object
    :param transformed_object: object that need to be apply range or part number
    :param user_request: request from the user
    :return: MapperResponse - response of the range or part number mapper
    """
    range_number = utils.get_range(user_request)
    part_number = utils.get_part_number(user_request)

    if part_number:
        return part_number_mapper.map_part_number(transformed_object, part_number)
    elif range_number:
        return range_mapper.map_range(transformed_object, range_number)
    return MapperResponse(hasError=False, object=transformed_object,
                          error_msg=None)
