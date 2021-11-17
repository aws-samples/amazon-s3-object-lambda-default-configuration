import ErrorCode from '../error/error_code';
import RangeResponse from './range_response';

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

export default function mapRange (rangeHeaderValue: string, transformedObject: Buffer): RangeResponse {
  const matchArray = rangeHeaderValue.match(/^([a-z]+)=(\d+)?-(\d+)?$/);

  if (matchArray === null) { // check if the range matches what we support
    return getRangeInvalidResponse(rangeHeaderValue);
  }

  if (matchArray[1].toLowerCase() !== SUPPORTED_UNIT) {
    return getRangeInvalidResponse(rangeHeaderValue,
            `Cannot process units other than ${SUPPORTED_UNIT}`);
  }

  const rangeStartStr = matchArray[2];
  const rangeEndStr = matchArray[3];
  let rangeStart: number;
  let rangeEnd: number;

  if (rangeStartStr === undefined && rangeEndStr === undefined) { // At least one should be present
    return getRangeInvalidResponse(rangeHeaderValue);
  } else if (rangeStartStr === undefined) {
    /* Range request was of the form <unit>=-<suffix-length> so we return the last `suffix-length` bytes. */

    const suffixLength = Number(rangeEndStr);
    rangeEnd = transformedObject.byteLength;
    rangeStart = rangeEnd - suffixLength;
  } else if (rangeEndStr === undefined) {
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

  if (rangeStart > rangeEnd || rangeStart < 0) {
    return getRangeInvalidResponse(rangeHeaderValue);
  }

  return { object: transformedObject.slice(rangeStart, rangeEnd), hasError: false };
}

function getRangeInvalidResponse (rangeHeaderValue: string, errorMessage?: string): RangeResponse {
  const message = (errorMessage === null || errorMessage === undefined)
    ? `Cannot process specified range: ${rangeHeaderValue}`
    : errorMessage;

  return {
    hasError: true,
    errorCode: ErrorCode.INVALID_RANGE,
    errorMessage: message
  };
}
