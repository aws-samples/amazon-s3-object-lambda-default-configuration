from xml.etree import ElementTree


def write_error_response_for_s3(s3_client, request_context, object_response):
    """
    write WriteGetObjectResponse to the S3 client for error msg received from s3
    :param s3_client: s3 client
    :param request_context: Requests that was sent to supporting Access Point
    :param object_response: Response received from supporting Access Point
    :return: WriteGetObjectResponse
    """
    root = ElementTree.fromstring(object_response.content.decode('utf-8'))
    error_code = root.find('Code').text
    return s3_client.write_get_object_response(
        RequestRoute=request_context['outputRoute'],
        RequestToken=request_context['outputToken'],
        StatusCode=object_response.status_code,
        ErrorCode=error_code,
        ErrorMessage='Received {} from the supporting Access Point.'.format(error_code),
    )


def write_error_response(s3_client, request_context, status_code, error_code, error_message):
    """
    write WriteGetObjectResponse to the S3 client for AWS Lambda errors
    :param s3_client: s3 client
    :param request_context: Requests that was sent to supporting Access Point
    :param status_code: Http status code for the type of error
    :param error_code: Error Code
    :param error_message: Error Message
    :return: WriteGetObjectResponse
    """
    return s3_client.write_get_object_response(
        RequestRoute=request_context['outputRoute'],
        RequestToken=request_context['outputToken'],
        StatusCode=status_code,
        ErrorCode=error_code,
        ErrorMessage=error_message
    )
