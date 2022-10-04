import { BaseObjectContext, UserRequest } from '../s3objectlambda_event.types';
import { validate } from '../request/validator';
import { errorResponse, responseForS3Errors } from '../error/error_response';
import ErrorCode from '../error/error_code';
import { transformHeaders } from '../transform/s3objectlambda_transformer';
import { IErrorResponse, IHeadObjectResponse, IResponse } from '../s3objectlambda_response.types';
import { applyRangeOrPartNumberHeaders, makeS3Request } from '../request/utils';

/**
 * Handles a HeadObject request, by performing the following steps:
 * 1. Validates the incoming user request.
 * 2. Retrieves the headers from Amazon S3.
 * 3. Applies a transformation. You can apply your custom transformation logic here.
 * 3. Sends the final transformed headers back to Amazon S3 Object Lambda.
 */
export default async function handleHeadObjectRequest (requestContext: BaseObjectContext,
  userRequest: UserRequest): Promise<IResponse> {
  // Validate user request and return error if invalid
  const errorMessage = validate(userRequest);
  if (errorMessage != null) {
    return errorResponse(requestContext, ErrorCode.INVALID_REQUEST, errorMessage);
  }

  // Read the original object from Amazon S3
  const objectResponse = await makeS3Request(requestContext.inputS3Url, userRequest, 'HEAD');

  if (objectResponse.headers == null) {
    return errorResponse(requestContext, ErrorCode.NO_SUCH_KEY, 'Requested key does not exist');
  }

  // Get the Headers as Map
  const rawResponseHeaders = objectResponse.headers.raw();
  const originalHeaders = new Map<string, any>();
  Object.keys(rawResponseHeaders).forEach(key => originalHeaders.set(key, rawResponseHeaders[key][0]));

  if (objectResponse.status >= 400) {
    // Errors in the Amazon S3 response should be forwarded to the caller without invoking transformObject.
    return responseForS3Errors(objectResponse);
  }

  if (objectResponse.status >= 300 && objectResponse.status < 400) {
    // Handle the redirect scenarios here such as Not Modified (304), Moved Permanently (301)
    return getHeadResponse(objectResponse.status, originalHeaders);
  }
  // Transform the Headers
  const transformedHeaders = transformHeaders(originalHeaders);
  // Handling range or partNumber
  const transformedHeadersWithRange = applyRangeOrPartNumberHeaders(transformedHeaders, userRequest);
  if (transformedHeadersWithRange.hasError || transformedHeadersWithRange.headers === undefined) {
    return errorResponse(requestContext,
      transformedHeadersWithRange.errorCode !== undefined
        ? transformedHeadersWithRange.errorCode
        : ErrorCode.INVALID_REQUEST,
      String(transformedHeadersWithRange.errorMessage),
      transformedHeadersWithRange.headers
    );
  }

  return getHeadResponse(200, transformedHeadersWithRange.headers);
};

/**
 * Returns the object expected from Object Lambda after a HEAD request
 * @param statusCode The statusCode to be sent back.
 * @param headers The headers which will be sent back.
 */
function getHeadResponse (statusCode: number, headers: Map<string, object>): IHeadObjectResponse | IErrorResponse {
  return {
    statusCode,
    headers: Object.fromEntries(headers.entries())
  };
}
