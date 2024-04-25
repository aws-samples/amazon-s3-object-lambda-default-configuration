import ErrorCode from '../error/error_code';

export interface RangeResponse {
  object?: Buffer
  headers?: Map<string, object>
  hasError: boolean
  errorCode?: ErrorCode
  errorMessage?: string
}
