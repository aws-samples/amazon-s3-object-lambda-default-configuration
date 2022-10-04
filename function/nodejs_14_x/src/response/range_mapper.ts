import ErrorCode from '../error/error_code';
import { RangeResponse } from './range_response.types';
import { CONTENT_LENGTH } from '../request/utils';

/**
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
 */
const SUPPORTED_UNIT = 'bytes';

export function mapRange (rangeHeaderValue: string, transformedObject: Buffer): RangeResponse {
  const matchArray = rangeHeaderValue.match(/^([a-z]+)=(\d+)?-(\d+)?$/);

  if (matchArray === null) { // check if the range matches what we support
    return getRangeInvalidResponse(rangeHeaderValue);
  }

  const [, rangeUnitStr, rangeStartStr, rangeEndStr] = matchArray;
  let rangeStart: number;
  let rangeEnd: number;

  if (rangeUnitStr.toLowerCase() !== SUPPORTED_UNIT) {
    return getRangeInvalidResponse(rangeHeaderValue,
            `Cannot process units other than ${SUPPORTED_UNIT}`);
  }

  const isRangeStartPresent = rangeStartStr !== undefined;
  const isRangeEndPresent = rangeEndStr !== undefined;

  if (!isRangeStartPresent && !isRangeEndPresent) { // At least one should be present
    return getRangeInvalidResponse(rangeHeaderValue);
  } else if (!isRangeStartPresent) {
    /* Range request was of the form <unit>=-<suffix-length> so we return the last `suffix-length` bytes. */

    const suffixLength = Number(rangeEndStr);
    rangeEnd = transformedObject.byteLength;
    rangeStart = rangeEnd - suffixLength;
  } else if (!isRangeEndPresent) {
    /* Range request was of the form <unit>=<range-start>- so we return from range-start to the end
        of the object. */
    rangeStart = Number(rangeStartStr);
    rangeEnd = transformedObject.byteLength;
  } else {
    /* Range request was of the form <unit>=<range-start>-<range-end> so we process both. */
    rangeStart = Number(rangeStartStr);
    const expectedLength = Number(rangeEndStr) + 1; // Add 1 as rangeEnd is inclusive
    rangeEnd = Math.min(transformedObject.byteLength, expectedLength); // Should not exceed object length
  }

  const isRangeValid = rangeStart >= 0 && rangeStart <= rangeEnd;
  if (!isRangeValid) {
    return getRangeInvalidResponse(rangeHeaderValue);
  }

  return { object: transformedObject.slice(rangeStart, rangeEnd), hasError: false };
}

export function mapRangeHead (rangeHeaderValue: string, transformedHeaders: Map<string, object>): RangeResponse {
  const matchArray = rangeHeaderValue.match(/^([a-z]+)=(\d+)?-(\d+)?$/);

  if (matchArray === null) { // check if the range matches what we support
    return getRangeInvalidResponse(rangeHeaderValue);
  }

  const [, rangeUnitStr, rangeStartStr, rangeEndStr] = matchArray;
  // Create a copy of the headers in order to not alter the original object
  const newHeaders = new Map(transformedHeaders);
  let rangeStart: number;
  let rangeEnd: number;
  let contentLength: number;

  if (!transformedHeaders.has(CONTENT_LENGTH)) {
    return getRangeInvalidResponse('No content-length was found in the headers');
  } else {
    contentLength = Number(transformedHeaders.get(CONTENT_LENGTH));
  }

  if (rangeUnitStr.toLowerCase() !== SUPPORTED_UNIT) {
    return getRangeInvalidResponse(rangeHeaderValue,
        `Cannot process units other than ${SUPPORTED_UNIT}`);
  }

  const isRangeStartPresent = rangeStartStr !== undefined;
  const isRangeEndPresent = rangeEndStr !== undefined;

  if (!isRangeStartPresent && !isRangeEndPresent) { // At least one should be present
    return getRangeInvalidResponse(rangeHeaderValue);
  } else if (!isRangeStartPresent) {
    /* Range request was of the form <unit>=-<suffix-length> so we return the last `suffix-length` bytes. */

    const suffixLength = Number(rangeEndStr);
    rangeEnd = contentLength;
    rangeStart = rangeEnd - suffixLength;
  } else if (!isRangeEndPresent) {
    /* Range request was of the form <unit>=<range-start>- so we return from range-start to the end
        of the object. */
    rangeStart = Number(rangeStartStr);
    rangeEnd = contentLength;
  } else {
    /* Range request was of the form <unit>=<range-start>-<range-end> so we process both. */
    rangeStart = Number(rangeStartStr);
    const expectedLength = Number(rangeEndStr) + 1; // Add 1 as rangeEnd is inclusive
    rangeEnd = Math.min(contentLength, expectedLength); // Should not exceed object length
  }

  const isRangeValid = rangeStart >= 0 && rangeStart <= rangeEnd;
  if (!isRangeValid) {
    return getRangeInvalidResponse(rangeHeaderValue);
  }

  // Set the new Content-Length accordingly.
  newHeaders.set(CONTENT_LENGTH, Object(rangeEnd - rangeStart));
  return { headers: newHeaders, hasError: false };
}

function getRangeInvalidResponse (rangeHeaderValue: string, errorMessage?: string): RangeResponse {
  const message = (errorMessage === undefined) ? `Cannot process specified range: ${rangeHeaderValue}` : errorMessage;

  return {
    hasError: true,
    errorCode: ErrorCode.INVALID_RANGE,
    errorMessage: message
  };
}
