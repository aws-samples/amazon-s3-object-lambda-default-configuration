import ErrorCode from '../error/error_code'

export default interface RangeResponse {
  object?: Buffer
  hasError: boolean
  errorCode?: ErrorCode
  errorMessage?: string
}
