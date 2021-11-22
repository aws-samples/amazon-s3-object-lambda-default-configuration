import ErrorCode from '../error/error_code';
import RangeResponse from './range_response.types';

const PART_SIZE = 5242880; // 5 MB

export default function mapPartNumber (partNumber: string, transformedObject: Buffer): RangeResponse {
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
