import { GetObjectContext, UserRequest } from '../s3objectlambda_event';
import { getPartNumber, getRange } from '../request/utils';

import { AWSError } from 'aws-sdk/lib/error';
import ErrorCode from '../error/error_code';
import fetch from 'node-fetch';
import { getErrorResponse, getResponseForS3Errors } from '../error/error_response';
import mapPartNumber from '../response/part_number_mapper';
import mapRange from '../response/range_mapper';
import RangeResponse from '../response/range_response';
import S3 from 'aws-sdk/clients/s3';
import transformObject from '../transform/s3objectlambda_transformer';
import validate from '../request/validator';
import { PromiseResult } from 'aws-sdk/lib/request';
import getChecksum from '../checksum/checksum';

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
  const objectResponse = await fetch(requestContext.inputS3Url);
  const originalObject = await objectResponse.buffer();

  if (originalObject === null) {
    return getErrorResponse(s3Client, requestContext, ErrorCode.NO_SUCH_KEY, 'Requested key does not exist');
  }

  const responseHeaders = getResponseHeaders(objectResponse.headers);

  if (!objectResponse.ok) {
    // Errors in the Amazon S3 response should be forwarded to the caller without invoking transformObject.
    return getResponseForS3Errors(s3Client, requestContext, objectResponse.status, originalObject, responseHeaders);
  }

  // Transform the object
  const transformedWholeObject = transformObject(originalObject);

  // Handle range or partNumber if present in the request
  const transformedObject = applyRangeOrPartNumber(transformedWholeObject, userRequest);

  // Send the transformed object or error back to Amazon S3 Object Lambda
  if (transformedObject.hasError) {
    return getErrorResponse(s3Client, requestContext, ErrorCode.INVALID_REQUEST, String(transformedObject.errorMessage), responseHeaders);
  } else if (transformedObject.object !== undefined) {
    return writeResponse(s3Client, requestContext, transformedObject.object, responseHeaders);
  } else {
    return null;
  }
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
  headers: Headers): Promise<PromiseResult<{}, AWSError>> {
  const { algorithm, digest } = getChecksum(transformedObject);

  return s3Client.writeGetObjectResponse({
    RequestRoute: requestContext.outputRoute,
    RequestToken: requestContext.outputToken,
    Body: transformedObject,
    Metadata: {
      'body-checksum-algorithm': algorithm,
      'body-checksum-digest': digest
    },
    ...headers
  }).promise();
}

function applyRangeOrPartNumber (transformedObject: Buffer, userRequest: UserRequest): RangeResponse {
  /*
     * Check if the request context has range or partNumber parameter. This helps us handle a ranged request
     * and return only the requested range to the GetObject caller. For more information on range and partNumber,
     * see {@link https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html#API_GetObject_RequestSyntax|GetObject Request Syntax}
     * in the Amazon S3 API Reference.
    */
  const range = getRange(userRequest);
  const partNumber = getPartNumber(userRequest);

  if (range != null) {
    return mapRange(range, transformedObject);
  } else if (partNumber != null) {
    return mapPartNumber(partNumber, transformedObject);
  } else {
    // The request was made for the whole object, so return as is.
    return { object: transformedObject, hasError: false };
  }
}
