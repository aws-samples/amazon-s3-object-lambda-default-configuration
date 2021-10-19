/**
 * Contains utility methods for Request handling, such as extracting query parameters.
 */
import { UserRequest } from '../s3objectlambda_event'

const RANGE = 'Range'
const PART_NUMBER = 'partNumber'

export function getPartNumber (userRequest: UserRequest): string | null {
  // PartNumber can be present as a request query parameter.
  return getQueryParam(userRequest.url, PART_NUMBER)
}

export function getRange (userRequest: UserRequest): string | null {
  // convert object to a TypeScript Map
  const headersMap = new Map(Object.entries(userRequest.headers).map(([k, v]) => [k.toLowerCase(), v]))

  // Range can be present as a request header or query parameter.
  if (headersMap.has(RANGE.toLowerCase())) {
    return headersMap.get(RANGE.toLowerCase())
  } else {
    return getQueryParam(userRequest.url, RANGE)
  }
}

function getQueryParam (url: string, name: string): string | null {
  url = url.toLowerCase()
  name = name.toLowerCase()
  return new URL(url).searchParams.get(name)
}
