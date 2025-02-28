/**
 * Contains utility methods for Request handling, such as extracting query parameters.
 */
import { mapPartNumber, mapPartNumberHead } from '../response/part_number_mapper';
import { mapRange, mapRangeHead } from '../response/range_mapper';
import { RangeResponse } from '../response/range_response.types';
import { UserRequest } from '../s3objectlambda_event.types';
import fetch, { Response } from 'node-fetch';

// Query parameters names
const RANGE = 'Range';
const PART_NUMBER = 'partNumber';
const X_AMZ_SIGNED_HEADERS = 'X-Amz-SignedHeaders';
const X_AMZN_SIGNED_HEADERS_DELIMETER = ';';

// Header constants
export const CONTENT_LENGTH = 'content-length';

/**
 * Get list of signed headers from the presigned url
 * @param url
 */
export function getSignedHeaders (url: string): string[] {
  const queryParam = getQueryParam(url, X_AMZ_SIGNED_HEADERS);
  return queryParam !== null ? queryParam.split(X_AMZN_SIGNED_HEADERS_DELIMETER) : [];
}

/**
 * Get the part number from the user request
 * @param userRequest The user request
 */
export function getPartNumber (userRequest: UserRequest): string | null {
  // PartNumber can be present as a request query parameter.
  return getQueryParam(userRequest.url, PART_NUMBER);
}

/**
 * Get the range from the user request
 * @param userRequest The user request
 */
export function getRange (userRequest: UserRequest): string | null {
  // Convert object to a TypeScript Map
  const headersMap = new Map(Object.entries(userRequest.headers).map(([k, v]) => [k.toLowerCase(), v]));

  // Range can be present as a request header or query parameter.
  if (headersMap.has(RANGE.toLowerCase())) {
    return headersMap.get(RANGE.toLowerCase());
  } else {
    return getQueryParam(userRequest.url, RANGE);
  }
}

/**
 * Check if the request context has range or partNumber parameter. This helps us handle a ranged request
 * and return only the requested range to the GetObject caller. For more information on range and partNumber,
 * see {@link https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html#API_GetObject_RequestSyntax|GetObject Request Syntax}
 * in the Amazon S3 API Reference.
 * @param transformedObject Object on which the Range or Part number is going to be applied to
 * @param userRequest The user request where it's checking if range or part number is specified
 * @returns The object having range or part number applied on them if they have been specfied in the user request
 */
export function applyRangeOrPartNumber (transformedObject: Buffer, userRequest: UserRequest): RangeResponse {
  const range = getRange(userRequest);
  const partNumber = getPartNumber(userRequest);

  if (range != null) {
    return mapRange(range, transformedObject);
  }

  if (partNumber != null) {
    return mapPartNumber(partNumber, transformedObject);
  }

  // The request was made for the whole object, so return as is.
  return { object: transformedObject, hasError: false };
}

/**
 * Check if the request context has range or partNumber parameter. This helps us handle a ranged request
 * and return only the requested range to the HeadObject caller. For more information on range and partNumber,
 * see {@link https://docs.aws.amazon.com/AmazonS3/latest/API/API_HeadObject.html#API_HeadObject_RequestSyntax|HeadObject
  * Request Syntax}
 * in the Amazon S3 API Reference.
 * @param transformedHeaders Map of the
 * @param userRequest The user request where it's checking if range or part number is specified
 * @returns The object having range or part number applied on them if they have been specfied in the user request
 */
export function applyRangeOrPartNumberHeaders (transformedHeaders: Map<string, object>,
  userRequest: UserRequest): RangeResponse {
  const range = getRange(userRequest);
  const partNumber = getPartNumber(userRequest);

  if (range != null) {
    return mapRangeHead(range, transformedHeaders);
  }

  if (partNumber != null) {
    return mapPartNumberHead(partNumber, transformedHeaders);
  }

  // The request was made for the whole object, so return as is.
  return { headers: transformedHeaders, hasError: false };
}

/**
 * Gets a query parameter from the url, converted to lower case. Returns null, in case it doesn't exist in the url.
 * @param url The url from where the query parameter is going to be extracted
 * @param name The name of the specific query parameter.
 */
function getQueryParam (url: string, name: string): string | null {
  url = url.toLowerCase();
  name = name.toLowerCase();
  return new URL(url).searchParams.get(name);
}

export async function makeS3Request (url: string, userRequest: UserRequest, method: 'GET' | 'HEAD'): Promise<Response> {
  const requestHeaders = getRequestHeaders(userRequest.headers, url);
  // TODO: handle fetch errors
  return fetch(url, {
    method,
    headers: Object.fromEntries(requestHeaders)
  });
}

export function addOptionalHeaders (headersObj: object, httpHeaders: Map<string, string>): void {
  let optionalHeaders = ['If-Match', 'If-Modified-Since', 'If-None-Match', 'If-Unmodified-Since'];
  optionalHeaders = optionalHeaders.map(header => header.toLowerCase());

  new Map(Object.entries(headersObj)).forEach((value: string, key: string) => {
    if (optionalHeaders.includes(key.toLowerCase())) {
      httpHeaders.set(key, value);
    }
  });
}

export function addSignedHeaders (headersObj: object, url: string, httpHeaders: Map<string, string>): void {
  const signedHeaders = getSignedHeaders(url);
  new Map(Object.entries(headersObj)).forEach((value: string, key: string) => {
    if (signedHeaders.includes(key.toLowerCase())) {
      httpHeaders.set(key, value);
    }
  });
}

/**
 * Get all headers that should be included in the pre-signed S3 URL. We do not add headers that will be
 * applied after transformation, such as Range.
 */
export function getRequestHeaders (headersObj: object, url: string): Map<string, string> {
  const httpHeaders: Map<string, string> = new Map();

  // If a header is signed, then it must be included in the actual http call.
  // Otherwise, the lambda will get a signature error response.
  addSignedHeaders(headersObj, url, httpHeaders);

  // Some headers are not signed, but should be passed via a presigned url call to ensure desired behaviour.
  addOptionalHeaders(headersObj, httpHeaders);

  // Additionally, we need to filter out the "Host" header, as the client would retrieve the correct value from
  // the endpoint.
  for (const key of httpHeaders.keys()) {
    if (key.toLowerCase() === 'host') {
      httpHeaders.delete(key);
    }
  }

  return httpHeaders;
}
