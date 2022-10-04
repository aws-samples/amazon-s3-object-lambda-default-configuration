import { GetObjectContext, UserRequest } from '../s3objectlambda_event.types';
import { makeS3Request, applyRangeOrPartNumber } from '../request/utils';
import { AWSError } from 'aws-sdk/lib/error';
import ErrorCode from '../error/error_code';
import { Response, Headers } from 'node-fetch';
import { getErrorResponse, getResponseForS3Errors } from '../error/error_response';
import S3 from 'aws-sdk/clients/s3';
import { transformObject } from '../transform/s3objectlambda_transformer';
import { validate } from '../request/validator';
import { PromiseResult } from 'aws-sdk/lib/request';
import getChecksum from '../checksum/checksum';
import { headerToWgorParam, ParamsKeys } from '../response/param_transformer';

/**
 * Handles a GetObject request, by performing the following steps:
 * 1. Validates the incoming user request.
 * 2. Retrieves the original object from Amazon S3.
 * 3. Applies a transformation. You can apply your custom transformation logic here.
 * 4. Handles post-processing of the transformation, such as applying range or part numbers.
 * 5. Sends the final transformed object back to Amazon S3 Object Lambda.
 */

export default async function handleGetObjectRequest (s3Client: S3, requestContext: GetObjectContext, userRequest: UserRequest):
Promise<PromiseResult<{}, AWSError> | null> {
  // Validate user request and return error if invalid
  const errorMessage = validate(userRequest);
  if (errorMessage != null) {
    return getErrorResponse(s3Client, requestContext, ErrorCode.INVALID_REQUEST, errorMessage);
  }

  // Read the original object from Amazon S3
  const objectResponse = await makeS3Request(requestContext.inputS3Url, userRequest, 'GET');
  const originalObject = Buffer.from(await objectResponse.arrayBuffer());

  if (originalObject === null) {
    return getErrorResponse(s3Client, requestContext, ErrorCode.NO_SUCH_KEY, 'Requested key does not exist');
  }

  const responseHeaders = getResponseHeaders(objectResponse.headers);
  if (objectResponse.status >= 400) {
    // Errors in the Amazon S3 response should be forwarded to the caller without invoking transformObject.
    return getResponseForS3Errors(s3Client, requestContext, objectResponse, responseHeaders, originalObject);
  }

  if (objectResponse.status >= 300 && objectResponse.status < 400) {
    // Handle the redirect scenarios here such as Not Modified (304), Moved Permanently (301)
    return writeResponse(s3Client, requestContext, originalObject, responseHeaders, objectResponse);
  }

  // Transform the object
  const transformedWholeObject = transformObject(originalObject);

  // Handle range or partNumber if present in the request
  const transformedObject = applyRangeOrPartNumber(transformedWholeObject, userRequest);

  // Send the transformed object or error back to Amazon S3 Object Lambda
  if (transformedObject.hasError) {
    return getErrorResponse(s3Client, requestContext, ErrorCode.INVALID_REQUEST, String(transformedObject.errorMessage), responseHeaders);
  }
  if (transformedObject.object !== undefined) {
    return writeResponse(s3Client, requestContext, transformedObject.object, responseHeaders, objectResponse);
  }
  return null;
}

/**
 * Removes headers that will be invalidated by the transformation eg: Content-Length and ETag.
 */
function getResponseHeaders (headers: Headers): Headers {
  headers.delete('Content-Length');
  headers.delete('ETag');

  return headers;
}

/**
 * Send the transformed object back to Amazon S3 Object Lambda, by invoking the WriteGetObjectResponse API.
 */
async function writeResponse (s3Client: S3, requestContext: GetObjectContext, transformedObject: Buffer,
  headers: Headers, objectResponse: Response): Promise<PromiseResult<{}, AWSError>> {
  const { algorithm, digest } = getChecksum(transformedObject);

  const WGORParams = new Map<string, any>();

  // Create the Map with the params for WGOR
  headers.forEach((value, key) => {
    const paramKey = headerToWgorParam(key);
    if (ParamsKeys.includes(paramKey) && value !== '' && value !== null) {
      if (paramKey === 'LastModified') {
        WGORParams.set(paramKey, new Date(value));
      } else {
        WGORParams.set(paramKey, value);
      }
    }
  });

  console.log('Sending transformed results to the Object Lambda Access Point');
  return s3Client.writeGetObjectResponse({
    RequestRoute: requestContext.outputRoute,
    RequestToken: requestContext.outputToken,
    StatusCode: objectResponse.status,
    Body: transformedObject,
    Metadata: {
      'body-checksum-algorithm': algorithm,
      'body-checksum-digest': digest

    },
    ...Object.fromEntries(WGORParams)
  }).promise();
}
