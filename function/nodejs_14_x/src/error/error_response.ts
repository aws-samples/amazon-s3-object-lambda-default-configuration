import { AWSError } from 'aws-sdk';
import ErrorCode from './error_code';
import { GetObjectContext } from '../s3objectlambda_event';
import S3 from 'aws-sdk/clients/s3';
import { PromiseResult } from 'aws-sdk/lib/request';

/**
 * Generates a response to Amazon S3 Object Lambda when there is an error.
 */

export async function getErrorResponse (s3Client: S3, requestContext: GetObjectContext,
  errorCode: ErrorCode, errorMessage: string, headers: Headers = new Headers()): Promise<PromiseResult<{}, AWSError>> {
  return s3Client.writeGetObjectResponse({
    RequestRoute: requestContext.outputRoute,
    RequestToken: requestContext.outputToken,
    StatusCode: ERROR_TO_STATUS_CODE_MAP[errorCode],
    ErrorCode: errorCode,
    ErrorMessage: errorMessage,
    ...headers
  }).promise();
}

export async function getResponseForS3Errors (s3Client: S3, requestContext: GetObjectContext, statusCode: number,
  body: Buffer, headers: Headers): Promise<PromiseResult<{}, AWSError>> {
  return s3Client.writeGetObjectResponse({
    RequestRoute: requestContext.outputRoute,
    RequestToken: requestContext.outputToken,
    StatusCode: statusCode,
    Body: body,
    ...headers
  }).promise();
}

/**
 * Maps error codes to HTTP Status codes based on the {@link https://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html|Error Responses}
 * mapping in Amazon S3.
 */

const ERROR_TO_STATUS_CODE_MAP = {
  [ErrorCode.INVALID_REQUEST]: 400,
  [ErrorCode.INVALID_PART]: 400,
  [ErrorCode.INVALID_RANGE]: 416,
  [ErrorCode.NO_SUCH_KEY]: 404
};
