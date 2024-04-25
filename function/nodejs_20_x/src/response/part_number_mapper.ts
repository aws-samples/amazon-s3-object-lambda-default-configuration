import ErrorCode from '../error/error_code';
import { RangeResponse } from './range_response.types';
import { CONTENT_LENGTH } from '../request/utils';

const PART_SIZE = 5242880; // 5 MB

export function mapPartNumber (partNumber: string, transformedObject: Buffer): RangeResponse {
  const objectLength = transformedObject.byteLength;
  const totalParts = Math.ceil(objectLength / PART_SIZE); // part numbers start at 1
  const requestedPart = Number(partNumber);

  if (isNaN(requestedPart) || !Number.isInteger(requestedPart) ||
    requestedPart > totalParts || requestedPart <= 0) {
    return {
      hasError: true,
      errorCode: ErrorCode.INVALID_PART,
      errorMessage: `Cannot specify part number: ${requestedPart}. Use part numbers 1 to ${totalParts}.`
    };
  }

  const partStart = (requestedPart - 1) * PART_SIZE;
  const partEnd = Math.min(partStart + PART_SIZE, objectLength);
  return { object: transformedObject.slice(partStart, partEnd), hasError: false };
}

export function mapPartNumberHead (partNumber: string, transformedHeaders: Map<string, object>): RangeResponse {
  let contentLength: number;
  // Create a copy of the headers in order to not alter the original object
  const newHeaders = new Map(transformedHeaders);
  if (!transformedHeaders.has(CONTENT_LENGTH)) {
    return {
      hasError: true,
      errorCode: ErrorCode.INVALID_REQUEST,
      errorMessage: 'No content-length was found in the headers'
    };
  } else {
    contentLength = Number(transformedHeaders.get(CONTENT_LENGTH));
  }
  const totalParts = Math.ceil(contentLength / PART_SIZE); // part numbers start at 1
  const requestedPart = Number(partNumber);

  if (isNaN(requestedPart) || !Number.isInteger(requestedPart) ||
      requestedPart > totalParts || requestedPart <= 0) {
    return {
      hasError: true,
      errorCode: ErrorCode.INVALID_PART,
      errorMessage: `Cannot specify part number: ${requestedPart}. Use part numbers 1 to ${totalParts}.`
    };
  }

  const partStart = (requestedPart - 1) * PART_SIZE;
  const partEnd = Math.min(partStart + PART_SIZE, contentLength);
  newHeaders.set(CONTENT_LENGTH, Object(partEnd - partStart));
  return { headers: newHeaders, hasError: false };
}
