import { getPartNumber, getRange } from './utils'
import { UserRequest } from '../s3objectlambda_event'

/**
 * Responsible for validating the user request. Returns a string error message if there are errors or null if valid.
 */
export default function validate (userRequest: UserRequest): string | null {
  const range = getRange(userRequest)
  const partNumber = getPartNumber(userRequest)

  if (range != null && partNumber != null) {
    return 'Cannot specify both Range header and partNumber query parameter'
  }
  return null
}
