import { AWSError } from 'aws-sdk';
import { Parser } from 'xml2js';
import ErrorCode from './error_code';
import { BaseObjectContext, GetObjectContext } from '../s3objectlambda_event.types';
import S3 from 'aws-sdk/clients/s3';
import { PromiseResult } from 'aws-sdk/lib/request';
import { Response, Headers } from 'node-fetch';
import { IErrorResponse } from '../s3objectlambda_response.types';

/**
 * Generates a response to Amazon S3 Object Lambda when there is an error
 * with a GET_OBJECT request.
 */
export async function getErrorResponse (s3Client: S3, requestContext: GetObjectContext,
  errorCode: ErrorCode, errorMessage: string, headers: Headers = new Headers()): Promise<PromiseResult<{}, AWSError>> {
  console.log(`Returning an error [${errorCode}] ${errorMessage} to the Object Lambda Access Point`);

  return s3Client.writeGetObjectResponse({
    RequestRoute: requestContext.outputRoute,
    RequestToken: requestContext.outputToken,
    StatusCode: ERROR_TO_STATUS_CODE_MAP[errorCode],
    ErrorCode: errorCode,
    ErrorMessage: errorMessage,
    ...headers
  }).promise();
}

/**
 * Generates a response to Amazon S3 Object Lambda when there is an error
 * with a HEAD_OBJECT / LIST_OBJECTS_V1 / LIST_OBJECTS_V2 request.
 */
export function errorResponse (requestContext: BaseObjectContext,
  errorCode: ErrorCode, errorMessage: string, headers: Map<string, object> = new Map<string, object>()): IErrorResponse {
  console.log(`Returning an error [${errorCode}] ${errorMessage} to the Object Lambda Access Point`);

  return {
    statusCode: ERROR_TO_STATUS_CODE_MAP[errorCode],
    errorCode: errorCode,
    errorMessage: errorMessage,
    ...Object.fromEntries(headers)
  };
}

/**
 * Sends back the error that was received from S3 on a GET_OBJECT request
 */
export async function getResponseForS3Errors (s3Client: S3, requestContext: GetObjectContext, objectResponse: Response,
  headers: Headers, objectResponseBody: Buffer): Promise<PromiseResult<{}, AWSError>> {
  const objectResponseData = await new Parser().parseStringPromise(objectResponseBody);
  console.log(`Encountered an S3 Error, status code: ${objectResponse.status}. Forwarding this to the Object Lambda Access Point.`);

  return s3Client.writeGetObjectResponse({
    RequestRoute: requestContext.outputRoute,
    RequestToken: requestContext.outputToken,
    StatusCode: objectResponse.status,
    ErrorCode: objectResponseData.Code,
    ErrorMessage: `Received ${objectResponse.statusText} from the supporting Access Point.`,
    ...headers
  }).promise();
}

/**
 * Sends back the error that was received from S3 on a HEAD_OBJECT / LIST_OBJECTS request
 */
export function responseForS3Errors (objectResponse: Response): IErrorResponse {
  console.log(`Encountered an S3 Error, status code: ${objectResponse.status}. Forwarding this to the Object Lambda Access Point.`);

  return {
    statusCode: objectResponse.status,
    errorMessage: `Received ${objectResponse.statusText} from the supporting Access Point.`
  };
}

/**
 * Maps error codes to HTTP Status codes based on the {@link https://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html | Error Responses}
 * mapping in Amazon S3.
 */
const ERROR_TO_STATUS_CODE_MAP = {
  [ErrorCode.INVALID_REQUEST]: 400,
  [ErrorCode.INVALID_PART]: 400,
  [ErrorCode.INVALID_RANGE]: 416,
  [ErrorCode.NO_SUCH_KEY]: 404
};
